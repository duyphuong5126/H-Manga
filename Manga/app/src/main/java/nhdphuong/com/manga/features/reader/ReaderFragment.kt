package nhdphuong.com.manga.features.reader

import android.annotation.TargetApi
import android.app.Dialog
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R
import nhdphuong.com.manga.databinding.FragmentReaderBinding
import nhdphuong.com.manga.supports.AnimationHelper
import nhdphuong.com.manga.views.DialogHelper
import nhdphuong.com.manga.views.adapters.BookReaderAdapter

/*
 * Created by nhdphuong on 5/5/18.
 */
class ReaderFragment : Fragment(), ReaderContract.View {
    companion object {
        private const val TAG = "ReaderFragment"
        private const val REQUEST_STORAGE_PERMISSION = 2364
    }

    private lateinit var mPresenter: ReaderContract.Presenter
    private lateinit var mBinding: FragmentReaderBinding
    private lateinit var mRotationAnimation: Animation
    private lateinit var mBookReaderAdapter: BookReaderAdapter
    private lateinit var mLoadingDialog: Dialog

    override fun setPresenter(presenter: ReaderContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_reader, container, false)
        return mBinding.root
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.window.statusBarColor = ContextCompat.getColor(context, R.color.grey_1)
        mLoadingDialog = DialogHelper.showLoadingDialog(activity)
        mBinding.ibBack.setOnClickListener {
            navigateToGallery()
        }

        mBinding.ibDownload.setOnClickListener {

        }

        mBinding.mtvCurrentPage.setOnClickListener {
            mPresenter.backToGallery()
        }

        mBinding.ibDownload.setOnClickListener {
            mPresenter.downloadCurrentPage()
        }

        mBinding.ibDownloadPopupClose.setOnClickListener {
            hideDownloadPopup()
        }
        mRotationAnimation = AnimationHelper.getRotationAnimation(context)
        mBinding.ibRefresh.let { ibRefresh ->
            ibRefresh.setOnClickListener {
                ibRefresh.startAnimation(mRotationAnimation)
                mPresenter.reloadCurrentPage { currentPage: Int ->
                    mBookReaderAdapter.resetPage(currentPage)
                    val handler = Handler()
                    handler.postDelayed({
                        ibRefresh.clearAnimation()
                    }, 3000)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStop() {
        super.onStop()
        activity.window.statusBarColor = ContextCompat.getColor(context, R.color.colorPrimary)
        mPresenter.stop()
    }

    override fun showBookTitle(bookTitle: String) {
        mBinding.mtvBookTitle.let { mtvBookTitle ->
            mtvBookTitle.text = bookTitle
            AnimationHelper.startTextRunning(mtvBookTitle)
        }
    }

    override fun showBookPages(pageList: List<String>) {
        mBookReaderAdapter = BookReaderAdapter(context, pageList, View.OnClickListener {
            if (mBinding.clReaderBottom.visibility == View.VISIBLE) {
                AnimationHelper.startSlideOutTop(activity, mBinding.clReaderTop) {
                    mBinding.clReaderTop.visibility = View.GONE
                }
                AnimationHelper.startSlideOutBottom(activity, mBinding.clReaderBottom) {
                    mBinding.clReaderBottom.visibility = View.GONE
                }
            } else {
                AnimationHelper.startSlideInTop(activity, mBinding.clReaderTop) {
                    mBinding.clReaderTop.visibility = View.VISIBLE
                }
                AnimationHelper.startSlideInBottom(activity, mBinding.clReaderBottom) {
                    mBinding.clReaderBottom.visibility = View.VISIBLE
                }
            }
        })
        mBinding.vpPages.adapter = mBookReaderAdapter
        mBinding.vpPages.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                mPresenter.updatePageIndicator(position)
                if (position - 1 >= 0) {
                    mBookReaderAdapter.resetPageToNormal(position - 1)
                }
                if (position + 1 < mBookReaderAdapter.count) {
                    mBookReaderAdapter.resetPageToNormal(position + 1)
                }
            }
        })
    }

    override fun showPageIndicator(pageString: String) {
        mBinding.mtvCurrentPage.text = pageString
    }

    override fun jumpToPage(pageNumber: Int) {
        mBinding.vpPages.setCurrentItem(pageNumber, true)
    }

    override fun navigateToGallery() {
        activity.onBackPressed()
    }

    override fun showRequestStoragePermission() {
        DialogHelper.showStoragePermissionDialog(activity, onOk = {
            requestStoragePermission()
        }, onDismiss = {
            Toast.makeText(context, getString(R.string.toast_storage_permission_require), Toast.LENGTH_SHORT).show()
        })
    }

    override fun showDownloadPopup() {
        mBinding.clDownloadedPopup.visibility = View.VISIBLE
    }

    override fun hideDownloadPopup() {
        mBinding.clDownloadedPopup.visibility = View.GONE
    }

    override fun updateDownloadPopupTitle(downloadTitle: String) {
        mBinding.mtvDownloadTitle.text = downloadTitle
    }

    override fun showLoading() {
        mLoadingDialog.show()
        mBinding.ibRefresh.startAnimation(mRotationAnimation)
    }

    override fun hideLoading() {
        mLoadingDialog.hide()
        mBinding.ibRefresh.clearAnimation()
    }

    override fun isActive(): Boolean = isAdded

    private fun requestStoragePermission() {
        val storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(storagePermission, REQUEST_STORAGE_PERMISSION)
    }
}