package app.template.patches.wallapop

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element
import java.io.File

private const val androidNs = "http://schemas.android.com/apk/res/android"
private val backgroundRoles = setOf("background", "backgroundTint", "cardBackgroundColor", "colorBackground", "windowBackground", "colorBackgroundFloating", "colorSurface", "colorSurfaceVariant")
private val foregroundRoles = setOf("colorForeground", "colorOnBackground", "colorOnSurface", "textColor", "textColorPrimary", "textColorSecondary", "textColorHint", "tint", "drawableTint", "iconTint", "titleTextColor", "subtitleTextColor", "searchBoxTextColor", "searchBoxTextColorHint")
private val lightNeutrals = setOf("@color/white", "@android:color/white", "@color/dark_scale_gray_4", "@color/dark_scale_gray_5", "@color/dark_scale_gray_6", "#fff", "#ffffff", "#ffffffff", "#f3f3f3", "#fff3f3f3", "#f5f5f5", "#fff5f5f5", "#fafafa", "#fffafafa")
private val darkNeutrals = setOf("@color/black", "@android:color/black", "@color/dark_scale_gray_1", "@color/dark_scale_gray_2", "#000", "#000000", "#ff000000", "#333333", "#ff333333", "#707070", "#ff707070")

// Internal dependency; the public bytecode patch selects both halves together.
internal val wallapopOledResourcesPatch = resourcePatch {
    execute {
        val res = get("res")
        if (!res.isDirectory) throw PatchException("Decoded resources not found")
        val aliases = linkedMapOf<Pair<String, String>, String>()
        fun alias(original: String, night: String): String {
            val name = aliases.getOrPut(original to night) { "wallapop_oled_role_${aliases.size}" }
            return "@color/$name"
        }
        fun convert(value: String, background: Boolean): String {
            val normalized = value.trim().lowercase()
            return when {
                background && (normalized in lightNeutrals || normalized in darkNeutrals) -> alias(value, "#ff000000")
                !background && normalized in darkNeutrals -> alias(value, "#fff5f5f5")
                else -> value
            }
        }
        fun hasExplicitBrandBackground(element: Element): Boolean {
            val attributes = element.attributes
            for (index in 0 until attributes.length) {
                val attribute = attributes.item(index)
                val role = attribute.localName ?: attribute.nodeName.substringAfter(':')
                if (role !in backgroundRoles) continue
                val value = attribute.nodeValue.lowercase()
                if (listOf("brand", "primary", "accent", "green", "accept_photos", "critical", "error", "success").any { it in value } &&
                    value !in lightNeutrals && value !in darkNeutrals) return true
            }
            return false
        }
        val backgroundDrawables = mutableSetOf<String>()
        var baseThemes = 0
        var changes = 0
        val xmlFiles = res.walkTopDown().filter { it.isFile && it.extension == "xml" }.toList()
        xmlFiles.forEach { file ->
            val dir = file.parentFile.name
            if (!(dir == "values" || dir.startsWith("values-") || dir.startsWith("layout"))) return@forEach
            val path = "res/${file.relativeTo(res).invariantSeparatorsPath}"
            document(path).use { doc ->
                val elements = doc.getElementsByTagName("*")
                for (i in 0 until elements.length) {
                    val element = elements.item(i) as? Element ?: continue
                    if (element.tagName == "style" && element.getAttribute("name") == "BaseAppTheme") {
                        // DayNight is required: a Light parent ignores Android night mode.
                        if (element.getAttribute("parent").removePrefix("@style/") != "Theme.AppCompat.Light.NoActionBar" &&
                            element.getAttribute("parent").removePrefix("@style/") != "Theme.AppCompat.DayNight.NoActionBar") {
                            throw PatchException("Unexpected Wallapop BaseAppTheme parent")
                        }
                        element.setAttribute("parent", "@style/Theme.AppCompat.DayNight.NoActionBar")
                        val overrides = mapOf(
                            "android:windowBackground" to alias("@color/white", "#ff000000"),
                            "android:colorBackground" to alias("@color/white", "#ff000000"),
                            "colorBackgroundFloating" to alias("@color/white", "#ff000000"),
                            "android:statusBarColor" to alias("@color/white", "#ff000000"),
                            "android:navigationBarColor" to alias("@color/black", "#ff000000"),
                            "android:windowLightStatusBar" to "@bool/wallapop_oled_light_status_icons",
                            "android:windowLightNavigationBar" to "false",
                            "android:forceDarkAllowed" to "false",
                        )
                        overrides.forEach { (name, value) ->
                            val nodes = element.childNodes
                            val existing = (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
                                .firstOrNull { it.tagName == "item" && it.getAttribute("name") == name }
                            val item = existing ?: doc.createElement("item").also {
                                it.setAttribute("name", name); element.appendChild(it)
                            }
                            item.textContent = value
                        }
                        baseThemes++
                    }
                    if (element.tagName == "item" && element.parentNode.nodeName == "style") {
                        val role = element.getAttribute("name").substringAfter(':')
                        val original = element.textContent.trim()
                        if (role in backgroundRoles && original.startsWith("@drawable/")) backgroundDrawables += original.substringAfter('/')
                        if (role in backgroundRoles || role in foregroundRoles) {
                            val replacement = convert(original, role in backgroundRoles)
                            if (replacement != original) { element.textContent = replacement; changes++ }
                        }
                    }
                    if (dir.startsWith("layout")) {
                        val attrs = element.attributes
                        for (j in 0 until attrs.length) {
                            val attr = attrs.item(j)
                            val role = attr.localName ?: attr.nodeName.substringAfter(':')
                            if (role in backgroundRoles && attr.nodeValue.startsWith("@drawable/")) backgroundDrawables += attr.nodeValue.substringAfter('/')
                            if (role in backgroundRoles || role in foregroundRoles) {
                                val replacement = if (role in foregroundRoles && hasExplicitBrandBackground(element)) {
                                    attr.nodeValue
                                } else convert(attr.nodeValue, role in backgroundRoles)
                                if (replacement != attr.nodeValue) { attr.nodeValue = replacement; changes++ }
                            }
                        }
                    }
                }
            }
        }
        if (baseThemes == 0) throw PatchException("Expected Wallapop BaseAppTheme not found")
        // Only drawables referenced as backgrounds; never recolour icon paths or images.
        xmlFiles.filter { it.parentFile.name.startsWith("drawable") && it.nameWithoutExtension in backgroundDrawables }.forEach { file ->
            document("res/${file.relativeTo(res).invariantSeparatorsPath}").use { doc ->
                val solids = doc.getElementsByTagName("solid")
                for (i in 0 until solids.length) {
                    val solid = solids.item(i) as? Element ?: continue
                    val original = solid.getAttributeNS(androidNs, "color")
                    val replacement = convert(original, true)
                    if (replacement != original) { solid.setAttributeNS(androidNs, "android:color", replacement); changes++ }
                }
            }
        }
        File(res, "values").mkdirs()
        File(res, "values-night").mkdirs()
        // ARSCLib infers a values file's type from its filename. Merge each
        // type into colors.xml / bools.xml rather than a mixed custom file.
        fun appendValues(path: String, type: String, entries: Map<String, String>) {
            val file = get(path, copy = false)
            if (!file.exists()) file.writeText("<?xml version=\"1.0\" encoding=\"utf-8\"?><resources/>")
            document(path).use { doc ->
                val root = doc.documentElement
                entries.forEach { (name, value) ->
                    val existing = doc.getElementsByTagName(type)
                    if ((0 until existing.length).any {
                            (existing.item(it) as? Element)?.getAttribute("name") == name
                        }) throw PatchException("OLED resource name already exists: $name")
                    val element = doc.createElement(type)
                    element.setAttribute("name", name)
                    element.textContent = value
                    root.appendChild(element)
                }
            }
        }
        appendValues("res/values/colors.xml", "color", aliases.entries.associate { it.value to it.key.first })
        appendValues("res/values-night/colors.xml", "color", aliases.entries.associate { it.value to it.key.second })
        appendValues("res/values/bools.xml", "bool", mapOf("wallapop_oled_light_status_icons" to "true"))
        appendValues("res/values-night/bools.xml", "bool", mapOf("wallapop_oled_light_status_icons" to "false"))
        println("Wallapop OLED: $baseThemes theme variant(s), $changes semantic XML replacements")
    }
}
