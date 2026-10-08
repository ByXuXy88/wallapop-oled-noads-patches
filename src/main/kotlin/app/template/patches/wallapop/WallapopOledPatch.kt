package app.template.patches.wallapop

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Wallapop 1.334.0: LinkColors.isDark and 26 packed Compose Color values.
// Roles verified from LinkColors.toString and the palette constructors.
private val linkColorsConstructor = Fingerprint(
    definingClass = "Lwkh;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("Z") + List(26) { "J" },
)
private val linkColorsDescription = Fingerprint(
    definingClass = "Lwkh;",
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    strings = listOf("LinkColors(isDark=", ", surfacePrimary=", ", surfaceSecondary=", ", surfaceTertiary="),
)

private val linkTheme = Fingerprint(
    definingClass = "Loxu;",
    name = "a",
    returnType = "V",
    parameters = listOf("Lxjh;", "Lcc6;", "Lbf6;", "I"),
)

@Suppress("unused")
val wallapopOledPatch = bytecodePatch(
    name = "Wallapop OLED dark mode",
    description = "Uses pure black for dark-mode Compose surfaces and themed legacy backgrounds. Preserves the original light palette, brand colours, text and icons. Follows Android dark mode.",
    default = true,
) {
    compatibleWith(wallapopCompatibility)
    dependsOn(wallapopOledResourcesPatch)
    execute {
        // Validate semantic strings as well as obfuscated class/signature.
        linkColorsDescription.method
        val method = linkColorsConstructor.method
        val implementation = method.implementation
            ?: throw PatchException("LinkColors constructor has no implementation")
        // p0=v0, p1=v1: this exact shape allows reusing v1/v2 after all
        // parameters have been stored. Reject new layouts rather than clobbering this.
        if (implementation.registerCount != 54 || implementation.tryBlocks.isNotEmpty()) {
            throw PatchException("Unexpected LinkColors constructor register/try layout")
        }
        val instructions = implementation.instructions.toList()
        if (instructions.lastOrNull()?.opcode != Opcode.RETURN_VOID ||
            instructions.count { it.opcode == Opcode.RETURN_VOID } != 1) {
            throw PatchException("Unexpected LinkColors constructor return layout")
        }
        val surfaces = setOf("b", "c", "d")
        val stores = instructions.filter { it.opcode == Opcode.IPUT_WIDE }.mapNotNull {
            (it as? ReferenceInstruction)?.reference as? FieldReference
        }
        if (stores.size != 26 || surfaces.any { name ->
                stores.count { it.definingClass == "Lwkh;" && it.name == name && it.type == "J" } != 1
            }) {
            throw PatchException("LinkColors surface fields changed")
        }
        // Some screens explicitly request the Light theme. Follow Android
        // instead, at the existing selection point; the same boolean controls
        // both Compose colours and the wrapped Android night configuration.
        val theme = linkTheme.method
        val themeImpl = theme.implementation
            ?: throw PatchException("LinkTheme has no implementation")
        val themeInstructions = themeImpl.instructions.toList()
        val systemChecks = themeInstructions.mapNotNull {
            ((it as? ReferenceInstruction)?.reference as? MethodReference)
        }.count { it.definingClass == "Lww00;" && it.name == "b" &&
            it.returnType == "Z" && it.parameterTypes == listOf("Lbf6;") }
        val selection = themeInstructions.withIndex().filter { (_, instruction) ->
            val ref = (instruction as? ReferenceInstruction)?.reference as? FieldReference
            instruction.opcode == Opcode.SGET_OBJECT && ref?.definingClass == "Leph;" &&
                ref.name == "a" && ref.type == "Lwkh;"
        }.map { it.index }
        if (themeImpl.registerCount != 67 || systemChecks != 1 || selection.size != 2) {
            throw PatchException("Unexpected LinkTheme palette selection layout")
        }
        theme.addInstructions(selection.first(), "invoke-static {v3}, Lww00;->b(Lbf6;)Z\nmove-result v6")
        val returnIndex = instructions.lastIndex
        // Compose sRGB Color packs unsigned ARGB in the high 32 bits.
        // Test isDark BEFORE v1 (p1) is reused. Light instances branch straight
        // to the existing return. Only three surfaces are changed in dark ones.
        method.addInstructionsWithLabels(
            returnIndex,
            """
                if-eqz p1, :oled_original_return
                const-wide v1, -0x100000000000000L
                iput-wide v1, p0, Lwkh;->b:J
                iput-wide v1, p0, Lwkh;->c:J
                iput-wide v1, p0, Lwkh;->d:J
            """.trimIndent(),
            ExternalLabel("oled_original_return", method.getInstruction(returnIndex)),
        )
    }
}
