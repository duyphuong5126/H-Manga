package nhdphuong.com.manga.features.setting.uimodel

import nhdphuong.com.manga.data.entity.alternativedomain.AlternativeDomainGroup

sealed class SettingUiModel {
    data class AlternativeDomainsUiModel(
        val activeDomainId: String?,
        val alternativeDomainGroup: AlternativeDomainGroup
    ) : SettingUiModel()

    data class AllowAppUpgradeStatus(var isEnabled: Boolean = false) : SettingUiModel()
}