package nhdphuong.com.manga.features.recent

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import javax.inject.Inject

class RecentActivity : AppCompatActivity() {

    companion object {
        private var mInstance: RecentActivity? = null

        fun start(fragment: Fragment, @RecentType recentType: String) {
            val intent = Intent(fragment.context, RecentActivity::class.java)
            intent.putExtra(Constants.RECENT_TYPE, recentType)
            fragment.startActivityForResult(intent, Constants.BOOK_PREVIEW_REQUEST)
            fragment.activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        fun restart(@RecentType recentType: String) {
            mInstance?.let { recentActivity ->
                recentActivity.intent.putExtra(Constants.RECENT_TYPE, recentType)
                recentActivity.recreate()
                recentActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }

    @Inject
    lateinit var mPresenter: RecentPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent)

        mInstance = this

        var recentFragment = supportFragmentManager.findFragmentById(R.id.clRecentFragment)
                as RecentFragment?
        if (recentFragment == null) {
            recentFragment = RecentFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.clRecentFragment, recentFragment)
                .commitAllowingStateLoss()
        }

        NHentaiApp.instance.applicationComponent.plus(
            RecentModule(recentFragment)
        ).inject(this)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
