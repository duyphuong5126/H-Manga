package nhdphuong.com.manga.features.recent

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
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
            fragment.startActivityForResult(intent, Constants.BOOK_PREVIEW_RESULT)
        }

        fun restart(@RecentType recentType: String) {
            mInstance?.let { recentActivity ->
                recentActivity.intent.putExtra(Constants.RECENT_TYPE, recentType)
                recentActivity.recreate()
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
}
