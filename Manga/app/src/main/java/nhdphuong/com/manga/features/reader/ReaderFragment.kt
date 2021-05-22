package nhdphuong.com.manga.features.reader

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.R
import nhdphuong.com.manga.features.NavigationRedirectActivity
import nhdphuong.com.manga.service.NetworkManager
import nhdphuong.com.manga.supports.AnimationHelper
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.views.PreLoadingLinearLayoutManager
import nhdphuong.com.manga.views.adapters.BookReaderAdapter
import nhdphuong.com.manga.views.adapters.ReaderNavigationAdapter
import nhdphuong.com.manga.views.adapters.ReaderSettingsAdapter
import nhdphuong.com.manga.views.addSnapPositionChangedListener
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.customs.MyTextView
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.doOnScrolled
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.scrollToAroundPosition
import nhdphuong.com.manga.views.uimodel.ReaderType
import nhdphuong.com.manga.views.uimodel.ReaderType.HorizontalPage
import nhdphuong.com.manga.views.uimodel.ReaderType.ReversedHorizontalPage
import nhdphuong.com.manga.views.uimodel.ReaderType.VerticalScroll
import nhdphuong.com.manga.views.zoomable.ZoomableBookLayout
import nhdphuong.com.manga.views.zoomable.ZoomableRecyclerView
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/*
 * Created by nhdphuong on 5/5/18.
 */
class ReaderFragment : Fragment(), ReaderContract.View, View.OnClickListener,
    ReaderSettingsAdapter.SettingsChangeListener {

    private lateinit var presenter: ReaderContract.Presenter
    private lateinit var rotationAnimation: Animation
    private lateinit var bookReaderAdapter: BookReaderAdapter
    private lateinit var navigationAdapter: ReaderNavigationAdapter

    private lateinit var llReaderTop: LinearLayout
    private lateinit var ibBack: ImageButton
    private lateinit var ibRefresh: ImageButton
    private lateinit var ibShare: ImageButton
    private lateinit var layoutReaderBottom: LinearLayout
    private lateinit var mtvBookTitle: MyTextView
    private lateinit var mtvCurrentPage: MyTextView
    private lateinit var rvQuickNavigation: RecyclerView
    private lateinit var rvBookPages: RecyclerView
    private lateinit var noNetworkLabel: MyTextView
    private lateinit var ibSettings: ImageButton
    private lateinit var layoutSettings: LinearLayout
    private lateinit var ibCloseSettingLayout: ImageButton
    private lateinit var rvSettings: RecyclerView
    private lateinit var mtvCurrentDirection: MyTextView
    private lateinit var navigatorLeft: MyTextView
    private lateinit var navigatorRight: MyTextView
    private lateinit var verticalBookLayout: ZoomableBookLayout
    private lateinit var verticalBookList: ZoomableRecyclerView

    private var viewDownloadedData = false

    private var readerSettingsAdapter: ReaderSettingsAdapter? = null

    @Inject
    lateinit var networkManager: NetworkManager

    private val snapHelper = PagerSnapHelper()
    private lateinit var spaceItemDecoration: SpaceItemDecoration

    override fun setPresenter(presenter: ReaderContract.Presenter) {
        this.presenter = presenter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reader, container, false)
    }

    override fun onStart() {
        super.onStart()
        context?.let(networkManager::attach)
        if (!viewDownloadedData) {
            if (!networkManager.isConnected) {
                noNetworkLabel.becomeVisible()
            }
            networkManager.addNetworkAvailableTask {
                activity?.runOnUiThread(noNetworkLabel::gone)
            }
            networkManager.addNetworkUnAvailableTask {
                activity?.runOnUiThread(noNetworkLabel::becomeVisible)
            }
        }
    }

    private var bottomReaderTemplate = ""
    private var backToGallery = ""
    private var toastStoragePermissionLabel: String = ""
    private var downloadProgressTemplate = ""
    private var nowReading = ""
    private var pageSharingTitleTemplate = ""
    private var pageSharingChooserTemplate = ""
    private var currentDirectionTemplate = ""
    private var rightToLeft = ""
    private var leftToRight = ""
    private var topDown = ""
    private var previousLabel = ""
    private var nextLabel = ""

    private var transparentColor = -1
    private var primaryColor = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            bottomReaderTemplate = it.getString(R.string.bottom_reader)
            backToGallery = it.getString(R.string.back_to_gallery)
            toastStoragePermissionLabel = it.getString(R.string.toast_storage_permission_require)
            downloadProgressTemplate = it.getString(R.string.download_progress)
            nowReading = it.getString(R.string.now_reading)
            pageSharingTitleTemplate = it.getString(R.string.page_sharing_title)
            pageSharingChooserTemplate = it.getString(R.string.page_sharing_chooser_title)
            currentDirectionTemplate = it.getString(R.string.current_direction_template)
            rightToLeft = it.getString(R.string.right_to_left)
            leftToRight = it.getString(R.string.left_to_right)
            topDown = it.getString(R.string.top_down)
            previousLabel = it.getString(R.string.previous_label)
            nextLabel = it.getString(R.string.next_label)

            transparentColor = ContextCompat.getColor(it, R.color.transparent)
            primaryColor = ContextCompat.getColor(it, R.color.colorPrimary)

            spaceItemDecoration = SpaceItemDecoration(
                it,
                R.dimen.space_normal,
                showFirstDivider = false,
                showLastDivider = false
            )
        }

        setUpUI(view)

        verticalBookList.addItemDecoration(spaceItemDecoration)

        activity?.let {
            rvQuickNavigation.addItemDecoration(
                SpaceItemDecoration(
                    it,
                    R.dimen.space_medium,
                    showFirstDivider = true,
                    showLastDivider = true
                )
            )
        }

        viewDownloadedData = arguments?.getBoolean(Constants.VIEW_DOWNLOADED_DATA) ?: false
        if (viewDownloadedData) {
            presenter.enableViewDownloadedDataMode()
        }

        ibShare.becomeVisibleIf(!viewDownloadedData)
        context?.let { context ->
            rotationAnimation = AnimationHelper.getRotationAnimation(context)
        }

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP) {
                presenter.endReading()
            }
            return@OnKeyListener false
        })

        presenter.start()
        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                presenter.forceBackToGallery()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        presenter.stop()
        context?.let(networkManager::detach)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ibShare -> {
                presenter.generateSharableLink()
            }

            R.id.ibBack -> {
                presenter.forceBackToGallery()
            }

            R.id.mtvCurrentPage -> {
                presenter.backToGallery()
            }

            R.id.ibSettings -> {
                if (layoutSettings.visibility == View.GONE) {
                    layoutSettings.visibility = View.VISIBLE
                } else {
                    layoutSettings.visibility = View.GONE
                }
            }

            R.id.ibCloseSettingLayout -> {
                layoutSettings.visibility = View.GONE
            }

            R.id.ibRefresh -> {
                presenter.requestVisiblePageRefreshing()
            }

            R.id.navigatorLeft -> {
                presenter.requestLeftPage()
            }

            R.id.navigatorRight -> {
                presenter.requestRightPage()
            }
        }
    }

    override fun showBookTitle(bookTitle: String) {
        mtvBookTitle.let { mtvBookTitle ->
            mtvBookTitle.text = bookTitle
            AnimationHelper.startTextRunning(mtvBookTitle)
        }
    }

    override fun setUpSettingList(readerType: ReaderType, isTapNavigationEnabled: Boolean) {
        context?.let {
            readerSettingsAdapter = ReaderSettingsAdapter(readerType, this, isTapNavigationEnabled)
            rvSettings.adapter = readerSettingsAdapter
            rvSettings.layoutManager = LinearLayoutManager(it, VERTICAL, false)
            rvSettings.addItemDecoration(SpaceItemDecoration(it, R.dimen.space_normal))
        }
        changeDirectionLabel(readerType)
        navigatorLeft.becomeVisibleIf(isTapNavigationEnabled)
        navigatorRight.becomeVisibleIf(isTapNavigationEnabled)
    }

    override fun showBookPages(pageList: List<String>, readerType: ReaderType, startPage: Int) {
        activity?.let { activity ->
            val reverseLayout = readerType == ReversedHorizontalPage
            navigationAdapter = ReaderNavigationAdapter(
                pageList,
                object : ReaderNavigationAdapter.ThumbnailSelectListener {
                    override fun onItemSelected(position: Int) {
                        if (readerType == VerticalScroll) {
                            verticalBookList.scrollToPosition(position)
                        } else {
                            rvBookPages.scrollToPosition(position)
                        }
                        updatePageInfo(position)
                        rvQuickNavigation.scrollToAroundPosition(position, ADDITIONAL_STEPS)
                    }
                })
            rvQuickNavigation.adapter = navigationAdapter
            rvQuickNavigation.layoutManager =
                LinearLayoutManager(activity, HORIZONTAL, reverseLayout)

            bookReaderAdapter = BookReaderAdapter(pageList, readerType) {
                toggleMenu()
            }
            val cacheSize = if (CACHE_SIZE <= pageList.size) CACHE_SIZE else pageList.size
            rvBookPages.setItemViewCacheSize(cacheSize)
            val orientation = if (readerType == VerticalScroll) VERTICAL else HORIZONTAL
            val layoutManager =
                PreLoadingLinearLayoutManager(activity, orientation, reverseLayout, PRELOAD_SIZE)
            when (readerType) {
                VerticalScroll -> {
                    changeToVerticalMode(bookReaderAdapter, layoutManager, startPage)
                }

                else -> {
                    changeToHorizontalMode(bookReaderAdapter, layoutManager, startPage)
                }
            }
        }
    }

    override fun showPageIndicator(currentPage: Int, total: Int) {
        mtvCurrentPage.text = String.format(bottomReaderTemplate, currentPage, total)
    }

    override fun showBackToGallery() {
        mtvCurrentPage.text = backToGallery
    }

    override fun navigateToGallery(lastVisitedPage: Int) {
        presenter.endReading()
        activity?.apply {
            val result = Intent().apply {
                putExtra(Constants.LAST_VISITED_PAGE_RESULT, lastVisitedPage)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    override fun pushNowReadingNotification(readingTitle: String, page: Int, total: Int) {
        activity?.let {
            val notificationIntent = Intent(it, NavigationRedirectActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                it, 0,
                notificationIntent, Intent.FILL_IN_ACTION
            )
            NotificationHelper.sendBigContentNotification(
                nowReading,
                NotificationCompat.PRIORITY_DEFAULT,
                readingTitle,
                true,
                System.currentTimeMillis().toInt(),
                pendingIntent
            ).let { notificationId ->
                presenter.updateNotificationId(notificationId)
            }
        }
    }

    override fun removeNotification(notificationId: Int) {
        NotificationHelper.cancelNotification(notificationId)
    }

    override fun processSharingCurrentPage(bookId: String, bookTitle: String, url: String) {
        val sharingTitle = String.format(pageSharingTitleTemplate, bookId, bookTitle)
        val shareIntent: Intent = Intent(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_SUBJECT, sharingTitle)
            .putExtra(Intent.EXTRA_TEXT, url)
            .setType("text/plain")

        context?.startActivity(
            Intent.createChooser(
                shareIntent,
                pageSharingChooserTemplate
            )
        )
    }

    override fun onTypeChanged(newType: ReaderType) {
        changeDirectionLabel(newType)
        presenter.changeViewMode(newType)
        context?.let {
            Toast.makeText(it, mtvCurrentDirection.text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTapNavigationSettingChanged(isEnabled: Boolean, currentReaderType: ReaderType) {
        presenter.changeTapNavigationSetting(isEnabled)
        navigatorLeft.becomeVisibleIf(isEnabled)
        navigatorRight.becomeVisibleIf(isEnabled)
        if (isEnabled) {
            navigatorLeft.text =
                if (currentReaderType == HorizontalPage) previousLabel else nextLabel
            navigatorRight.text =
                if (currentReaderType == HorizontalPage) nextLabel else previousLabel
            navigatorLeft.setBackgroundColor(primaryColor)
            navigatorRight.setBackgroundColor(primaryColor)

            navigatorLeft.postDelayed({
                navigatorLeft.text = ""
                navigatorRight.text = ""

                navigatorLeft.setBackgroundColor(transparentColor)
                navigatorRight.setBackgroundColor(transparentColor)
            }, 3000)
        }
    }

    override fun goToPage(page: Int) {
        rvBookPages.scrollToPosition(page)
        rvQuickNavigation.scrollToAroundPosition(page, ADDITIONAL_STEPS)
        updatePageInfo(page)
    }

    override fun refreshVisiblePages(readerType: ReaderType) {
        ibRefresh.startAnimation(rotationAnimation)
        ibRefresh.postDelayed(ibRefresh::clearAnimation, 4000)

        if (readerType == VerticalScroll) {
            verticalBookList.layoutManager
        } else {
            rvBookPages.layoutManager
        }.let {
            it as? LinearLayoutManager
        }?.let {
            if (bookReaderAdapter.itemCount > 0) {
                var firstVisible = it.findFirstCompletelyVisibleItemPosition()
                var lastVisible = it.findLastCompletelyVisibleItemPosition()
                if (firstVisible < 0) {
                    firstVisible = 0
                }
                if (lastVisible >= bookReaderAdapter.itemCount) {
                    lastVisible = bookReaderAdapter.itemCount - 1
                }

                (firstVisible..lastVisible).forEach(bookReaderAdapter::notifyItemChanged)
            }
        }
    }

    override fun showLoading() {
        if (isAdded) {
            ibRefresh.startAnimation(rotationAnimation)
        }
    }

    override fun hideLoading() {
        if (isAdded) {
            ibRefresh.clearAnimation()
        }
    }

    override fun isActive(): Boolean = isAdded

    private fun changeToVerticalMode(
        adapter: BookReaderAdapter,
        layoutManager: LinearLayoutManager,
        startPage: Int
    ) {
        rvBookPages.gone()
        rvBookPages.layoutManager = null
        rvBookPages.adapter = null

        verticalBookList.adapter = adapter
        verticalBookList.layoutManager = layoutManager
        verticalBookList.tapListener = {
            toggleMenu()
        }

        snapHelper.attachToRecyclerView(null)

        verticalBookList.clearOnScrollListeners()

        updatePageInfo(startPage)
        rvQuickNavigation.doOnGlobalLayout {
            rvQuickNavigation.scrollToAroundPosition(startPage, ADDITIONAL_STEPS)
        }
        val initialScrollingSetUp = AtomicBoolean(false)
        verticalBookList.doOnScrolled {
            if (initialScrollingSetUp.compareAndSet(false, true)) {
                return@doOnScrolled
            }
            val firstItem = layoutManager.findFirstCompletelyVisibleItemPosition()
            val lastItem = layoutManager.findLastCompletelyVisibleItemPosition()

            if (firstItem in 0..lastItem && lastItem - firstItem <= 1) {
                val middleItem = (firstItem + lastItem) / 2
                updatePageInfo(middleItem)
                rvQuickNavigation.scrollToAroundPosition(middleItem, ADDITIONAL_STEPS)
            }
        }

        navigationAdapter.forceNavigate(startPage)
        verticalBookLayout.becomeVisible()
    }

    private fun changeToHorizontalMode(
        adapter: BookReaderAdapter,
        layoutManager: LinearLayoutManager,
        startPage: Int
    ) {
        verticalBookLayout.gone()
        verticalBookList.layoutManager = null
        verticalBookList.adapter = null
        verticalBookList.tapListener = null

        rvBookPages.adapter = adapter
        rvBookPages.layoutManager = layoutManager

        snapHelper.attachToRecyclerView(rvBookPages)

        rvBookPages.clearOnScrollListeners()

        rvBookPages.addSnapPositionChangedListener(snapHelper, this::onPageChanged)

        rvBookPages.scrollToPosition(startPage)
        updatePageInfo(startPage)
        rvQuickNavigation.doOnGlobalLayout {
            rvQuickNavigation.scrollToAroundPosition(startPage, ADDITIONAL_STEPS)
        }
        rvBookPages.becomeVisible()
    }

    private fun updatePageInfo(page: Int) {
        presenter.updatePageIndicator(page)
        navigationAdapter.updateSelectedIndex(page)
    }

    private fun setUpUI(rootView: View) {
        llReaderTop = rootView.findViewById(R.id.llReaderTop)
        ibBack = rootView.findViewById(R.id.ibBack)
        ibRefresh = rootView.findViewById(R.id.ibRefresh)
        ibShare = rootView.findViewById(R.id.ibShare)
        layoutReaderBottom = rootView.findViewById(R.id.layoutReaderBottom)
        mtvBookTitle = rootView.findViewById(R.id.mtvBookTitle)
        mtvCurrentPage = rootView.findViewById(R.id.mtvCurrentPage)
        rvQuickNavigation = rootView.findViewById(R.id.rvQuickNavigation)
        rvBookPages = rootView.findViewById(R.id.book_pages)
        noNetworkLabel = rootView.findViewById(R.id.no_network_label)
        ibSettings = rootView.findViewById(R.id.ibSettings)
        layoutSettings = rootView.findViewById(R.id.llSettings)
        ibCloseSettingLayout = rootView.findViewById(R.id.ibCloseSettingLayout)
        rvSettings = rootView.findViewById(R.id.rvSettings)
        mtvCurrentDirection = rootView.findViewById(R.id.mtvCurrentDirection)
        navigatorLeft = rootView.findViewById(R.id.navigatorLeft)
        navigatorRight = rootView.findViewById(R.id.navigatorRight)
        verticalBookLayout = rootView.findViewById(R.id.vertical_book_layout)
        verticalBookList = rootView.findViewById(R.id.vertical_list)

        ibBack.setOnClickListener(this)
        mtvCurrentPage.setOnClickListener(this)
        ibSettings.setOnClickListener(this)
        ibCloseSettingLayout.setOnClickListener(this)
        ibShare.setOnClickListener(this)
        ibRefresh.setOnClickListener(this)
        navigatorLeft.setOnClickListener(this)
        navigatorRight.setOnClickListener(this)
    }

    private fun onPageChanged(position: Int) {
        rvQuickNavigation.scrollToAroundPosition(position, ADDITIONAL_STEPS)
        updatePageInfo(position)
    }

    private fun changeDirectionLabel(readerType: ReaderType) {
        val directionLabel = when (readerType) {
            HorizontalPage -> leftToRight
            VerticalScroll -> topDown
            ReversedHorizontalPage -> rightToLeft
        }
        val currentDirectionLabel = String.format(currentDirectionTemplate, directionLabel)
        mtvCurrentDirection.text = currentDirectionLabel
    }

    private fun toggleMenu() {
        activity?.let { activity ->
            if (layoutReaderBottom.visibility == View.VISIBLE) {
                AnimationHelper.startSlideOutTop(activity, llReaderTop) {
                    llReaderTop.visibility = View.GONE
                }
                AnimationHelper.startSlideOutBottom(activity, layoutReaderBottom) {
                    layoutReaderBottom.visibility = View.GONE
                }
            } else {
                AnimationHelper.startSlideInTop(activity, llReaderTop) {
                    llReaderTop.visibility = View.VISIBLE
                }
                AnimationHelper.startSlideInBottom(activity, layoutReaderBottom) {
                    layoutReaderBottom.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        private const val CACHE_SIZE = 15
        private const val PRELOAD_SIZE = 5
        private const val ADDITIONAL_STEPS = 2
    }
}
