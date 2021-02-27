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
import nhdphuong.com.manga.data.repository.MasterDataRepository
import nhdphuong.com.manga.data.repository.installation.InstallationRepository
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.CurrentAppVersion
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.TagDataVersion
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.AppVersionsCenter
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.SupportEmail
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.SupportTwitter
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.AvailableVersion
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.NewVersionAvailable
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.IFileUtils
import nhdphuong.com.manga.usecase.GetVersionCodeUseCase
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AboutUsPresenter @Inject constructor(
    private val view: AboutUsContract.View,
    private val getVersionCodeUseCase: GetVersionCodeUseCase,
    private val masterDataRepository: MasterDataRepository,
    private val installationRepository: InstallationRepository,
    private val fileUtils: IFileUtils,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : AboutUsContract.Presenter {
    private val compositeDisposable = CompositeDisposable()

    private val aboutList = ArrayList<AboutUiModel>()

    private val tagDataVersion = TagDataVersion("")

    private val logger: Logger by lazy {
        Logger("AboutUsPresenter")
    }

    init {
        aboutList.add(CurrentAppVersion(BuildConfig.VERSION_NAME))
        aboutList.add(tagDataVersion)
        aboutList.add(AppVersionsCenter(REPOSITORY_URL))
        aboutList.add(SupportEmail(EMAIL))
        aboutList.add(SupportTwitter(TWITTER_NAME, TWITTER_URL))
    }

    override fun setUp() {
        view.setUpAboutList(aboutList)
        io.launch {
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
                logger.e("Failed to check app version with error: $error")
            })
        }

        getVersionCodeUseCase.execute()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { versionCode ->
                tagDataVersion.versionCode = versionCode.toString()
                view.updateAboutItem(1)
            }, onError = { error ->
                logger.e("Failed to get version code with error: $error")
            }).addTo(compositeDisposable)

        masterDataRepository.getVersionHistory()
            .timeout(10, TimeUnit.SECONDS)
            .map { versionHistory ->
                versionHistory.filter { it.isActivated && it.versionNumber > BuildConfig.VERSION_CODE }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                view.showLoading()
            }
            .doAfterTerminate {
                view.hideLoading()
            }
            .subscribeBy(onSuccess = { newVersions ->
                newVersions.takeIf { it.isNotEmpty() }?.map {
                    AvailableVersion(it.versionNumber, it.versionCode, it.whatsNew, it.downloadUrl)
                }?.let {
                    aboutList.removeAll { item ->
                        item is NewVersionAvailable || item is AvailableVersion
                    }
                    aboutList.add(NewVersionAvailable)
                    aboutList.addAll(it)
                    view.refreshAboutList()
                }
            }, onError = { error ->
                logger.e("Failed to get version code with error: $error")
            }).addTo(compositeDisposable)
    }

    override fun downloadApk(versionNumber: Int, versionCode: String, outputDirectory: String) {
        if (!fileUtils.isStoragePermissionAccepted()) {
            view.showRequestStoragePermission()
            return
        }
        val downloadingSource = Single.create<AvailableVersion> {
            val targetVersion = aboutList.firstOrNull { version ->
                version is AvailableVersion && version.versionNumber == versionNumber
            } as? AvailableVersion
            targetVersion?.let(it::onSuccess) ?: it.onError(NullPointerException())
        }.flatMap {
            val targetPath = "$outputDirectory/v${it.versionCode}.apk"
            val targetApkExisted = File(targetPath).exists()
            logger.d("Target path $targetPath existed: $targetApkExisted")
            if (targetApkExisted) {
                Single.just(targetPath)
            } else {
                logger.d("Downloading apk from ${it.apkUrl}")
                installationRepository.downloadFile(it.apkUrl, outputDirectory, it.versionCode)
            }
        }

        downloadingSource.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = {
                logger.d("Downloaded and saved file $it")
                view.showUpgradeCompleted(versionNumber)
                view.startInstallApk(it)
            }, onError = {
                logger.e("Failed to download version $versionNumber with error $it")
                view.showUpgradeFailed(versionNumber, versionCode)
            }).addTo(compositeDisposable)
    }

    override fun clear() {
        compositeDisposable.clear()
    }

    companion object {
        private const val REPOSITORY_URL = "https://github.com/duyphuong5126/H-Manga/releases"
        private const val EMAIL = "kanade5126@gmail.com"
        private const val TWITTER_NAME = "Nonoka (@Nonoka5126)"
        private const val TWITTER_URL = "https://twitter.com/Nonoka5126"
    }
}
