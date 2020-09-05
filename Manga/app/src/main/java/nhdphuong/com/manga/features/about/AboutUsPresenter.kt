package nhdphuong.com.manga.features.about

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.BuildConfig
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.repository.MasterDataRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.usecase.GetVersionCodeUseCase
import javax.inject.Inject

class AboutUsPresenter @Inject constructor(
    private val view: AboutUsContract.View,
    private val getVersionCodeUseCase: GetVersionCodeUseCase,
    private val masterDataRepository: MasterDataRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : AboutUsContract.Presenter {
    private val compositeDisposable = CompositeDisposable()

    override fun setUp() {
        io.launch {
            val notificationAllowed = sharedPreferencesManager.isUpgradeNotificationAllowed
            main.launch {
                view.showAppUpgradeNotificationAcceptance(notificationAllowed)
            }

            masterDataRepository.getAppVersion(onSuccess = { latestVersion ->
                if (BuildConfig.VERSION_CODE != latestVersion.versionNumber) {
                    main.launch {
                        view.showAppUpgradeNotification(latestVersion.versionCode)
                    }
                }
            }, onError = { error ->
                Logger.e(TAG, "Failed to check app version with error: $error")
            })
        }

        getVersionCodeUseCase.execute()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { versionCode ->
                view.showTagDataVersion(versionCode.toString())
            }, onError = { error ->
                Logger.e(TAG, "Failed to get version code with error: $error")
            }).addTo(compositeDisposable)
    }

    override fun changeAppUpgradeNotificationAcceptance(notificationAllowed: Boolean) {
        sharedPreferencesManager.isUpgradeNotificationAllowed = notificationAllowed
    }

    override fun clear() {
        compositeDisposable.clear()
    }

    companion object {
        private const val TAG = "AboutUsPresenter"
    }
}
