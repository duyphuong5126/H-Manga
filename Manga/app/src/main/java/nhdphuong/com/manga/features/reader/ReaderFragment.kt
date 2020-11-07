package nhdphuong.com.manga.features.reader

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_reader.clDownloadedPopup
import kotlinx.android.synthetic.main.fragment_reader.clReaderTop
import kotlinx.android.synthetic.main.fragment_reader.ibBack
import kotlinx.android.synthetic.main.fragment_reader.ibDownload
import kotlinx.android.synthetic.main.fragment_reader.ibDownloadPopupClose
import kotlinx.android.synthetic.main.fragment_reader.ibRefresh
import kotlinx.android.synthetic.main.fragment_reader.ibShare
import kotlinx.android.synthetic.main.fragment_reader.layoutReaderBottom
import kotlinx.android.synthetic.main.fragment_reader.mtvBookTitle
import kotlinx.android.synthetic.main.fragment_reader.mtvCurrentPage
import kotlinx.android.synthetic.main.fragment_reader.mtvDownloadTitle
import kotlinx.android.synthetic.main.fragment_reader.rvQuickNavigation
import kotlinx.android.synthetic.main.fragment_reader.vpPages
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.AnimationHelper
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.views.DialogHelper
import nhdphuong.com.manga.views.adapters.BookReaderAdapter
import nhdphuong.com.manga.views.adapters.ReaderNavigationAdapter
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.gone

/*
 * Created by nhdphuong on 5/5/18.
 */
class ReaderFragment : Fragment(), ReaderContract.View, View.OnClickListener {

    private lateinit var presenter: ReaderContract.Presenter
    private lateinit var rotationAnimation: Animation
    private lateinit var bookReaderAdapter: BookReaderAdapter
    private lateinit var navigationAdapter: ReaderNavigationAdapter

    private var viewDownloadedData = false

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDownloadedData = arguments?.getBoolean(Constants.VIEW_DOWNLOADED_DATA) ?: false
        if (viewDownloadedData) {
            presenter.enableViewDownloadedDataMode()
        }
        ibBack.setOnClickListener(this)
        mtvCurrentPage.setOnClickListener(this)
        ibDownload.setOnClickListener(this)
        ibDownloadPopupClose.setOnClickListener(this)
        ibShare.setOnClickListener(this)

        ibDownload.becomeVisibleIf(!viewDownloadedData)
        ibShare.becomeVisibleIf(!viewDownloadedData)
        context?.let { context ->
            rotationAnimation = AnimationHelper.getRotationAnimation(context)
            ibRefresh.setOnClickListener(this)
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
                ibRefresh?.startAnimation(rotationAnimation)
                presenter.reloadCurrentPage { currentPage: Int ->
                    bookReaderAdapter.resetPage(currentPage)
                    ibRefresh?.postDelayed({
                        ibRefresh?.clearAnimation()
                    }, 3000)
                }
            }
        }
    }

    override fun showBookTitle(bookTitle: String) {
        mtvBookTitle.let { mtvBookTitle ->
            mtvBookTitle.text = bookTitle
            AnimationHelper.startTextRunning(mtvBookTitle)
        }
    }

    override fun showBookPages(pageList: List<String>) {
        activity?.let { activity ->
            navigationAdapter = ReaderNavigationAdapter(
                pageList,
                object : ReaderNavigationAdapter.ThumbnailSelectListener {
                    override fun onItemSelected(position: Int) {
                        vpPages?.currentItem = position
                    }
                })
            rvQuickNavigation?.adapter = navigationAdapter
            rvQuickNavigation?.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            rvQuickNavigation?.addItemDecoration(
                SpaceItemDecoration(
                    activity,
                    R.dimen.space_medium,
                    showFirstDivider = false,
                    showLastDivider = true
                )
            )

            bookReaderAdapter = BookReaderAdapter(pageList, View.OnClickListener {
                if (layoutReaderBottom?.visibility == View.VISIBLE) {
                    AnimationHelper.startSlideOutTop(activity, clReaderTop) {
                        clReaderTop.visibility = View.GONE
                    }
                    AnimationHelper.startSlideOutBottom(activity, layoutReaderBottom) {
                        layoutReaderBottom.visibility = View.GONE
                    }
                } else {
                    AnimationHelper.startSlideInTop(activity, clReaderTop) {
                        clReaderTop.visibility = View.VISIBLE
                    }
                    AnimationHelper.startSlideInBottom(activity, layoutReaderBottom) {
                        layoutReaderBottom.visibility = View.VISIBLE
                    }
                }
            })
            vpPages?.adapter = bookReaderAdapter
            vpPages?.offscreenPageLimit = OFFSCREEN_LIMIT
            vpPages?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {

                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {
                    presenter.updatePageIndicator(position)
                    if (position - 1 >= 0) {
                        bookReaderAdapter.resetPageToNormal(position - 1)
                    }
                    if (position + 1 < bookReaderAdapter.count) {
                        bookReaderAdapter.resetPageToNormal(position + 1)
                    }
                    rvQuickNavigation?.scrollToPosition(position)
                    navigationAdapter.updateSelectedIndex(position)
                }
            })
        }
    }

    override fun showPageIndicator(currentPage: Int, total: Int) {
        mtvCurrentPage.text = String.format(getString(R.string.bottom_reader), currentPage, total)
    }

    override fun showBackToGallery() {
        mtvCurrentPage.text = getString(R.string.back_to_gallery)
    }

    override fun jumpToPage(pageNumber: Int) {
        vpPages.setCurrentItem(pageNumber, true)
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
        activity?.let { activity ->
            DialogHelper.showStoragePermissionDialog(activity, onOk = {
                requestStoragePermission()
            }, onDismiss = {
                Toast.makeText(
                    activity,
                    getString(R.string.toast_storage_permission_require),
                    Toast.LENGTH_SHORT
                ).show()
            })
        }
    }

    override fun showDownloadPopup() {
        clDownloadedPopup.visibility = View.VISIBLE
    }

    override fun hideDownloadPopup() {
        clDownloadedPopup?.postDelayed({
            clDownloadedPopup?.gone()
        }, 3000)
    }

    override fun updateDownloadPopupTitle(downloadPage: Int) {
        mtvDownloadTitle.text = getString(R.string.download_progress, downloadPage.toString())
    }

    override fun pushNowReadingNotification(readingTitle: String, page: Int, total: Int) {
        NotificationHelper.sendBigContentNotification(
            getString(R.string.now_reading),
            NotificationCompat.PRIORITY_DEFAULT,
            readingTitle,
            true
        ).let { notificationId ->
            presenter.updateNotificationId(notificationId)
        }
    }

    override fun removeNotification(notificationId: Int) {
        NotificationHelper.cancelNotification(notificationId)
    }

    override fun processSharingCurrentPage(bookId: String, bookTitle: String, url: String) {
        val sharingTitle = getString(R.string.page_sharing_title, bookId, bookTitle)
        val shareIntent: Intent = Intent(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_SUBJECT, sharingTitle)
            .putExtra(Intent.EXTRA_TEXT, url)
            .setType("text/plain")

        context?.startActivity(
            Intent.createChooser(
                shareIntent,
                getString(R.string.page_sharing_chooser_title)
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

    private fun requestStoragePermission() {
        val storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(storagePermission, REQUEST_STORAGE_PERMISSION)
    }

    companion object {
        private const val TAG = "ReaderFragment"
        private const val REQUEST_STORAGE_PERMISSION = 2364
        private const val OFFSCREEN_LIMIT = 5
    }
}
