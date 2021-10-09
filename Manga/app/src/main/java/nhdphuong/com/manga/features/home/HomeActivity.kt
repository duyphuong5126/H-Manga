package nhdphuong.com.manga.features.home

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import nhdphuong.com.manga.Constants.Companion.ACTION_SEARCH_QUERY_CHANGED
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.SEARCH_INFO
import nhdphuong.com.manga.Constants.Companion.SELECTED_TAG
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.features.RandomContract
import nhdphuong.com.manga.features.SearchContract
import nhdphuong.com.manga.features.header.HeaderFragment
import nhdphuong.com.manga.features.header.HeaderModule
import nhdphuong.com.manga.features.header.HeaderPresenter
import nhdphuong.com.manga.work.VersionCheckWorker
import javax.inject.Inject


class HomeActivity : AppCompatActivity(), SearchContract, RandomContract {
    companion object {
        private const val TAG = "HomeActivity"

        @JvmStatic
        fun start(fromContext: Context) {
            val intent = Intent(fromContext, HomeActivity::class.java)
            fromContext.startActivity(intent)
        }

        @JvmStatic
        fun start(fromContext: Context, bookId: String) {
            val intent = Intent(fromContext, HomeActivity::class.java).putExtra(BOOK_ID, bookId)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            fromContext.startActivity(intent)
        }

        @JvmStatic
        fun startSearching(fromContext: Context, searchTerm: String) {
            val intent =
                Intent(fromContext, HomeActivity::class.java).putExtra(SEARCH_INFO, searchTerm)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
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

    private val searchQueryChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.run {
                if (action == ACTION_SEARCH_QUERY_CHANGED) {
                    val selectedTag = getStringExtra(SELECTED_TAG)
                    if (!selectedTag.isNullOrBlank()) {
                        mHeaderFragment.updateSearchBar(selectedTag)
                        onSearchInputted(selectedTag)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        showFragments()
        BroadCastReceiverHelper.registerBroadcastReceiver(
            this,
            searchQueryChangedReceiver,
            ACTION_SEARCH_QUERY_CHANGED
        )
        VersionCheckWorker.start(this)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        window?.statusBarColor = ContextCompat.getColor(this@HomeActivity, R.color.colorPrimary)
    }

    override fun onDestroy() {
        super.onDestroy()
        BroadCastReceiverHelper.unRegisterBroadcastReceiver(this, searchQueryChangedReceiver)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
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

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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
            val bookId = intent?.getStringExtra(BOOK_ID)
            if (!bookId.isNullOrBlank()) {
                homeFragment.arguments = Bundle().apply {
                    putString(BOOK_ID, bookId)
                }
            } else {
                intent?.getStringExtra(SEARCH_INFO)?.let { searchInfo ->
                    homeFragment.arguments = Bundle().apply {
                        putString(SEARCH_INFO, searchInfo)
                    }
                }
            }
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
            intent?.getStringExtra(SEARCH_INFO)?.let { searchInfo ->
                headerFragment.arguments = Bundle().apply {
                    putString(SEARCH_INFO, searchInfo)
                }
            }
            supportFragmentManager.beginTransaction().replace(R.id.clHeader, headerFragment, TAG)
                .addToBackStack(TAG).commitAllowingStateLoss()
        }
        headerFragment.setSearchInputListener(this)
        headerFragment.setRandomContract(this)
        mHeaderFragment = headerFragment

        val homeComponent = NHentaiApp.instance.applicationComponent.plus(
            HomeModule(homeFragment),
            HeaderModule(headerFragment)
        )
        homeComponent.inject(this)
        homeComponent.inject(homeFragment)
    }
}
