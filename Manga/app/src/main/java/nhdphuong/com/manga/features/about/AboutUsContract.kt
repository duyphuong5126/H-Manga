package nhdphuong.com.manga.features.about

import nhdphuong.com.manga.features.about.uimodel.AboutUiModel

interface AboutUsContract {
    interface View {
        fun updateAboutItem(index: Int)
        fun showAppUpgradeNotification(latestVersionNumber: Int, latestVersionCode: String)
        fun hideAppUpgradeNotification()
        fun setUpAboutList(aboutList: List<AboutUiModel>)
        fun refreshAboutList()
        fun startInstallApk(apkPath: String)
        fun showUpgradeCompleted(versionNumber: Int)
        fun showUpgradeFailed(versionNumber: Int, versionCode: String)
    }

    interface Presenter {
        fun setUp()
        fun clear()
        fun changeAppUpgradeNotificationAcceptance(notificationAllowed: Boolean)
        fun downloadApk(versionNumber: Int, versionCode: String, outputDirectory: String)
    }
}
