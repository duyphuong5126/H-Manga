package nhdphuong.com.manga.features.setting

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.alternativedomain.AlternativeDomain
import nhdphuong.com.manga.features.setting.uimodel.SettingUiModel

interface SettingsContract {
    interface View : Base.View<Presenter> {
        fun setUpSettings(settingList: List<SettingUiModel>)
        fun updateSettingList()
        fun showRestartAppMessage()
    }

    interface Presenter : Base.Presenter {
        fun activateAlternativeDomain(alternativeDomain: AlternativeDomain)
        fun clearAlternativeDomain()
        fun changeAppUpgradeNotificationAcceptance(notificationAllowed: Boolean)
    }
}