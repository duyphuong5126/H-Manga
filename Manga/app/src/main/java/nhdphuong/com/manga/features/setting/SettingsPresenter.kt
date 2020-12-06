package nhdphuong.com.manga.features.setting

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.entity.alternativedomain.AlternativeDomain
import nhdphuong.com.manga.data.repository.MasterDataRepository
import nhdphuong.com.manga.features.setting.uimodel.SettingUiModel
import nhdphuong.com.manga.features.setting.uimodel.SettingUiModel.AllowAppUpgradeStatus
import nhdphuong.com.manga.features.setting.uimodel.SettingUiModel.AlternativeDomainsUiModel
import javax.inject.Inject

class SettingsPresenter @Inject constructor(
    private val view: SettingsContract.View,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val masterDataRepository: MasterDataRepository
) : SettingsContract.Presenter {
    private val compositeDisposable = CompositeDisposable()
    private val settingList = arrayListOf<SettingUiModel>()

    override fun start() {
        settingList.add(AllowAppUpgradeStatus(sharedPreferencesManager.isUpgradeNotificationAllowed))
        view.setUpSettings(settingList)
        masterDataRepository.getAlternativeDomains()
            .map {
                Pair(masterDataRepository.getActiveAlternativeDomainId(), it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (activeDomainId, domainGroup) ->
                Logger.d(TAG, "Alternative domains = $domainGroup")
                settingList.add(AlternativeDomainsUiModel(activeDomainId, domainGroup))
                if (view.isActive()) {
                    view.updateSettingList()
                }
            }, {
                Logger.d(TAG, "Failed to get alternative domains with error $it")
            }).let(compositeDisposable::add)
    }

    override fun activateAlternativeDomain(alternativeDomain: AlternativeDomain) {
        masterDataRepository.activateAlternativeDomain(alternativeDomain)
        view.showRestartAppMessage()
    }

    override fun clearAlternativeDomain() {
        masterDataRepository.disableAlternativeDomain()
        view.showRestartAppMessage()
    }

    override fun changeAppUpgradeNotificationAcceptance(notificationAllowed: Boolean) {
        sharedPreferencesManager.isUpgradeNotificationAllowed = notificationAllowed
    }

    override fun stop() {
        compositeDisposable.clear()
    }

    companion object {
        private const val TAG = "SettingsPresenter"
    }
}