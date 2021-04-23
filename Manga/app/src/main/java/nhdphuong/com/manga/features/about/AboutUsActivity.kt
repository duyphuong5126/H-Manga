package nhdphuong.com.manga.features.about

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_about_us.mbUpgradeButton
import nhdphuong.com.manga.BuildConfig
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.supports.openEmailApp
import nhdphuong.com.manga.supports.openUrl
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.createLoadingDialog
import nhdphuong.com.manga.views.showFailedToUpgradeAppDialog
import nhdphuong.com.manga.views.showInstallationConfirmDialog
import java.io.File
import javax.inject.Inject


class AboutUsActivity : AppCompatActivity(), AboutUsContract.View, View.OnClickListener,
    AboutAdapter.AboutCallback {
    @Inject
    lateinit var presenter: AboutUsContract.Presenter

    private lateinit var backButton: ImageButton
    private lateinit var rvAboutList: RecyclerView

    private var aboutAdapter: AboutAdapter? = null

    private var toastStoragePermissionLabel: String = ""
    private var upgradeButtonTemplate = ""
    private var appBeingUpgradeMessage = ""

    private var loadingDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        NHentaiApp.instance.applicationComponent.plus(AboutUsModule(this)).inject(this)

        loadingDialog = createLoadingDialog()

        toastStoragePermissionLabel = getString(R.string.toast_storage_permission_require)
        upgradeButtonTemplate = getString(R.string.app_upgrade_button)
        appBeingUpgradeMessage = getString(R.string.app_being_upgraded_message)

        setUpView()
        backButton.setOnClickListener(this)
        presenter.setUp()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onDestroy() {
        presenter.clear()
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ibBack -> {
                onBackPressed()
            }
        }
    }

    override fun showAppUpgradeNotification(latestVersionNumber: Int, latestVersionCode: String) {
        mbUpgradeButton.text = String.format(upgradeButtonTemplate, latestVersionCode)
        mbUpgradeButton.setOnClickListener {
            installVersion(latestVersionCode, latestVersionNumber)
        }
        mbUpgradeButton.becomeVisible()
    }

    override fun updateAboutItem(index: Int) {
        aboutAdapter?.notifyItemChanged(index)
    }

    override fun hideAppUpgradeNotification() {
        mbUpgradeButton.becomeVisible()
    }

    override fun setUpAboutList(aboutList: List<AboutUiModel>) {
        aboutAdapter = AboutAdapter(aboutList, this)
        rvAboutList.adapter = aboutAdapter
        rvAboutList.addItemDecoration(
            SpaceItemDecoration(
                this,
                R.dimen.space_small,
                showFirstDivider = false,
                showLastDivider = false
            )
        )
        rvAboutList.layoutManager = object : LinearLayoutManager(this, VERTICAL, false) {
            override fun canScrollVertically(): Boolean {
                val firstVisiblePos = findFirstCompletelyVisibleItemPosition()
                val lastVisiblePos = findLastCompletelyVisibleItemPosition()
                return if (firstVisiblePos in 0..lastVisiblePos) {
                    val visibleItems = lastVisiblePos - firstVisiblePos + 1
                    val children = aboutAdapter?.itemCount ?: 0
                    visibleItems < children
                } else true
            }
        }
    }

    override fun refreshAboutList() {
        aboutAdapter?.notifyDataSetChanged()
    }

    override fun openLink(url: String) {
        openUrl(url)
    }

    override fun openEmailAddress(email: String) {
        openEmailApp(email, BuildConfig.VERSION_NAME)
    }

    override fun installVersion(versionCode: String, versionNumber: Int) {
        showInstallationConfirmDialog(versionCode, onOk = {
            aboutAdapter?.showInstallationProgress(versionNumber)
            val installationDirectory = NHentaiApp.instance.installationDirectory
            presenter.downloadApk(versionNumber, versionCode, installationDirectory)
        })
    }

    override fun startInstallApk(apkPath: String) {
        val file = File(apkPath)
        if (file.exists()) {
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(uriFromFile(file), "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(Intent.createChooser(intent, "Choose an app"))
            } catch (error: Throwable) {
                Toast.makeText(this, "Cannot install app because of $error", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showAppBeingUpgraded() {
        Toast.makeText(this, appBeingUpgradeMessage, Toast.LENGTH_SHORT).show()
    }

    override fun showUpgradeCompleted(versionNumber: Int) {
        aboutAdapter?.hideInstallationProgress(versionNumber)
    }

    override fun showUpgradeFailed(versionNumber: Int, versionCode: String) {
        aboutAdapter?.hideInstallationProgress(versionNumber)
        showFailedToUpgradeAppDialog(versionCode, onOk = {
            installVersion(versionCode, versionNumber)
        }, onCancel = {
            openUrl(REPOSITORY_URL)
        })
    }

    override fun showLoading() {
        loadingDialog?.show()
    }

    override fun hideLoading() {
        loadingDialog?.dismiss()
    }

    private fun uriFromFile(file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
        } else {
            Uri.fromFile(file)
        }
    }

    private fun setUpView() {
        backButton = findViewById(R.id.ibBack)
        rvAboutList = findViewById(R.id.rvAboutList)
    }

    companion object {
        private const val REPOSITORY_URL = "https://github.com/duyphuong5126/H-Manga/releases"

        @JvmStatic
        fun start(fromContext: Context) {
            val intent = Intent(fromContext, AboutUsActivity::class.java)
            fromContext.startActivity(intent)
        }
    }
}
