package nhdphuong.com.manga.features.preview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.databinding.FragmentBookPreviewBinding
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.views.DialogHelper
import nhdphuong.com.manga.views.InfoCardLayout
import nhdphuong.com.manga.views.MyGridLayoutManager
import nhdphuong.com.manga.views.adapters.BookAdapter
import nhdphuong.com.manga.views.adapters.PreviewAdapter

/*
 * Created by nhdphuong on 4/14/18.
 */
class BookPreviewFragment : Fragment(), BookPreviewContract.View, InfoCardLayout.TagSelectedListener {
    companion object {
        private const val TAG = "BookPreviewFragment"
        private const val NUM_OF_ROWS = 2
        private const val REQUEST_STORAGE_PERMISSION = 3142
    }

    private lateinit var mPresenter: BookPreviewContract.Presenter
    private lateinit var mBinding: FragmentBookPreviewBinding
    private lateinit var mPreviewAdapter: PreviewAdapter
    private lateinit var mRecommendBookAdapter: BookAdapter
    private lateinit var mAnimatorSet: AnimatorSet
    private var isDownloadRequested = false

    @Volatile
    private var isPresenterStarted: Boolean = false

    private lateinit var mPreviewLayoutManager: MyGridLayoutManager

    override fun setPresenter(presenter: BookPreviewContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView")
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_book_preview, container, false)
        return mBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        mBinding.svBookCover.let { svBookCover ->
            val scrollDownAnimator = ObjectAnimator.ofInt(svBookCover, "scrollY", 1000)
            scrollDownAnimator.startDelay = 100
            scrollDownAnimator.duration = 6500
            val scrollUpAnimator = ObjectAnimator.ofInt(svBookCover, "scrollY", -1000)
            scrollUpAnimator.startDelay = 100
            scrollUpAnimator.duration = 6500
            scrollDownAnimator.addListener(getAnimationListener(scrollUpAnimator))
            scrollUpAnimator.addListener(getAnimationListener(scrollDownAnimator))

            mAnimatorSet = AnimatorSet()
            mAnimatorSet.playTogether(scrollDownAnimator)
            svBookCover.setOnTouchListener { _, _ ->
                true
            }
        }

        mBinding.mtvDownload.setOnClickListener {
            isDownloadRequested = true
            mPresenter.downloadBook()
        }

        val changeFavoriteListener = View.OnClickListener { mPresenter.changeBookFavorite() }
        mBinding.mtvFavorite.setOnClickListener(changeFavoriteListener)
        mBinding.mtvNotFavorite.setOnClickListener(changeFavoriteListener)

        // Gingerbread
        mBinding.hsvPreviewThumbNail.overScrollMode = View.OVER_SCROLL_NEVER
        mBinding.hsvRecommendList.overScrollMode = View.OVER_SCROLL_NEVER
        mBinding.svPreview.overScrollMode = View.OVER_SCROLL_NEVER
        mBinding.svBookCover.overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
        mPresenter.start()
    }

    override fun onResume() {
        super.onResume()
        mBinding.root.viewTreeObserver.addOnGlobalLayoutListener {
            if (!isPresenterStarted) {
                isPresenterStarted = true
                mPresenter.loadInfoLists()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            val permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                showRequestStoragePermission()
            } else {
                if (isDownloadRequested) {
                    mPresenter.downloadBook()
                }
            }
            val result = if (permissionGranted) "granted" else "denied"
            Logger.d(TAG, "Storage permission is $result")
        }
    }

    override fun onStop() {
        super.onStop()
        mPresenter.stop()
        isPresenterStarted = false
    }

    override fun showBookCoverImage(coverUrl: String) {
        if (!NHentaiApp.instance.isCensored) {
            ImageUtils.loadImage(coverUrl, R.drawable.ic_404_not_found, mBinding.ivBookCover, onLoadSuccess = {
                mPresenter.saveCurrentAvailableCoverUrl(coverUrl)
                mAnimatorSet.start()
            }, onLoadFailed = {
                mPresenter.reloadCoverImage()
            })
        } else {
            mBinding.ivBookCover.setImageResource(R.drawable.ic_nothing_here_grey)
            mPresenter.saveCurrentAvailableCoverUrl(coverUrl)
            mAnimatorSet.start()
        }
    }

    override fun show1stTitle(firstTitle: String) {
        if (!TextUtils.isEmpty(firstTitle)) {
            mBinding.tvTitle1.visibility = View.VISIBLE
            mBinding.tvTitle1.text = firstTitle
        } else {
            mBinding.tvTitle1.visibility = View.GONE
        }
    }

    override fun show2ndTitle(secondTitle: String) {
        if (!TextUtils.isEmpty(secondTitle)) {
            mBinding.tvTitle2.visibility = View.VISIBLE
            mBinding.tvTitle2.text = secondTitle
        } else {
            mBinding.tvTitle2.visibility = View.GONE
        }
    }

    override fun showTagList(tagList: List<Tag>) {
        mBinding.tvTagsLabel.visibility = View.VISIBLE
        mBinding.clTags.visibility = View.VISIBLE
        loadInfoList(mBinding.clTags, tagList)
    }

    override fun showArtistList(artistList: List<Tag>) {
        mBinding.tvArtistsLabel.visibility = View.VISIBLE
        mBinding.clArtists.visibility = View.VISIBLE
        loadInfoList(mBinding.clArtists, artistList)
    }

    override fun showLanguageList(languageList: List<Tag>) {
        mBinding.tvLanguagesLabel.visibility = View.VISIBLE
        mBinding.clLanguages.visibility = View.VISIBLE
        loadInfoList(mBinding.clLanguages, languageList)
    }

    override fun showCategoryList(categoryList: List<Tag>) {
        mBinding.tvCategoriesLabel.visibility = View.VISIBLE
        mBinding.clCategories.visibility = View.VISIBLE
        loadInfoList(mBinding.clCategories, categoryList)
    }

    override fun showCharacterList(characterList: List<Tag>) {
        mBinding.tvCharactersLabel.visibility = View.VISIBLE
        mBinding.clCharacters.visibility = View.VISIBLE
        loadInfoList(mBinding.clCharacters, characterList)
    }

    override fun showGroupList(groupList: List<Tag>) {
        mBinding.tvGroupsLabel.visibility = View.VISIBLE
        mBinding.clGroups.visibility = View.VISIBLE
        loadInfoList(mBinding.clGroups, groupList)
    }

    override fun showParodyList(parodyList: List<Tag>) {
        mBinding.tvParodiesLabel.visibility = View.VISIBLE
        mBinding.clParodies.visibility = View.VISIBLE
        loadInfoList(mBinding.clParodies, parodyList)
    }

    override fun hideTagList() {
        mBinding.tvTagsLabel.visibility = View.GONE
        mBinding.clTags.visibility = View.GONE
    }

    override fun hideArtistList() {
        mBinding.tvArtistsLabel.visibility = View.GONE
        mBinding.clArtists.visibility = View.GONE
    }

    override fun hideLanguageList() {
        mBinding.tvLanguagesLabel.visibility = View.GONE
        mBinding.clLanguages.visibility = View.GONE
    }

    override fun hideCategoryList() {
        mBinding.tvCategoriesLabel.visibility = View.GONE
        mBinding.clCategories.visibility = View.GONE
    }

    override fun hideCharacterList() {
        mBinding.tvCharactersLabel.visibility = View.GONE
        mBinding.clCharacters.visibility = View.GONE
    }

    override fun hideGroupList() {
        mBinding.tvGroupsLabel.visibility = View.GONE
        mBinding.clGroups.visibility = View.GONE
    }

    override fun hideParodyList() {
        mBinding.tvParodiesLabel.visibility = View.GONE
        mBinding.clParodies.visibility = View.GONE
    }

    override fun showPageCount(pageCount: String) {
        mBinding.tvPageCount.text = pageCount
    }

    override fun showUploadedTime(uploadedTime: String) {
        mBinding.tvUpdatedAt.text = uploadedTime
    }

    override fun showBookThumbnailList(thumbnailList: List<String>) {
        var spanCount = thumbnailList.size / NUM_OF_ROWS
        if (thumbnailList.size % NUM_OF_ROWS != 0) {
            spanCount++
        }

        Logger.d(TAG, "thumbnails: ${thumbnailList.size}, number of rows: $NUM_OF_ROWS, spanCount: $spanCount")
        mPreviewLayoutManager = object : MyGridLayoutManager(context!!, spanCount) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        mBinding.rvPreviewList.run {
            layoutManager = mPreviewLayoutManager
            mPreviewAdapter = PreviewAdapter(NUM_OF_ROWS, thumbnailList, object : PreviewAdapter.ThumbnailClickCallback {
                override fun onThumbnailClicked(page: Int) {
                    mPresenter.startReadingFrom(page)
                }
            })
            adapter = mPreviewAdapter
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBinding.hsvPreviewThumbNail.setOnScrollChangeListener { _, _, _, _, _ ->
                if (!mBinding.hsvPreviewThumbNail.canScrollHorizontally(1)) {
                    Logger.d(TAG, "End of list, load more thumbnails")
                    mPresenter.loadMoreThumbnails()
                }
            }
        }
    }

    override fun updateBookThumbnailList() {
        var spanCount = mPreviewAdapter.itemCount / NUM_OF_ROWS
        if (mPreviewAdapter.itemCount % NUM_OF_ROWS != 0) {
            spanCount++
        }
        mPreviewLayoutManager.spanCount = spanCount
        mPreviewAdapter.notifyDataSetChanged()
    }

    override fun showRecommendBook(bookList: List<Book>) {
        Logger.d(TAG, "recommended books, spanCount: ${bookList.size}")
        mBinding.mtvRecommendBook.visibility = View.VISIBLE
        val gridLayoutManager = object : MyGridLayoutManager(context!!, bookList.size) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }

        mBinding.rvRecommendList.layoutManager = gridLayoutManager
        mRecommendBookAdapter = BookAdapter(bookList, BookAdapter.RECOMMEND_BOOK, object : BookAdapter.OnBookClick {
            override fun onItemClick(item: Book) {
                BookPreviewActivity.restart(item)
            }
        })
        mBinding.rvRecommendList.adapter = mRecommendBookAdapter
    }

    override fun showNoRecommendBook() {
        mBinding.mtvRecommendBook.visibility = View.GONE
    }

    override fun showRequestStoragePermission() {
        DialogHelper.showStoragePermissionDialog(activity!!, onOk = {
            requestStoragePermission()
        }, onDismiss = {
            Toast.makeText(context, getString(R.string.toast_storage_permission_require), Toast.LENGTH_SHORT).show()
            isDownloadRequested = false
        })
    }

    override fun initDownloading(total: Int) {
        mBinding.clDownloadProgress.visibility = View.VISIBLE
        mBinding.pbDownloading.max = total
        mBinding.mtvDownloaded.text = String.format(getString(R.string.preview_download_progress), 0, total)
    }

    override fun updateDownloadProgress(progress: Int, total: Int) {
        mBinding.clDownloadProgress.visibility = View.VISIBLE
        mBinding.pbDownloading.max = total
        mBinding.pbDownloading.progressDrawable = getProgressDrawableId(progress, total)
        mBinding.pbDownloading.progress = progress
        mBinding.mtvDownloaded.text = String.format(getString(R.string.preview_download_progress), progress, total)
    }

    override fun finishDownloading() {
        mBinding.mtvDownloaded.text = getString(R.string.done)
        val handler = Handler()
        handler.postDelayed({
            mBinding.pbDownloading.progressDrawable = getProgressDrawableId(0, mBinding.pbDownloading.max)
            mBinding.pbDownloading.max = 0
            mBinding.clDownloadProgress.visibility = View.GONE
            mBinding.mtvDownloaded.text = getString(R.string.preview_download_progress)
        }, 2000)
    }

    override fun finishDownloading(downloadFailedCount: Int, total: Int) {
        mBinding.mtvDownloaded.text = String.format(getString(R.string.fail_to_download), downloadFailedCount)
        val handler = Handler()
        handler.postDelayed({
            mBinding.pbDownloading.progressDrawable = getProgressDrawableId(0, mBinding.pbDownloading.max)
            mBinding.pbDownloading.max = 0
            mBinding.clDownloadProgress.visibility = View.GONE
            mBinding.mtvDownloaded.text = getString(R.string.preview_download_progress)
        }, 2000)
    }

    override fun showBookBeingDownloaded(bookId: String) {
        DialogHelper.showBookDownloadingDialog(activity!!, bookId, onOk = {
            mPresenter.restartBookPreview(bookId)
        }, onDismiss = {

        })
    }

    override fun showThisBookBeingDownloaded() {
        DialogHelper.showThisBookDownloadingDialog(activity!!, onOk = {

        })
    }

    override fun showFavoriteBookSaved(isFavorite: Boolean) {
        if (isFavorite) {
            mBinding.mtvNotFavorite.visibility = View.INVISIBLE
            mBinding.mtvFavorite.visibility = View.VISIBLE
        } else {
            mBinding.mtvNotFavorite.visibility = View.VISIBLE
            mBinding.mtvFavorite.visibility = View.INVISIBLE
        }
    }

    override fun showFavoriteBooks(favoriteList: List<Int>) {
        mRecommendBookAdapter.setFavoriteList(favoriteList)
    }

    override fun showRecentBooks(recentList: List<Int>) {
        mRecommendBookAdapter.setRecentList(recentList)
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun isActive() = isAdded

    override fun onTagSelected(tag: Tag) {
        val intent = Intent(Constants.TAG_SELECTED_ACTION)
        intent.putExtra(Constants.SELECTED_TAG, tag.name)
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
        activity?.onBackPressed()
    }

    private fun loadInfoList(layout: ViewGroup, infoList: List<Tag>) {
        val infoCardLayout = InfoCardLayout(activity?.layoutInflater!!, infoList, context!!)
        infoCardLayout.loadInfoList(layout)
        infoCardLayout.setTagSelectedListener(this)
    }

    private fun getAnimationListener(callOnEndingObject: ObjectAnimator) = object : Animator.AnimatorListener {
        override fun onAnimationEnd(p0: Animator?) {
            callOnEndingObject.start()
        }

        override fun onAnimationCancel(p0: Animator?) {

        }

        override fun onAnimationRepeat(p0: Animator?) {

        }

        override fun onAnimationStart(p0: Animator?) {

        }
    }

    private fun requestStoragePermission() {
        val storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(storagePermission, REQUEST_STORAGE_PERMISSION)
    }

    private fun getProgressDrawableId(progress: Int, max: Int): Drawable {
        val percentage = (progress * 1f) / (max * 1f)
        return ActivityCompat.getDrawable(context!!, when {
            percentage >= Constants.DOWNLOAD_GREEN_LEVEL -> R.drawable.bg_download_green
            percentage >= Constants.DOWNLOAD_YELLOW_LEVEL -> R.drawable.bg_download_yellow
            else -> R.drawable.bg_download_red
        })!!
    }
}