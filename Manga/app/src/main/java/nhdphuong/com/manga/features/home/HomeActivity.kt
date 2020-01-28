package nhdphuong.com.manga.features.home

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Bundle
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import javax.inject.Inject
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.features.RandomContract
import nhdphuong.com.manga.features.SearchContract
import nhdphuong.com.manga.features.header.HeaderFragment
import nhdphuong.com.manga.features.header.HeaderModule
import nhdphuong.com.manga.features.header.HeaderPresenter


class HomeActivity : AppCompatActivity(), SearchContract, RandomContract {
    companion object {
        private const val TAG = "HomeActivity"

        @JvmStatic
        fun start(fromContext: Context) {
            val intent = Intent(fromContext, HomeActivity::class.java)
            fromContext.startActivity(intent)
        }
    }

    @Suppress("unused")
    @Inject
    lateinit var mHomePresenter: HomePresenter

    private lateinit var mHomeFragment: HomeFragment
    private lateinit var mHeaderFragment: HeaderFragment

    @Suppress("unused")
    @Inject
    lateinit var mHeaderPresenter: HeaderPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.e(TAG, "onCreate")
        setContentView(R.layout.activity_home)
        showFragments()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        Logger.e(TAG, "onResume")
        window?.statusBarColor = ContextCompat.getColor(this@HomeActivity, R.color.colorPrimary)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        data?.run {
            if (action == Constants.TAG_SELECTED_ACTION) {
                getStringExtra(Constants.SELECTED_TAG)?.let { selectedTag ->
                    if (!TextUtils.isEmpty(selectedTag)) {
                        mHeaderFragment.updateSearchBar(selectedTag)
                        onSearchInputted(selectedTag)
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Logger.e(TAG, "onKeyDown keyCode=$keyCode, event=${event.action}")
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                return if (isTaskRoot) {
                    val homeIntent = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(homeIntent)
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                    false
                }
            }

            else -> {
                super.onKeyDown(keyCode, event)
                return false
            }
        }

    }

    override fun onSearchInputted(data: String) {
        mHomeFragment.changeSearchInputted(data)
    }

    override fun onRandomSelected() {
        mHomeFragment.randomizeBook()
    }

    private fun showFragments() {
        var homeFragment = supportFragmentManager.findFragmentById(R.id.clMainFragment)
                as HomeFragment?
        if (homeFragment == null) {
            homeFragment = HomeFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.clMainFragment, homeFragment, TAG)
                .addToBackStack(TAG)
                .commitAllowingStateLoss()
        }
        mHomeFragment = homeFragment

        var headerFragment = supportFragmentManager.findFragmentById(R.id.clHeader)
                as HeaderFragment?
        if (headerFragment == null) {
            headerFragment = HeaderFragment()
            supportFragmentManager.beginTransaction().replace(R.id.clHeader, headerFragment, TAG)
                .addToBackStack(TAG).commitAllowingStateLoss()
        }
        headerFragment.setSearchInputListener(this)
        headerFragment.setRandomContract(this)
        mHeaderFragment = headerFragment

        NHentaiApp.instance.applicationComponent.plus(
            HomeModule(homeFragment),
            HeaderModule(headerFragment)
        ).inject(this)
    }
}
