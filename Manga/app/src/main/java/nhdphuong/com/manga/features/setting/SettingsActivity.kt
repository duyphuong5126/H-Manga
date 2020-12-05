package nhdphuong.com.manga.features.setting

import android.app.AlarmManager
import android.app.PendingIntent
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
import kotlin.system.exitProcess


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

    override fun showRestartAppMessage() {
        showRestartAppDialog(onOk = {
            val intent = Intent(applicationContext, HomeActivity::class.java)
            val mPendingIntent = PendingIntent.getActivity(
                applicationContext,
                123654,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            val mgr = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
            mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent
            exitProcess(0)
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