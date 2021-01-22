package nhdphuong.com.manga.features.reader

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.R
import nhdphuong.com.manga.features.NavigationRedirectActivity
import nhdphuong.com.manga.service.NetworkManager
import nhdphuong.com.manga.supports.AnimationHelper
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.views.PreLoadingLinearLayoutManager
import nhdphuong.com.manga.views.adapters.BookReaderAdapter
import nhdphuong.com.manga.views.adapters.ReaderNavigationAdapter
import nhdphuong.com.manga.views.addSnapPositionChangedListener
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.customs.MyTextView
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.doOnScrolled
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.scrollToAroundPosition
import nhdphuong.com.manga.views.showStoragePermissionDialog
import nhdphuong.com.manga.views.uimodel.ReaderType
import nhdphuong.com.manga.views.uimodel.ReaderType.HorizontalPage
import nhdphuong.com.manga.views.uimodel.ReaderType.VerticalScroll
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/*
 * Created by nhdphuong on 5/5/18.
 */
class ReaderFragment : Fragment(), ReaderContract.View, View.OnClickListener {

    private lateinit var presenter: ReaderContract.Presenter
    private lateinit var rotationAnimation: Animation
    private lateinit var bookReaderAdapter: BookReaderAdapter
    private lateinit var navigationAdapter: ReaderNavigationAdapter

    private lateinit var clDownloadedPopup: ConstraintLayout
    private lateinit var llReaderTop: LinearLayout
    private lateinit var ibBack: ImageButton
    private lateinit var ibDownload: ImageButton
    private lateinit var ibDownloadPopupClose: ImageButton
    private lateinit var ibRefresh: ImageButton
    private lateinit var ibShare: ImageButton
    private lateinit var layoutReaderBottom: LinearLayout
    private lateinit var mtvBookTitle: MyTextView
    private lateinit var mtvCurrentPage: MyTextView
    private lateinit var mtvDownloadTitle: MyTextView
    private lateinit var rvQuickNavigation: RecyclerView
    private lateinit var rvBookPages: RecyclerView
    private lateinit var viewModeButton: ImageButton
    private lateinit var noNetworkLabel: MyTextView

    private var viewDownloadedData = false

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
        }

        setUpUI(view)

        context?.let {
            spaceItemDecoration = SpaceItemDecoration(
                it,
                R.dimen.space_normal,
                showFirstDivider = false,
                showLastDivider = false
            )
        }

        viewDownloadedData = arguments?.getBoolean(Constants.VIEW_DOWNLOADED_DATA) ?: false
        if (viewDownloadedData) {
            presenter.enableViewDownloadedDataMode()
        }

        ibDownload.becomeVisibleIf(!viewDownloadedData, otherWiseVisibility = View.INVISIBLE)
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.start()
        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                presenter.forceBackToGallery()
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            val permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                showRequestStoragePermission()
            }
            val result = if (permissionGranted) "granted" else "denied"
            Logger.d(TAG, "Storage permission is $result")
        }
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

            R.id.ibDownload -> {
                presenter.downloadCurrentPage()
            }

            R.id.ibDownloadPopupClose -> {
                hideDownloadPopup()
            }

            R.id.ibRefresh -> {
                ibRefresh.startAnimation(rotationAnimation)
                presenter.reloadCurrentPage { currentPage: Int ->
                    bookReaderAdapter.resetPage(currentPage)
                    ibRefresh.postDelayed(ibRefresh::clearAnimation, 3000)
                }
            }

            R.id.ib_view_mode -> {
                presenter.toggleViewMode()
            }
        }
    }

    override fun showBookTitle(bookTitle: String) {
        mtvBookTitle.let { mtvBookTitle ->
            mtvBookTitle.text = bookTitle
            AnimationHelper.startTextRunning(mtvBookTitle)
        }
    }

    override fun showBookPages(pageList: List<String>, readerType: ReaderType, startPage: Int) {
        viewModeButton.setImageResource(
            if (readerType == HorizontalPage) {
                R.drawable.ic_horizontal_view_white
            } else {
                R.drawable.ic_vertical_view_white
            }
        )
        activity?.let { activity ->
            navigationAdapter = ReaderNavigationAdapter(
                pageList,
                object : ReaderNavigationAdapter.ThumbnailSelectListener {
                    override fun onItemSelected(position: Int) {
                        rvBookPages.scrollToPosition(position)
                        updatePageInfo(position)
                        rvQuickNavigation.scrollToAroundPosition(position, ADDITIONAL_STEPS)
                    }
                })
            rvQuickNavigation.adapter = navigationAdapter
            rvQuickNavigation.layoutManager =
                LinearLayoutManager(activity, HORIZONTAL, false)
            rvQuickNavigation.addItemDecoration(
                SpaceItemDecoration(
                    activity,
                    R.dimen.space_medium,
                    showFirstDivider = false,
                    showLastDivider = true
                )
            )

            bookReaderAdapter = BookReaderAdapter(pageList, readerType) {
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
            rvBookPages.adapter = bookReaderAdapter
            rvBookPages.setItemViewCacheSize(PRELOAD_SIZE)
            val orientation = if (readerType == VerticalScroll) VERTICAL else HORIZONTAL
            val layoutManager =
                PreLoadingLinearLayoutManager(activity, orientation, false, PRELOAD_SIZE)
            rvBookPages.layoutManager = layoutManager
            when (readerType) {
                VerticalScroll -> {
                    changeToVerticalMode(layoutManager, startPage)
                }

                else -> {
                    changeToHorizontalMode(startPage)
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

    override fun showRequestStoragePermission() {
        activity?.showStoragePermissionDialog(onOk = {
            requestStoragePermission()
        }, onDismiss = {
            Toast.makeText(
                activity,
                toastStoragePermissionLabel,
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    override fun showDownloadPopup() {
        clDownloadedPopup.visibility = View.VISIBLE
    }

    override fun hideDownloadPopup() {
        clDownloadedPopup.postDelayed({
            clDownloadedPopup.gone()
        }, 3000)
    }

    override fun updateDownloadPopupTitle(downloadPage: Int) {
        mtvDownloadTitle.text = String.format(downloadProgressTemplate, downloadPage.toString())
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

    private fun changeToVerticalMode(layoutManager: LinearLayoutManager, startPage: Int) {
        rvBookPages.addItemDecoration(spaceItemDecoration)
        snapHelper.attachToRecyclerView(null)

        rvBookPages.clearOnScrollListeners()

        updatePageInfo(startPage)
        rvQuickNavigation.doOnGlobalLayout {
            rvQuickNavigation.scrollToAroundPosition(startPage, ADDITIONAL_STEPS)
        }
        val initialScrollingSetUp = AtomicBoolean(false)
        rvBookPages.doOnScrolled {
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
    }

    private fun changeToHorizontalMode(startPage: Int) {
        rvBookPages.removeItemDecoration(spaceItemDecoration)
        snapHelper.attachToRecyclerView(rvBookPages)

        rvBookPages.clearOnScrollListeners()

        rvBookPages.addSnapPositionChangedListener(snapHelper, this::onPageChanged)

        rvBookPages.scrollToPosition(startPage)
        updatePageInfo(startPage)
        rvQuickNavigation.doOnGlobalLayout {
            rvQuickNavigation.scrollToAroundPosition(startPage, ADDITIONAL_STEPS)
        }
    }

    private fun updatePageInfo(page: Int) {
        presenter.updatePageIndicator(page)
        navigationAdapter.updateSelectedIndex(page)
    }

    private fun setUpUI(rootView: View) {
        clDownloadedPopup = rootView.findViewById(R.id.clDownloadedPopup)
        llReaderTop = rootView.findViewById(R.id.llReaderTop)
        ibBack = rootView.findViewById(R.id.ibBack)
        ibDownload = rootView.findViewById(R.id.ibDownload)
        ibDownloadPopupClose = rootView.findViewById(R.id.ibDownloadPopupClose)
        ibRefresh = rootView.findViewById(R.id.ibRefresh)
        ibShare = rootView.findViewById(R.id.ibShare)
        layoutReaderBottom = rootView.findViewById(R.id.layoutReaderBottom)
        mtvBookTitle = rootView.findViewById(R.id.mtvBookTitle)
        mtvCurrentPage = rootView.findViewById(R.id.mtvCurrentPage)
        mtvDownloadTitle = rootView.findViewById(R.id.mtvDownloadTitle)
        rvQuickNavigation = rootView.findViewById(R.id.rvQuickNavigation)
        rvBookPages = rootView.findViewById(R.id.book_pages)
        viewModeButton = rootView.findViewById(R.id.ib_view_mode)
        noNetworkLabel = rootView.findViewById(R.id.no_network_label)

        ibBack.setOnClickListener(this)
        mtvCurrentPage.setOnClickListener(this)
        ibDownload.setOnClickListener(this)
        ibDownloadPopupClose.setOnClickListener(this)
        ibShare.setOnClickListener(this)
        ibRefresh.setOnClickListener(this)
        viewModeButton.setOnClickListener(this)
    }

    private fun requestStoragePermission() {
        val storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(storagePermission, REQUEST_STORAGE_PERMISSION)
    }

    private fun onPageChanged(position: Int) {
        rvQuickNavigation.scrollToAroundPosition(position, ADDITIONAL_STEPS)
        updatePageInfo(position)
    }

    companion object {
        private const val TAG = "ReaderFragment"
        private const val REQUEST_STORAGE_PERMISSION = 2364
        private const val PRELOAD_SIZE = 5
        private const val ADDITIONAL_STEPS = 2
    }
}
