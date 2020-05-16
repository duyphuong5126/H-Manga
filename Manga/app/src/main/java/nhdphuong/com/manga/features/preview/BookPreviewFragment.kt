package nhdphuong.com.manga.features.preview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_book_preview.layoutArtists
import kotlinx.android.synthetic.main.fragment_book_preview.layoutCategories
import kotlinx.android.synthetic.main.fragment_book_preview.layoutCharacters
import kotlinx.android.synthetic.main.fragment_book_preview.clDownloadProgress
import kotlinx.android.synthetic.main.fragment_book_preview.layoutGroups
import kotlinx.android.synthetic.main.fragment_book_preview.layoutLanguages
import kotlinx.android.synthetic.main.fragment_book_preview.layoutParodies
import kotlinx.android.synthetic.main.fragment_book_preview.layoutTags
import kotlinx.android.synthetic.main.fragment_book_preview.hsvPreviewThumbNail
import kotlinx.android.synthetic.main.fragment_book_preview.hsvRecommendList
import kotlinx.android.synthetic.main.fragment_book_preview.ivBookCover
import kotlinx.android.synthetic.main.fragment_book_preview.mtvDownload
import kotlinx.android.synthetic.main.fragment_book_preview.mtvDownloaded
import kotlinx.android.synthetic.main.fragment_book_preview.mtvFavorite
import kotlinx.android.synthetic.main.fragment_book_preview.mtvNotFavorite
import kotlinx.android.synthetic.main.fragment_book_preview.mtvRecommendBook
import kotlinx.android.synthetic.main.fragment_book_preview.pbDownloading
import kotlinx.android.synthetic.main.fragment_book_preview.rvPreviewList
import kotlinx.android.synthetic.main.fragment_book_preview.rvRecommendList
import kotlinx.android.synthetic.main.fragment_book_preview.svBookCover
import kotlinx.android.synthetic.main.fragment_book_preview.svPreview
import kotlinx.android.synthetic.main.fragment_book_preview.tvArtistsLabel
import kotlinx.android.synthetic.main.fragment_book_preview.tvCategoriesLabel
import kotlinx.android.synthetic.main.fragment_book_preview.tvCharactersLabel
import kotlinx.android.synthetic.main.fragment_book_preview.tvGroupsLabel
import kotlinx.android.synthetic.main.fragment_book_preview.tvLanguagesLabel
import kotlinx.android.synthetic.main.fragment_book_preview.tvPageCount
import kotlinx.android.synthetic.main.fragment_book_preview.tvParodiesLabel
import kotlinx.android.synthetic.main.fragment_book_preview.tvTagsLabel
import kotlinx.android.synthetic.main.fragment_book_preview.tvTitle_1
import kotlinx.android.synthetic.main.fragment_book_preview.tvTitle_2
import kotlinx.android.synthetic.main.fragment_book_preview.tvUpdatedAt
import kotlinx.android.synthetic.main.fragment_book_preview.ibBack
import kotlinx.android.synthetic.main.fragment_book_preview.buttonClearDownloadedData
import kotlinx.android.synthetic.main.fragment_book_preview.buttonUnSeen
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.DOWNLOADING_FAILED_COUNT
import nhdphuong.com.manga.Constants.Companion.PROGRESS
import nhdphuong.com.manga.Constants.Companion.TOTAL
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_STARTED
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_PROGRESS
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_COMPLETED
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_FAILED
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_STARTED
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_PROGRESS
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_COMPLETED
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_FAILED
import nhdphuong.com.manga.Constants.Companion.ACTION_DISMISS_GALLERY_REFRESHING_DIALOG
import nhdphuong.com.manga.Constants.Companion.ACTION_SHOW_GALLERY_REFRESHING_DIALOG
import nhdphuong.com.manga.Constants.Companion.DELETING_FAILED_COUNT
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.features.reader.ReaderActivity
import nhdphuong.com.manga.supports.AnimationHelper
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.views.InformationCardAdapter
import nhdphuong.com.manga.views.MyGridLayoutManager
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.becomeInvisible
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.DialogHelper
import nhdphuong.com.manga.views.adapters.BookAdapter
import nhdphuong.com.manga.views.adapters.PreviewAdapter

/*
 * Created by nhdphuong on 4/14/18.
 */
class BookPreviewFragment :
    Fragment(),
    BookPreviewContract.View,
    InformationCardAdapter.TagSelectedListener {
    companion object {
        private const val TAG = "BookPreviewFragment"
        private const val NUM_OF_ROWS = 2
        private const val REQUEST_STORAGE_PERMISSION = 3142
        private const val COVER_IMAGE_ANIMATION_START_DELAY = 100L
        private const val COVER_IMAGE_ANIMATION_DURATION = 6500L
        private const val COVER_IMAGE_ANIMATION_UP_OFFSET = -1000
        private const val COVER_IMAGE_ANIMATION_DOWN_OFFSET = 1000
        private const val DOWNLOADING_BAR_HIDING_DELAY = 2000L
        private const val DELETING_BAR_HIDING_DELAY = 3000L
        private const val CLOSE_AFTER_REMOVED_TIME = 4000L
        private const val SHOW_DOWNLOADING_COMPLETE_DIALOG_DELAY = 3000L
        private const val PREVIEW_CACHE_SIZE = 10
    }

    private lateinit var presenter: BookPreviewContract.Presenter
    private lateinit var previewAdapter: PreviewAdapter
    private lateinit var recommendBookAdapter: BookAdapter
    private lateinit var animatorSet: AnimatorSet
    private lateinit var refreshGalleryDialog: Dialog
    private var isDownloadingRequested = false
    private var isDeletingRequested = false

    @Volatile
    private var isPresenterStarted: Boolean = false

    private var viewDownloadedData = false

    private lateinit var previewLayoutManager: MyGridLayoutManager

    private val bookDownloadingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_DOWNLOADING_STARTED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    val totalBookPages = intent.extras?.getInt(TOTAL) ?: 0
                    presenter.initDownloading(bookId, totalBookPages)
                    isDownloadingRequested = false
                }
                ACTION_DOWNLOADING_PROGRESS -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    val totalBookPages = intent.extras?.getInt(TOTAL) ?: 0
                    val progress = intent.extras?.getInt(PROGRESS) ?: 0
                    presenter.updateDownloadingProgress(bookId, progress, totalBookPages)
                    isDownloadingRequested = false
                }
                ACTION_DOWNLOADING_COMPLETED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    presenter.finishDownloading(bookId)
                    isDownloadingRequested = false
                }
                ACTION_DOWNLOADING_FAILED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    val totalBookPages = intent.extras?.getInt(TOTAL) ?: 0
                    val downloadingFailedCount =
                        intent.extras?.getInt(DOWNLOADING_FAILED_COUNT) ?: 0
                    presenter.finishDownloading(bookId, downloadingFailedCount, totalBookPages)
                    isDownloadingRequested = false
                }
                ACTION_DELETING_STARTED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    presenter.initDeleting(bookId)
                    isDeletingRequested = false
                }
                ACTION_DELETING_PROGRESS -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    val totalBookPages = intent.extras?.getInt(TOTAL) ?: 0
                    val progress = intent.extras?.getInt(PROGRESS) ?: 0
                    presenter.updateDeletingProgress(bookId, progress, totalBookPages)
                }
                ACTION_DELETING_FAILED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    val deletingFailedCount = intent.extras?.getInt(DELETING_FAILED_COUNT) ?: 0
                    presenter.finishDeleting(bookId, deletingFailedCount)
                    isDeletingRequested = false
                }
                ACTION_DELETING_COMPLETED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    presenter.finishDeleting(bookId)
                    isDeletingRequested = false
                }
                ACTION_SHOW_GALLERY_REFRESHING_DIALOG -> {
                    refreshGalleryDialog.show()
                }
                ACTION_DISMISS_GALLERY_REFRESHING_DIALOG -> {
                    refreshGalleryDialog.dismiss()
                }
                else -> Unit
            }
        }
    }

    override fun setPresenter(presenter: BookPreviewContract.Presenter) {
        this.presenter = presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_book_preview, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        viewDownloadedData = arguments?.getBoolean(Constants.VIEW_DOWNLOADED_DATA) ?: false
        if (viewDownloadedData) {
            presenter.enableViewDownloadedDataMode()
        }
        svBookCover.let { svBookCover ->
            val scrollDownAnimator =
                ObjectAnimator.ofInt(svBookCover, "scrollY", COVER_IMAGE_ANIMATION_DOWN_OFFSET)
            scrollDownAnimator.startDelay = COVER_IMAGE_ANIMATION_START_DELAY
            scrollDownAnimator.duration = COVER_IMAGE_ANIMATION_DURATION
            val scrollUpAnimator =
                ObjectAnimator.ofInt(svBookCover, "scrollY", COVER_IMAGE_ANIMATION_UP_OFFSET)
            scrollUpAnimator.startDelay = COVER_IMAGE_ANIMATION_START_DELAY
            scrollUpAnimator.duration = COVER_IMAGE_ANIMATION_DURATION
            scrollDownAnimator.addListener(getAnimationListener(scrollUpAnimator))
            scrollUpAnimator.addListener(getAnimationListener(scrollDownAnimator))

            animatorSet = AnimatorSet()
            animatorSet.playTogether(scrollDownAnimator)
            svBookCover.setOnTouchListener { _, _ ->
                true
            }
        }

        mtvDownload.becomeVisibleIf(!viewDownloadedData)
        mtvDownload.setOnClickListener {
            isDownloadingRequested = true
            presenter.downloadBook()
        }

        buttonClearDownloadedData.becomeVisibleIf(viewDownloadedData)
        buttonClearDownloadedData.setOnClickListener {
            isDeletingRequested = true
            presenter.deleteBook()
        }

        val changeFavoriteListener = View.OnClickListener { presenter.changeBookFavorite() }
        mtvFavorite.setOnClickListener(changeFavoriteListener)
        mtvNotFavorite.setOnClickListener(changeFavoriteListener)

        hsvPreviewThumbNail.overScrollMode = View.OVER_SCROLL_NEVER
        hsvRecommendList.overScrollMode = View.OVER_SCROLL_NEVER
        hsvRecommendList.becomeVisibleIf(!viewDownloadedData)
        mtvRecommendBook.becomeVisibleIf(!viewDownloadedData)
        svPreview.overScrollMode = View.OVER_SCROLL_NEVER
        svBookCover.overScrollMode = View.OVER_SCROLL_NEVER

        ivBookCover.setOnClickListener {
            activity?.let { activity ->
                if (ibBack.visibility == View.VISIBLE) {
                    AnimationHelper.startSlideOutTop(activity, ibBack) {
                        ibBack.gone()
                    }
                } else {
                    AnimationHelper.startSlideInTop(activity, ibBack) {
                        ibBack.becomeVisible()
                    }
                }
            }
        }

        ibBack.setOnClickListener {
            activity?.onBackPressed()
        }

        buttonUnSeen.setOnClickListener {
            activity?.let {
                DialogHelper.showUnSeenBookConfirmationDialog(it, onOk = {
                    presenter.unSeenBook()
                }, onDismiss = {
                    Logger.d(TAG, "UnSeen canceled")
                })
            }
        }

        view.viewTreeObserver.addOnGlobalLayoutListener {
            if (!isPresenterStarted) {
                isPresenterStarted = true
                presenter.loadInfoLists()
            }
        }

        activity?.let { activity ->
            val loadingTitle = activity.getString(R.string.refreshing_gallery)
            refreshGalleryDialog = DialogHelper.createLoadingDialog(activity, loadingTitle)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
        presenter.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.READING_REQUEST) {
            presenter.refreshRecentStatus()
        }
    }

    override fun onStart() {
        super.onStart()
        BroadCastReceiverHelper.registerBroadcastReceiver(
            context,
            bookDownloadingReceiver,
            ACTION_DOWNLOADING_STARTED,
            ACTION_DELETING_STARTED,
            ACTION_DOWNLOADING_PROGRESS,
            ACTION_DELETING_PROGRESS,
            ACTION_DOWNLOADING_FAILED,
            ACTION_DELETING_FAILED,
            ACTION_DOWNLOADING_COMPLETED,
            ACTION_DELETING_COMPLETED,
            ACTION_SHOW_GALLERY_REFRESHING_DIALOG,
            ACTION_DISMISS_GALLERY_REFRESHING_DIALOG
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            val permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            when {
                !permissionGranted -> showRequestStoragePermission()
                isDownloadingRequested -> presenter.downloadBook()
                isDeletingRequested -> presenter.deleteBook()
            }
            val result = if (permissionGranted) "granted" else "denied"
            Logger.d(TAG, "Storage permission is $result")
        }
    }

    override fun onStop() {
        super.onStop()
        isPresenterStarted = false
        BroadCastReceiverHelper.unRegisterBroadcastReceiver(context, bookDownloadingReceiver)
    }

    override fun onDestroy() {
        presenter.stop()
        super.onDestroy()
    }

    override fun showBookCoverImage(coverUrl: String) {
        if (!NHentaiApp.instance.isCensored) {
            ImageUtils.loadImage(
                coverUrl,
                R.drawable.ic_404_not_found,
                ivBookCover,
                onLoadSuccess = {
                    presenter.saveCurrentAvailableCoverUrl(coverUrl)
                    animatorSet.start()
                },
                onLoadFailed = {
                    presenter.reloadCoverImage()
                })
        } else {
            ivBookCover.setImageResource(R.drawable.ic_nothing_here_grey)
            presenter.saveCurrentAvailableCoverUrl(coverUrl)
            animatorSet.start()
        }
    }

    override fun show1stTitle(firstTitle: String) {
        if (!TextUtils.isEmpty(firstTitle)) {
            tvTitle_1.becomeVisible()
            tvTitle_1.text = firstTitle
        } else {
            tvTitle_1.gone()
        }
    }

    override fun show2ndTitle(secondTitle: String) {
        if (!TextUtils.isEmpty(secondTitle)) {
            tvTitle_2.becomeVisible()
            tvTitle_2.text = secondTitle
        } else {
            tvTitle_2.gone()
        }
    }

    override fun showTagList(tagList: List<Tag>) {
        tvTagsLabel.becomeVisible()
        layoutTags.becomeVisible()
        loadInfoList(layoutTags, tagList)
    }

    override fun showArtistList(artistList: List<Tag>) {
        tvArtistsLabel.becomeVisible()
        layoutArtists.becomeVisible()
        loadInfoList(layoutArtists, artistList)
    }

    override fun showLanguageList(languageList: List<Tag>) {
        tvLanguagesLabel.becomeVisible()
        layoutLanguages.becomeVisible()
        loadInfoList(layoutLanguages, languageList)
    }

    override fun showCategoryList(categoryList: List<Tag>) {
        tvCategoriesLabel.becomeVisible()
        layoutCategories.becomeVisible()
        loadInfoList(layoutCategories, categoryList)
    }

    override fun showCharacterList(characterList: List<Tag>) {
        tvCharactersLabel.becomeVisible()
        layoutCharacters.becomeVisible()
        loadInfoList(layoutCharacters, characterList)
    }

    override fun showGroupList(groupList: List<Tag>) {
        tvGroupsLabel.becomeVisible()
        layoutGroups.becomeVisible()
        loadInfoList(layoutGroups, groupList)
    }

    override fun showParodyList(parodyList: List<Tag>) {
        tvParodiesLabel.becomeVisible()
        layoutParodies.becomeVisible()
        loadInfoList(layoutParodies, parodyList)
    }

    override fun hideTagList() {
        tvTagsLabel.gone()
        layoutTags.gone()
    }

    override fun hideArtistList() {
        tvArtistsLabel.gone()
        layoutArtists.gone()
    }

    override fun hideLanguageList() {
        tvLanguagesLabel.gone()
        layoutLanguages.gone()
    }

    override fun hideCategoryList() {
        tvCategoriesLabel.gone()
        layoutCategories.gone()
    }

    override fun hideCharacterList() {
        tvCharactersLabel.gone()
        layoutCharacters.gone()
    }

    override fun hideGroupList() {
        tvGroupsLabel.gone()
        layoutGroups.gone()
    }

    override fun hideParodyList() {
        tvParodiesLabel.gone()
        layoutParodies.gone()
    }

    override fun showPageCount(pageCount: Int) {
        tvPageCount.text = getString(R.string.page_count, pageCount.toString())
    }

    override fun showUploadedTime(uploadedTime: String) {
        tvUpdatedAt.text = getString(R.string.uploaded, uploadedTime)
    }

    override fun showBookThumbnailList(thumbnailList: List<String>) {
        if (thumbnailList.isEmpty()) {
            return
        }
        var spanCount = thumbnailList.size / NUM_OF_ROWS
        if (thumbnailList.size % NUM_OF_ROWS != 0) {
            spanCount++
        }

        Logger.d(
            TAG, "thumbnails: ${thumbnailList.size}," +
                    " number of rows: $NUM_OF_ROWS, spanCount: $spanCount"
        )
        previewLayoutManager = object : MyGridLayoutManager(context!!, spanCount) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        rvPreviewList.run {
            layoutManager = previewLayoutManager
            previewAdapter = PreviewAdapter(
                NUM_OF_ROWS,
                thumbnailList,
                object : PreviewAdapter.ThumbnailClickCallback {
                    override fun onThumbnailClicked(page: Int) {
                        presenter.startReadingFrom(page)
                    }
                })
            adapter = previewAdapter
            setItemViewCacheSize(PREVIEW_CACHE_SIZE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hsvPreviewThumbNail.setOnScrollChangeListener { _, _, _, _, _ ->
                if (!hsvPreviewThumbNail.canScrollHorizontally(1)) {
                    Logger.d(TAG, "End of list, load more thumbnails")
                    presenter.loadMoreThumbnails()
                }
            }
        }
    }

    override fun updateBookThumbnailList() {
        var spanCount = previewAdapter.itemCount / NUM_OF_ROWS
        if (previewAdapter.itemCount % NUM_OF_ROWS != 0) {
            spanCount++
        }
        previewLayoutManager.spanCount = spanCount
        previewAdapter.notifyDataSetChanged()
    }

    override fun showRecommendBook(bookList: List<Book>) {
        Logger.d(TAG, "recommended books, spanCount: ${bookList.size}")
        mtvRecommendBook.becomeVisible()
        val gridLayoutManager = object : MyGridLayoutManager(context!!, bookList.size) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }

        rvRecommendList.layoutManager = gridLayoutManager
        recommendBookAdapter = BookAdapter(
            bookList,
            BookAdapter.RECOMMEND_BOOK,
            object : BookAdapter.OnBookClick {
                override fun onItemClick(item: Book) {
                    BookPreviewActivity.restart(item)
                }
            })
        rvRecommendList.adapter = recommendBookAdapter
    }

    override fun showNoRecommendBook() {
        mtvRecommendBook.gone()
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
            isDownloadingRequested = false
            isDeletingRequested = false
        })
    }

    override fun initDownloading(total: Int) {
        clDownloadProgress.becomeVisible()
        pbDownloading.max = total
        mtvDownloaded.text = String.format(getString(R.string.preview_download_progress), 0, total)
    }

    override fun updateDownloadProgress(progress: Int, total: Int) {
        clDownloadProgress.becomeVisible()
        pbDownloading.max = total
        pbDownloading.progressDrawable = getProgressDrawableId(progress, total)
        pbDownloading.progress = progress
        mtvDownloaded.text =
            String.format(getString(R.string.preview_download_progress), progress, total)
    }

    override fun finishDownloading() {
        mtvDownloaded.text = getString(R.string.done)
        val handler = Handler()
        handler.postDelayed({
            pbDownloading.progressDrawable =
                getProgressDrawableId(0, pbDownloading.max)
            pbDownloading.max = 0
            clDownloadProgress.gone()
            mtvDownloaded.text = getString(R.string.preview_download_progress)
        }, DOWNLOADING_BAR_HIDING_DELAY)
    }

    override fun finishDownloading(downloadFailedCount: Int, total: Int) {
        mtvDownloaded.text =
            String.format(getString(R.string.fail_to_download), downloadFailedCount)
        val handler = Handler()
        handler.postDelayed({
            pbDownloading.progressDrawable =
                getProgressDrawableId(0, pbDownloading.max)
            pbDownloading.max = 0
            clDownloadProgress.gone()
            mtvDownloaded.text = getString(R.string.preview_download_progress)
        }, DOWNLOADING_BAR_HIDING_DELAY)
    }

    override fun initDeleting() {
        mtvDownloaded.text = ""
    }

    override fun updateDeletingProgress(progress: Int, total: Int) {
        clDownloadProgress.becomeVisible()
        pbDownloading.max = total
        pbDownloading.progressDrawable = getProgressDrawableId(progress, total)
        pbDownloading.progress = progress
        mtvDownloaded.text = getString(R.string.preview_deleting_progress, progress, total)
    }

    override fun finishDeleting(bookId: String) {
        pbDownloading.max = 1
        pbDownloading.progress = 1
        pbDownloading.progressDrawable = getProgressDrawableId(1, 1)
        mtvDownloaded.text = getString(R.string.cleared)

        Handler().postDelayed({
            pbDownloading.progressDrawable = getProgressDrawableId(0, 1)
            clDownloadProgress.gone()
        }, DELETING_BAR_HIDING_DELAY)

        closePreviewAfterRemovedBook(bookId)
    }

    override fun finishDeleting(bookId: String, deletingFailedCount: Int) {
        mtvDownloaded.text = getString(R.string.fail_to_delete, deletingFailedCount)
        pbDownloading.progressDrawable = getProgressDrawableId(0, pbDownloading.max)

        Handler().postDelayed({
            pbDownloading.max = 0
            pbDownloading.progress = 0
            clDownloadProgress.gone()
        }, DELETING_BAR_HIDING_DELAY)

        closePreviewAfterRemovedBook(bookId)
    }

    override fun showBookBeingDownloaded(bookId: String) {
        DialogHelper.showBookDownloadingDialog(activity!!, bookId, onOk = {
            presenter.restartBookPreview(bookId)
        }, onDismiss = {
            Logger.d(TAG, "Downloading book $bookId is aware")
        })
    }

    override fun showThisBookBeingDownloaded() {
        DialogHelper.showThisBookDownloadingDialog(activity!!, onOk = {
            Logger.d(TAG, "Downloading this book is aware")
        })
    }

    override fun showFavoriteBookSaved(isFavorite: Boolean) {
        if (isFavorite) {
            mtvNotFavorite.becomeInvisible()
            mtvFavorite.becomeVisible()
        } else {
            mtvNotFavorite.becomeVisible()
            mtvFavorite.becomeInvisible()
        }
    }

    override fun showFavoriteBooks(favoriteList: List<String>) {
        recommendBookAdapter.setFavoriteList(favoriteList)
    }

    override fun showRecentBooks(recentList: List<String>) {
        recommendBookAdapter.setRecentList(recentList)
    }

    override fun showOpenFolderView() {
        activity?.run {
            Handler().postDelayed({
                DialogHelper.showDownloadingFinishedDialog(this, onOk = {
                    val viewGalleryIntent = Intent(Intent.ACTION_VIEW)
                    viewGalleryIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    viewGalleryIntent.type = "image/*"
                    startActivity(
                        Intent.createChooser(viewGalleryIntent, getString(R.string.open_with))
                    )
                }, onDismiss = {

                })
            }, if (refreshGalleryDialog.isShowing) SHOW_DOWNLOADING_COMPLETE_DIALOG_DELAY else 0)
        }
    }

    override fun startReadingFromPage(page: Int, book: Book) {
        ReaderActivity.start(this, page, book, viewDownloadedData)
        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun showUnSeenButton() {
        buttonUnSeen.becomeVisible()
    }

    override fun hideUnSeenButton() {
        buttonUnSeen.gone()
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun isActive() = isAdded

    override fun onTagSelected(tag: Tag) {
        if (viewDownloadedData) {
            return
        }
        activity?.run {
            val intent = intent
            intent.action = Constants.TAG_SELECTED_ACTION
            intent.putExtra(Constants.SELECTED_TAG, tag.name)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun loadInfoList(layout: ViewGroup, infoList: List<Tag>) {
        val infoCardLayout = InformationCardAdapter(infoList)
        infoCardLayout.loadInfoList(layout)
        infoCardLayout.setTagSelectedListener(this)
    }

    private fun getAnimationListener(
        callOnEndingObject: ObjectAnimator
    ): Animator.AnimatorListener {
        return object : Animator.AnimatorListener {
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
    }

    private fun requestStoragePermission() {
        val storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(storagePermission, REQUEST_STORAGE_PERMISSION)
    }

    private fun getProgressDrawableId(progress: Int, max: Int): Drawable {
        val percentage = (progress * 1f) / (max * 1f)
        return ActivityCompat.getDrawable(
            context!!, when {
                percentage >= Constants.DOWNLOAD_GREEN_LEVEL -> R.drawable.bg_download_green
                percentage >= Constants.DOWNLOAD_YELLOW_LEVEL -> R.drawable.bg_download_yellow
                else -> R.drawable.bg_download_red
            }
        )!!
    }

    private fun closePreviewAfterRemovedBook(bookId: String) {
        Handler().postDelayed({
            activity?.run {
                intent.action = Constants.REFRESH_DOWNLOADED_BOOK_LIST
                intent.putExtra(BOOK_ID, bookId)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }, CLOSE_AFTER_REMOVED_TIME)
    }
}
