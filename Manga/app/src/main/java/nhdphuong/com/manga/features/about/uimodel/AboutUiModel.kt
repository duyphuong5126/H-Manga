package nhdphuong.com.manga.features.about.uimodel

sealed class AboutUiModel {
    data class CurrentAppVersion(val versionCode: String) : AboutUiModel()

    data class TagDataVersion(var versionCode: String) : AboutUiModel()

    data class AppVersionsCenter(val url: String) : AboutUiModel()

    data class SupportEmail(val address: String) : AboutUiModel()

    data class SupportTwitter(val name: String, val url: String) : AboutUiModel()

    data class AllowAppUpgradeStatus(var isEnabled: Boolean = false) : AboutUiModel()

    object NewVersionAvailable : AboutUiModel()

    data class AvailableVersion(
        val versionNumber: Int,
        val versionCode: String,
        val whatsNew: String,
        val apkUrl: String
    ) : AboutUiModel()
}