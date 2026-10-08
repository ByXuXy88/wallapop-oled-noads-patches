package app.template.patches.wallapop

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch

private val shouldShowAds = Fingerprint(
    definingClass = "Lcom/wallapop/ads/featureflags/domain/usecase/ShouldShowAdsCommand;",
    name = "invoke",
    returnType = "Z",
    parameters = emptyList(),
)

private val adsEnabled = Fingerprint(
    definingClass = "Lcom/wallapop/ads/featureflags/data/AdsFeatureFlagsDataSourceImpl;",
    name = "getShouldShowAds",
    returnType = "Z",
    parameters = emptyList(),
)

@Suppress("unused")
val wallapopNoAdsPatch = bytecodePatch(
    name = "Wallapop No Ads",
    description = "Disables Wallapop's central advertising eligibility gates. Does not unlock subscriptions or remove promoted marketplace listings.",
    default = true,
) {
    compatibleWith(wallapopCompatibility)
    execute {
        // Resolve and validate both before mutating. A changed APK must fail clearly.
        val methods = listOf(shouldShowAds.method, adsEnabled.method)
        methods.forEach {
            val implementation = it.implementation
                ?: throw PatchException("Advertising gate has no implementation: ${it.name}")
            if (implementation.registerCount < 1) {
                throw PatchException("Advertising gate has no usable register: ${it.name}")
            }
        }
        // v0 may be the instance register in the tiny getter. Overwriting it is
        // valid because execution returns immediately, before the original body.
        methods.forEach { it.addInstructions(0, "const/4 v0, 0x0\nreturn v0") }
    }
}
