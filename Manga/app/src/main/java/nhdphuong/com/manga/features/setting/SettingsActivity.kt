package nhdphuong.com.manga.features.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.alternativedomain.AlternativeDomain
import nhdphuong.com.manga.features.home.HomeActivity
import nhdphuong.com.manga.features.setting.uimodel.SettingUiModel
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.views.showRestartAppDialog
import javax.inject.Inject


class SettingsActivity : AppCompatActivity(), SettingsContract.View,
    SettingsAdapter.SettingCallback, View.OnClickListener {
    @Inject
    lateinit var presenter: SettingsContract.Presenter

    private var settingsAdapter: SettingsAdapter? = null

    private lateinit var rvSettings: RecyclerView
    private lateinit var ibBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        NHentaiApp.instance.applicationComponent.plus(SettingsModule(this)).inject(this)

        rvSettings = findViewById(R.id.rvSettings)
        ibBack = findViewById(R.id.ibBack)
        ibBack.setOnClickListener(this)

        presenter.start()
    }

    override fun setUpSettings(settingList: List<SettingUiModel>) {
        settingsAdapter = SettingsAdapter(settingList, this)
        rvSettings.adapter = settingsAdapter
        rvSettings.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvSettings.addItemDecoration(
            SpaceItemDecoration(
                this, R.dimen.space_medium,
                showFirstDivider = false,
                showLastDivider = false
            )
        )
    }

    override fun updateSettingList() {
        settingsAdapter?.notifyDataSetChanged()
    }

    override fun showRestartAppMessage() {
        showRestartAppDialog(onOk = {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finishAffinity()
            Runtime.getRuntime().exit(0)
        })
    }

    override fun changeAppUpgradeNotificationStatus(enabled: Boolean) {
        presenter.changeAppUpgradeNotificationAcceptance(enabled)
    }

    override fun onDomainSelected(alternativeDomain: AlternativeDomain) {
        presenter.activateAlternativeDomain(alternativeDomain)
    }

    override fun onClearAlternativeDomain() {
        presenter.clearAlternativeDomain()
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ibBack -> onBackPressed()
            else -> {
            }
        }
    }

    override fun isActive(): Boolean {
        val currentState = lifecycle.currentState
        return currentState != Lifecycle.State.DESTROYED
    }

    companion object {
        @JvmStatic
        fun start(fromContext: Context) {
            fromContext.startActivity(Intent(fromContext, SettingsActivity::class.java))
        }
    }
}