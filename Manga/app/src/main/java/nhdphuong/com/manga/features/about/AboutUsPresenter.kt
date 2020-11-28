package nhdphuong.com.manga.features.about

import io.reactivex.Single
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
import nhdphuong.com.manga.data.repository.installation.InstallationRepository
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.CurrentAppVersion
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.TagDataVersion
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.AppVersionsCenter
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.SupportEmail
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.SupportTwitter
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.AllowAppUpgradeStatus
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.AvailableVersion
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.usecase.GetVersionCodeUseCase
import java.io.File
import javax.inject.Inject

class AboutUsPresenter @Inject constructor(
    private val view: AboutUsContract.View,
    private val getVersionCodeUseCase: GetVersionCodeUseCase,
    private val masterDataRepository: MasterDataRepository,
    private val installationRepository: InstallationRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : AboutUsContract.Presenter {
    private val compositeDisposable = CompositeDisposable()

    private val aboutList = ArrayList<AboutUiModel>()

    private val tagDataVersion = TagDataVersion("")
    private val allowAppUpgradeStatus = AllowAppUpgradeStatus()

    init {
        aboutList.add(CurrentAppVersion(BuildConfig.VERSION_NAME))
        aboutList.add(tagDataVersion)
        aboutList.add(AppVersionsCenter(REPOSITORY_URL))
        aboutList.add(SupportEmail(EMAIL))
        aboutList.add(SupportTwitter(TWITTER_NAME, TWITTER_URL))
        aboutList.add(allowAppUpgradeStatus)
    }

    override fun setUp() {
        view.setUpAboutList(aboutList)
        io.launch {
            allowAppUpgradeStatus.isEnabled = sharedPreferencesManager.isUpgradeNotificationAllowed
            main.launch {
                view.updateAboutItem(5)
            }

            masterDataRepository.getAppVersion(onSuccess = { latestVersion ->
                if (BuildConfig.VERSION_CODE != latestVersion.versionNumber) {
                    main.launch {
                        view.showAppUpgradeNotification(
                            latestVersion.versionNumber,
                            latestVersion.versionCode
                        )
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
                tagDataVersion.versionCode = versionCode.toString()
                view.updateAboutItem(1)
            }, onError = { error ->
                Logger.e(TAG, "Failed to get version code with error: $error")
            }).addTo(compositeDisposable)

        masterDataRepository.getVersionHistory()
            .map { versionHistory ->
                versionHistory.filter { it.versionNumber > BuildConfig.VERSION_CODE }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { newVersions ->
                val currentVersion = BuildConfig.VERSION_CODE
                newVersions.filter {
                    it.isActivated && it.versionNumber > currentVersion
                }.takeIf { it.isNotEmpty() }?.map {
                    AvailableVersion(it.versionNumber, it.versionCode, it.whatsNew, it.downloadUrl)
                }?.let {
                    aboutList.removeAll { item -> item is AboutUiModel.NewVersionAvailable || item is AvailableVersion }
                    aboutList.add(AboutUiModel.NewVersionAvailable)
                    aboutList.addAll(it)
                    view.refreshAboutList()
                }
            }, onError = { error ->
                Logger.e(TAG, "Failed to get version code with error: $error")
            }).addTo(compositeDisposable)
    }

    override fun changeAppUpgradeNotificationAcceptance(notificationAllowed: Boolean) {
        sharedPreferencesManager.isUpgradeNotificationAllowed = notificationAllowed
    }

    override fun downloadApk(versionNumber: Int, versionCode: String, outputDirectory: String) {
        val downloadingSource = Single.create<AvailableVersion> {
            val targetVersion = aboutList.firstOrNull { version ->
                version is AvailableVersion && version.versionNumber == versionNumber
            } as? AvailableVersion
            targetVersion?.let(it::onSuccess) ?: it.onError(NullPointerException())
        }.flatMap {
            val targetPath = "$outputDirectory/v${it.versionCode}.apk"
            val targetApkExisted = File(targetPath).exists()
            Logger.d(TAG, "Target path $targetPath existed: $targetApkExisted")
            if (targetApkExisted) {
                Single.just(targetPath)
            } else {
                installationRepository.downloadFile(it.apkUrl, outputDirectory, it.versionCode)
            }
        }

        downloadingSource.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = {
                Logger.d(TAG, "Downloaded and saved file $it")
                view.showUpgradeCompleted(versionNumber)
                view.startInstallApk(it)
            }, onError = {
                Logger.d(TAG, "Failed to download version $versionNumber with error $it")
                view.showUpgradeFailed(versionNumber, versionCode)
            }).addTo(compositeDisposable)
    }

    override fun clear() {
        compositeDisposable.clear()
    }

    companion object {
        private const val TAG = "AboutUsPresenter"
        private const val REPOSITORY_URL = "https://github.com/duyphuong5126/H-Manga/releases"
        private const val EMAIL = "kanade5126@gmail.com"
        private const val TWITTER_NAME = "Nonoka (@Nonoka5126)"
        private const val TWITTER_URL = "https://twitter.com/Nonoka5126"
    }
}
