package nhdphuong.com.manga.features.reader

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_reader.clDownloadedPopup
import kotlinx.android.synthetic.main.fragment_reader.clReaderBottom
import kotlinx.android.synthetic.main.fragment_reader.clReaderTop
import kotlinx.android.synthetic.main.fragment_reader.ibBack
import kotlinx.android.synthetic.main.fragment_reader.ibDownload
import kotlinx.android.synthetic.main.fragment_reader.ibDownloadPopupClose
import kotlinx.android.synthetic.main.fragment_reader.ibRefresh
import kotlinx.android.synthetic.main.fragment_reader.mtvBookTitle
import kotlinx.android.synthetic.main.fragment_reader.mtvCurrentPage
import kotlinx.android.synthetic.main.fragment_reader.mtvDownloadTitle
import kotlinx.android.synthetic.main.fragment_reader.vpPages
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.AnimationHelper
import nhdphuong.com.manga.views.DialogHelper
import nhdphuong.com.manga.views.adapters.BookReaderAdapter
import nhdphuong.com.manga.views.becomeVisibleIf

/*
 * Created by nhdphuong on 5/5/18.
 */
class ReaderFragment : Fragment(), ReaderContract.View {
    companion object {
        private const val TAG = "ReaderFragment"
        private const val REQUEST_STORAGE_PERMISSION = 2364
    }

    private lateinit var presenter: ReaderContract.Presenter
    private lateinit var rotationAnimation: Animation
    private lateinit var bookReaderAdapter: BookReaderAdapter

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
        ibBack.setOnClickListener {
            navigateToGallery()
        }

        ibDownload.setOnClickListener {

        }

        mtvCurrentPage.setOnClickListener {
            presenter.backToGallery()
        }

        ibDownload.becomeVisibleIf(!viewDownloadedData)
        ibDownload.setOnClickListener {
            presenter.downloadCurrentPage()
        }

        ibDownloadPopupClose.setOnClickListener {
            hideDownloadPopup()
        }
        rotationAnimation = AnimationHelper.getRotationAnimation(context!!)
        ibRefresh.let { ibRefresh ->
            ibRefresh.setOnClickListener {
                ibRefresh.startAnimation(rotationAnimation)
                presenter.reloadCurrentPage { currentPage: Int ->
                    bookReaderAdapter.resetPage(currentPage)
                    val handler = Handler()
                    handler.postDelayed({
                        ibRefresh.clearAnimation()
                    }, 3000)
                }
            }
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

    override fun showBookTitle(bookTitle: String) {
        mtvBookTitle.let { mtvBookTitle ->
            mtvBookTitle.text = bookTitle
            AnimationHelper.startTextRunning(mtvBookTitle)
        }
    }

    override fun showBookPages(pageList: List<String>) {
        val activity = activity!!
        bookReaderAdapter = BookReaderAdapter(context!!, pageList, View.OnClickListener {
            if (clReaderBottom.visibility == View.VISIBLE) {
                AnimationHelper.startSlideOutTop(activity, clReaderTop) {
                    clReaderTop.visibility = View.GONE
                }
                AnimationHelper.startSlideOutBottom(activity, clReaderBottom) {
                    clReaderBottom.visibility = View.GONE
                }
            } else {
                AnimationHelper.startSlideInTop(activity, clReaderTop) {
                    clReaderTop.visibility = View.VISIBLE
                }
                AnimationHelper.startSlideInBottom(activity, clReaderBottom) {
                    clReaderBottom.visibility = View.VISIBLE
                }
            }
        })
        vpPages.adapter = bookReaderAdapter
        vpPages.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
            }
        })
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

    override fun navigateToGallery() {
        presenter.endReading()
        activity?.onBackPressed()
    }

    override fun showRequestStoragePermission() {
        DialogHelper.showStoragePermissionDialog(activity!!, onOk = {
            requestStoragePermission()
        }, onDismiss = {
            Toast.makeText(
                context,
                getString(R.string.toast_storage_permission_require),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    override fun showDownloadPopup() {
        clDownloadedPopup.visibility = View.VISIBLE
    }

    override fun hideDownloadPopup() {
        clDownloadedPopup.visibility = View.GONE
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
}
