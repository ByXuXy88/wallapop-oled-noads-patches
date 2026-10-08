package app.template.patches.wallapop

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal val wallapopCompatibility = Compatibility(
    name = "Wallapop",
    packageName = "com.wallapop",
    apkFileType = ApkFileType.APK,
    appIconColor = 0x13C1AC,
    targets = listOf(AppTarget(version = "1.334.0", versionCode = 10141414)),
)
