package nhdphuong.com.manga.features.about

interface AboutUsContract {
    interface View {
        fun showTagDataVersion(versionCode: String)
        fun showAppUpgradeNotificationAcceptance(notificationAllowed: Boolean)
        fun showAppUpgradeNotification()
        fun hideAppUpgradeNotification()
    }

    interface Presenter {
        fun setUp()
        fun clear()
        fun changeAppUpgradeNotificationAcceptance(notificationAllowed: Boolean)
    }
}
