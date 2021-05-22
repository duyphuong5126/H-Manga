package nhdphuong.com.manga.features.preview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.getDrawable
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_COMPLETED
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_FAILED
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_PROGRESS
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_STARTED
import nhdphuong.com.manga.Constants.Companion.ACTION_DISMISS_GALLERY_REFRESHING_DIALOG
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_COMPLETED
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_FAILED
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_PROGRESS
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_STARTED
import nhdphuong.com.manga.Constants.Companion.ACTION_SHOW_GALLERY_REFRESHING_DIALOG
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.DELETING_FAILED_COUNT
import nhdphuong.com.manga.Constants.Companion.DOWNLOADING_FAILED_COUNT
import nhdphuong.com.manga.Constants.Companion.PROGRESS
import nhdphuong.com.manga.Constants.Companion.TOTAL
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.data.entity.DownloadingResult
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.entity.comment.Comment
import nhdphuong.com.manga.features.comment.CommentThreadActivity
import nhdphuong.com.manga.features.reader.ReaderActivity
import nhdphuong.com.manga.service.BookDownloadingService
import nhdphuong.com.manga.supports.AnimationHelper
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.supports.copyToClipBoard
import nhdphuong.com.manga.supports.isFirstTimeInstall
import nhdphuong.com.manga.views.InformationCardAdapter
import nhdphuong.com.manga.views.MyGridLayoutManager
import nhdphuong.com.manga.views.adapters.BookAdapter
import nhdphuong.com.manga.views.adapters.CommentAdapter
import nhdphuong.com.manga.views.adapters.PreviewAdapter
import nhdphuong.com.manga.views.becomeInvisible
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.createLoadingDialog
import nhdphuong.com.manga.views.customs.MyButton
import nhdphuong.com.manga.views.customs.MyTextView
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.doOnScrollToBottom
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.showBookDeletingConfirmationDialog
import nhdphuong.com.manga.views.showBookDownloadingDialog
import nhdphuong.com.manga.views.showBookDownloadingFailureDialog
import nhdphuong.com.manga.views.showDownloadingFinishedDialog
import nhdphuong.com.manga.views.showThisBookDownloadingDialog
import nhdphuong.com.manga.views.showUnSeenBookConfirmationDialog

/*
 * Created by nhdphuong on 4/14/18.
 */
class BookPreviewFragment :
    Fragment(),
    BookPreviewContract.View,
    InformationCardAdapter.TagSelectedListener,
    View.OnClickListener {
    companion object {
        private const val NUM_OF_ROWS = 2
        private const val COVER_IMAGE_ANIMATION_START_DELAY = 100L
        private const val COVER_IMAGE_ANIMATION_DURATION = 6500L
        private const val COVER_IMAGE_ANIMATION_UP_OFFSET = -1000
        private const val COVER_IMAGE_ANIMATION_DOWN_OFFSET = 1000
        private const val DOWNLOADING_BAR_HIDING_DELAY = 2000L
        private const val DELETING_BAR_HIDING_DELAY = 3000L
        private const val CLOSE_AFTER_REMOVED_TIME = 3500L
        private const val SHOW_DOWNLOADING_COMPLETE_DIALOG_DELAY = 3000L
        private const val PREVIEW_CACHE_SIZE = 10
        private const val PREFETCH_COMMENTS_DISTANCE = 10
    }

    private val logger: Logger by lazy {
        Logger("BookPreviewFragment")
    }

    private lateinit var buttonClearDownloadedData: MyTextView
    private lateinit var buttonUnSeen: AppCompatImageButton
    private lateinit var clBookIdClickableArea: ConstraintLayout
    private lateinit var clDownloadProgress: ConstraintLayout
    private lateinit var hsvRecommendList: HorizontalScrollView
    private lateinit var ibBack: ImageButton
    private lateinit var ivBookCover: ImageView
    private lateinit var lastVisitedPage: View
    private lateinit var layoutArtists: LinearLayout
    private lateinit var layoutCategories: LinearLayout
    private lateinit var layoutCharacters: LinearLayout
    private lateinit var layoutGroups: LinearLayout
    private lateinit var layoutLanguages: LinearLayout
    private lateinit var layoutParodies: LinearLayout
    private lateinit var layoutTags: LinearLayout
    private lateinit var mbShowFullList: MyButton
    private lateinit var mtvCommentThread: MyTextView
    private lateinit var mtvDownload: MyTextView
    private lateinit var mtvDownloaded: MyTextView
    private lateinit var mtvFavorite: MyTextView
    private lateinit var mtvLastVisitedPage: MyTextView
    private lateinit var mtvNotFavorite: MyTextView
    private lateinit var mtvRecommendBook: MyTextView
    private lateinit var pbDownloading: ProgressBar
    private lateinit var rvCommentList: RecyclerView
    private lateinit var rvPreviewList: RecyclerView
    private lateinit var rvRecommendList: RecyclerView
    private lateinit var svBookCover: NestedScrollView
    private lateinit var svPreview: NestedScrollView
    private lateinit var tvArtistsLabel: MyTextView
    private lateinit var tvBookId: MyTextView
    private lateinit var tvCategoriesLabel: MyTextView
    private lateinit var tvCharactersLabel: MyTextView
    private lateinit var tvGroupsLabel: MyTextView
    private lateinit var tvLanguagesLabel: MyTextView
    private lateinit var tvPageCount: MyTextView
    private lateinit var tvParodiesLabel: MyTextView
    private lateinit var tvTagsLabel: MyTextView
    private lateinit var tvTitle1: MyTextView
    private lateinit var tvTitle2: MyTextView
    private lateinit var tvUpdatedAt: MyTextView
    private lateinit var ivPageThumbnail: ImageView
    private lateinit var mtvPageNumber: MyTextView
    private lateinit var vNavigation: View

    private lateinit var presenter: BookPreviewContract.Presenter
    private lateinit var previewAdapter: PreviewAdapter
    private lateinit var recommendBookAdapter: BookAdapter
    private lateinit var animatorSet: AnimatorSet
    private lateinit var refreshGalleryDialog: Dialog

    private var commentAdapter: CommentAdapter? = null
    private var tagsInfoAdapter: InformationCardAdapter? = null
    private var charactersInfoAdapter: InformationCardAdapter? = null
    private var artistsInfoAdapter: InformationCardAdapter? = null
    private var categoriesInfoAdapter: InformationCardAdapter? = null
    private var parodiesInfoAdapter: InformationCardAdapter? = null
    private var groupsInfoAdapter: InformationCardAdapter? = null
    private var languagesInfoAdapter: InformationCardAdapter? = null

    private var lastOrientation = Configuration.ORIENTATION_UNDEFINED

    @Volatile
    private var isPresenterStarted: Boolean = false

    private var viewDownloadedData = false

    private lateinit var previewLayoutManager: MyGridLayoutManager

    private var bookId: String = ""

    private var pageCountTemplate: String = ""
    private var uploadedTimeTemplate: String = ""
    private var previewDownloadProgressTemplate: String = ""
    private var previewDeleteProgressTemplate: String = ""
    private var failedToDownloadTemplate: String = ""
    private var failedToDeleteTemplate: String = ""
    private var doneLabel: String = ""
    private var clearedLabel: String = ""
    private var openWithLabel: String = ""
    private var showFullCommentThreadTemplate: String = ""
    private var favoriteWithCountTemplate: String = ""

    private val bookDownloadingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_DOWNLOADING_STARTED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    val totalBookPages = intent.extras?.getInt(TOTAL) ?: 0
                    presenter.initDownloading(bookId, totalBookPages)
                }
                ACTION_DOWNLOADING_PROGRESS -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    val totalBookPages = intent.extras?.getInt(TOTAL) ?: 0
                    val progress = intent.extras?.getInt(PROGRESS) ?: 0
                    presenter.updateDownloadingProgress(bookId, progress, totalBookPages)
                }
                ACTION_DOWNLOADING_COMPLETED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    presenter.finishDownloading(bookId)
                }
                ACTION_DOWNLOADING_FAILED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    val totalBookPages = intent.extras?.getInt(TOTAL) ?: 0
                    val downloadingFailedCount =
                        intent.extras?.getInt(DOWNLOADING_FAILED_COUNT) ?: 0
                    presenter.finishDownloading(bookId, downloadingFailedCount, totalBookPages)
                }
                ACTION_DELETING_STARTED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    presenter.initDeleting(bookId)
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
                }
                ACTION_DELETING_COMPLETED -> {
                    val bookId = intent.extras?.getString(BOOK_ID).orEmpty()
                    presenter.finishDeleting(bookId)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_preview, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            pageCountTemplate = it.getString(R.string.page_count)
            uploadedTimeTemplate = it.getString(R.string.uploaded)
            previewDownloadProgressTemplate = it.getString(R.string.preview_download_progress)
            previewDeleteProgressTemplate = it.getString(R.string.preview_deleting_progress)
            failedToDownloadTemplate = it.getString(R.string.fail_to_download)
            failedToDeleteTemplate = it.getString(R.string.fail_to_delete)
            doneLabel = it.getString(R.string.done)
            clearedLabel = it.getString(R.string.cleared)
            openWithLabel = it.getString(R.string.open_with)
            showFullCommentThreadTemplate = it.getString(R.string.show_full_comment_list)
            favoriteWithCountTemplate = it.getString(R.string.favorite_with_count)
        }

        setUpUI(view)

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
        mtvDownload.setOnClickListener(this)

        buttonClearDownloadedData.becomeVisibleIf(viewDownloadedData)
        buttonClearDownloadedData.setOnClickListener(this)

        val changeFavoriteListener = View.OnClickListener { presenter.changeBookFavorite() }
        mtvFavorite.setOnClickListener(changeFavoriteListener)
        mtvNotFavorite.setOnClickListener(changeFavoriteListener)

        hsvRecommendList.becomeVisibleIf(!viewDownloadedData)
        mtvRecommendBook.becomeVisibleIf(!viewDownloadedData)
        svPreview.overScrollMode = View.OVER_SCROLL_NEVER
        svBookCover.overScrollMode = View.OVER_SCROLL_NEVER

        ivBookCover.setOnClickListener(this)
        ibBack.setOnClickListener(this)
        buttonUnSeen.setOnClickListener(this)

        view.doOnGlobalLayout {
            if (!isPresenterStarted) {
                isPresenterStarted = true
                presenter.loadInfoLists()
            }
        }

        activity?.let {
            lastOrientation =
                it.resources?.configuration?.orientation ?: Configuration.ORIENTATION_UNDEFINED
            refreshGalleryDialog = it.createLoadingDialog(R.string.refreshing_gallery)
        }
        presenter.start()
        checkAndRequestStoragePermissionIfNecessary()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.READING_REQUEST) {
            presenter.refreshRecentStatus()
            data?.getIntExtra(Constants.LAST_VISITED_PAGE_RESULT, -1)
                ?.takeIf { it >= 0 }
                ?.let(presenter::refreshLastVisitedPage)
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
        if (bookId.isNotBlank()) {
            BookDownloadingService.getLastStatus(bookId)?.let {
                when (it) {
                    is DownloadingResult.DownloadingProgress -> {
                        updateDownloadProgress(it.progress, it.total)
                    }

                    is DownloadingResult.DownloadingFailure -> {
                        finishDownloadingWithError(bookId)
                    }

                    is DownloadingResult.DownloadingCompleted -> {
                        finishDownloading()
                    }
                }
            }
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (lastOrientation != newConfig.orientation) {
            lastOrientation = newConfig.orientation
            try {
                layoutTags.doOnGlobalLayout {
                    tagsInfoAdapter?.loadInfoList(layoutTags)
                }
                layoutArtists.doOnGlobalLayout {
                    artistsInfoAdapter?.loadInfoList(layoutArtists)
                }
                layoutCharacters.doOnGlobalLayout {
                    charactersInfoAdapter?.loadInfoList(layoutCharacters)
                }
                layoutGroups.doOnGlobalLayout {
                    groupsInfoAdapter?.loadInfoList(layoutGroups)
                }
                layoutParodies.doOnGlobalLayout {
                    parodiesInfoAdapter?.loadInfoList(layoutParodies)
                }
                layoutLanguages.doOnGlobalLayout {
                    languagesInfoAdapter?.loadInfoList(layoutLanguages)
                }
                layoutCategories.doOnGlobalLayout {
                    categoriesInfoAdapter?.loadInfoList(layoutCategories)
                }
            } catch (throwable: Throwable) {
                logger.e("Failed to update info lists with error: $throwable")
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mtvDownload -> {
                presenter.downloadBook()
            }

            R.id.buttonClearDownloadedData -> {
                presenter.requestBookDeleting()
            }

            R.id.mtvFavorite,
            R.id.mtvNotFavorite -> {
                presenter.changeBookFavorite()
            }

            R.id.ivBookCover -> {
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

            R.id.ibBack -> {
                activity?.onBackPressed()
            }

            R.id.buttonUnSeen -> {
                activity?.showUnSeenBookConfirmationDialog(onOk = {
                    presenter.unSeenBook()
                })
            }

            R.id.clBookIdClickableArea -> {
                Toast.makeText(context, "Copied Book ID $bookId to clipboard", LENGTH_SHORT).show()
                context?.copyToClipBoard(bookId, bookId)
            }
        }
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
            tvTitle1.becomeVisible()
            tvTitle1.text = firstTitle
        } else {
            tvTitle1.gone()
        }
    }

    override fun show2ndTitle(secondTitle: String) {
        if (!TextUtils.isEmpty(secondTitle)) {
            tvTitle2.becomeVisible()
            tvTitle2.text = secondTitle
        } else {
            tvTitle2.gone()
        }
    }

    override fun showBookId(bookId: String) {
        tvBookId.text = bookId
        clBookIdClickableArea.setOnClickListener(this)
        this.bookId = bookId
    }

    override fun showFavoriteCount(favoriteCount: Int) {
        try {
            val favorites = SupportUtils.formatFavoriteNumber(favoriteCount.toLong())
            val favoriteWithCount = String.format(favoriteWithCountTemplate, favorites)
            mtvFavorite.text = favoriteWithCount
            mtvNotFavorite.text = favoriteWithCount
        } catch (throwable: Throwable) {
            logger.d(throwable.localizedMessage)
        }
    }

    override fun showTagList(tagList: List<Tag>) {
        tvTagsLabel.becomeVisible()
        layoutTags.becomeVisible()
        tagsInfoAdapter = loadInfoList(layoutTags, tagList)
    }

    override fun showArtistList(artistList: List<Tag>) {
        tvArtistsLabel.becomeVisible()
        layoutArtists.becomeVisible()
        artistsInfoAdapter = loadInfoList(layoutArtists, artistList)
    }

    override fun showLanguageList(languageList: List<Tag>) {
        tvLanguagesLabel.becomeVisible()
        layoutLanguages.becomeVisible()
        languagesInfoAdapter = loadInfoList(layoutLanguages, languageList)
    }

    override fun showCategoryList(categoryList: List<Tag>) {
        tvCategoriesLabel.becomeVisible()
        layoutCategories.becomeVisible()
        categoriesInfoAdapter = loadInfoList(layoutCategories, categoryList)
    }

    override fun showCharacterList(characterList: List<Tag>) {
        tvCharactersLabel.becomeVisible()
        layoutCharacters.becomeVisible()
        charactersInfoAdapter = loadInfoList(layoutCharacters, characterList)
    }

    override fun showGroupList(groupList: List<Tag>) {
        tvGroupsLabel.becomeVisible()
        layoutGroups.becomeVisible()
        groupsInfoAdapter = loadInfoList(layoutGroups, groupList)
    }

    override fun showParodyList(parodyList: List<Tag>) {
        tvParodiesLabel.becomeVisible()
        layoutParodies.becomeVisible()
        parodiesInfoAdapter = loadInfoList(layoutParodies, parodyList)
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
        tvPageCount.text = String.format(pageCountTemplate, pageCount.toString())
    }

    override fun showUploadedTime(uploadedTime: String) {
        tvUpdatedAt.text = String.format(uploadedTimeTemplate, uploadedTime)
    }

    override fun showBookThumbnailList(thumbnailList: List<String>) {
        if (thumbnailList.isEmpty()) {
            return
        }
        rvPreviewList.run {
            previewAdapter = PreviewAdapter(
                NUM_OF_ROWS,
                thumbnailList,
                object : PreviewAdapter.ThumbnailClickCallback {
                    override fun onThumbnailClicked(page: Int) {
                        presenter.startReadingFrom(page)
                    }
                })

            var spanCount = previewAdapter.itemCount / NUM_OF_ROWS
            if (previewAdapter.itemCount % NUM_OF_ROWS != 0) {
                spanCount++
            }
            context?.let {
                previewLayoutManager = object : MyGridLayoutManager(it, spanCount) {
                    override fun isAutoMeasureEnabled(): Boolean {
                        return true
                    }
                }
                layoutManager = previewLayoutManager
            }
            adapter = previewAdapter

            setItemViewCacheSize(PREVIEW_CACHE_SIZE)
        }
        presenter.loadLastVisitedPage()
    }

    override fun showRecommendBook(bookList: List<Book>) {
        logger.d("recommended books, spanCount: ${bookList.size}")
        context?.let {
            mtvRecommendBook.becomeVisible()
            hsvRecommendList.becomeVisible()
            val gridLayoutManager = object : MyGridLayoutManager(it, bookList.size) {
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
    }

    override fun showNoRecommendBook() {
        mtvRecommendBook.gone()
        hsvRecommendList.gone()
    }

    override fun initDownloading(total: Int) {
        clDownloadProgress.becomeVisible()
        pbDownloading.max = total
        mtvDownloaded.text = String.format(previewDownloadProgressTemplate, 0, total)
        buttonClearDownloadedData.gone()
    }

    override fun updateDownloadProgress(progress: Int, total: Int) {
        clDownloadProgress.becomeVisible()
        pbDownloading.max = total
        updateProgressDrawable(progress, total)
        pbDownloading.progress = progress
        mtvDownloaded.text =
            String.format(previewDownloadProgressTemplate, progress, total)
        buttonClearDownloadedData.gone()
        BookDownloadingService.clearStatus(bookId)
    }

    override fun finishDownloading() {
        mtvDownloaded.text = doneLabel
        pbDownloading.let {
            it.postDelayed({
                updateProgressDrawable(0, it.max)
                it.max = 0
                clDownloadProgress.gone()
                mtvDownloaded.text = previewDownloadProgressTemplate
            }, DOWNLOADING_BAR_HIDING_DELAY)
        }
        buttonClearDownloadedData.becomeVisibleIf(viewDownloadedData)
        BookDownloadingService.clearStatus(bookId)
    }

    override fun finishDownloading(downloadFailedCount: Int, total: Int) {
        mtvDownloaded.text =
            String.format(failedToDownloadTemplate, downloadFailedCount)
        pbDownloading.let {
            it.postDelayed({
                updateProgressDrawable(0, it.max)
                it.max = 0
                clDownloadProgress.gone()
                mtvDownloaded.text = previewDownloadProgressTemplate
            }, DOWNLOADING_BAR_HIDING_DELAY)
        }
        buttonClearDownloadedData.becomeVisibleIf(viewDownloadedData)
        BookDownloadingService.clearStatus(bookId)
    }

    private fun finishDownloadingWithError(bookId: String) {
        activity?.showBookDownloadingFailureDialog(bookId)
        pbDownloading.let {
            it.postDelayed({
                updateProgressDrawable(0, it.max)
                it.max = 0
                clDownloadProgress.gone()
                mtvDownloaded.text = previewDownloadProgressTemplate
            }, DOWNLOADING_BAR_HIDING_DELAY)
        }
        buttonClearDownloadedData.becomeVisibleIf(viewDownloadedData)
        BookDownloadingService.clearStatus(bookId)
    }

    override fun initDeleting() {
        mtvDownloaded.text = ""
    }

    override fun updateDeletingProgress(progress: Int, total: Int) {
        clDownloadProgress.becomeVisible()
        pbDownloading.max = total
        updateProgressDrawable(progress, total)
        pbDownloading.progress = progress
        mtvDownloaded.text = String.format(previewDeleteProgressTemplate, progress, total)
    }

    override fun finishDeleting(bookId: String) {
        pbDownloading.max = 1
        pbDownloading.progress = 1
        updateProgressDrawable(1, 1)
        mtvDownloaded.text = clearedLabel

        pbDownloading.postDelayed({
            updateProgressDrawable(0, 1)
            clDownloadProgress.gone()
        }, DELETING_BAR_HIDING_DELAY)

        closePreviewAfterRemovedBook(bookId)
    }

    override fun finishDeleting(bookId: String, deletingFailedCount: Int) {
        mtvDownloaded.text = String.format(failedToDeleteTemplate, deletingFailedCount)
        pbDownloading.let {
            updateProgressDrawable(0, it.max)

            it.postDelayed({
                it.max = 0
                it.progress = 0
                clDownloadProgress.gone()
            }, DELETING_BAR_HIDING_DELAY)
        }

        closePreviewAfterRemovedBook(bookId)
    }

    override fun showThisBookBeingDownloaded() {
        activity?.showThisBookDownloadingDialog()
    }

    override fun showThisBookWasAddedIntoQueue(currentBookId: String) {
        activity?.showBookDownloadingDialog(currentBookId)
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

    override fun showBookDownloadedMessage(bookId: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            activity?.showDownloadingFinishedDialog(bookId)
        }, if (refreshGalleryDialog.isShowing) SHOW_DOWNLOADING_COMPLETE_DIALOG_DELAY else 0)
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

    override fun showLastVisitedPage(page: Int, pageUrl: String) {
        ImageUtils.loadImage(pageUrl, R.drawable.ic_404_not_found, ivPageThumbnail)
        mtvPageNumber.text = "$page"
        vNavigation.setOnClickListener {
            presenter.startReadingFrom(page - 1)
        }
        mtvLastVisitedPage.becomeVisible()
        lastVisitedPage.becomeVisible()
    }

    override fun hideLastVisitedPage() {
        ImageUtils.clear(ivPageThumbnail)
        mtvPageNumber.text = ""
        mtvLastVisitedPage.gone()
        lastVisitedPage.gone()
    }

    override fun setUpCommentList(commentList: List<Comment>, pageSize: Int) {
        commentAdapter = CommentAdapter(commentList)
        rvCommentList.adapter = commentAdapter
        rvCommentList.isNestedScrollingEnabled = false
        context?.let {
            val linearLayoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            rvCommentList.layoutManager = linearLayoutManager
            rvCommentList.addItemDecoration(SpaceItemDecoration(it, R.dimen.space_medium))
            val hasComments = commentList.isNotEmpty()
            mtvCommentThread.becomeVisibleIf(hasComments)
            rvCommentList.becomeVisibleIf(hasComments)
            if (hasComments) {
                svPreview.doOnScrollToBottom(
                    linearLayoutManager, PREFETCH_COMMENTS_DISTANCE, this::prefetchCommentList
                )
            }
        }
    }

    override fun showMoreCommentList(commentList: List<Comment>) {
        if (commentList.isEmpty()) {
            return
        }
        commentAdapter?.addNewComments(commentList)
        if (commentList.isNotEmpty()) {
            (rvCommentList.layoutManager as? LinearLayoutManager)?.let {
                svPreview.doOnScrollToBottom(
                    it, PREFETCH_COMMENTS_DISTANCE, this::prefetchCommentList
                )
            }
        }
    }

    override fun hideCommentList() {
        mtvCommentThread.gone()
        rvCommentList.gone()
    }

    override fun enableShowFullCommentListButton(notShownComments: Int, bookId: String) {
        mbShowFullList.text = String.format(showFullCommentThreadTemplate, notShownComments)
        mbShowFullList.becomeVisible()
        mbShowFullList.setOnClickListener {
            context?.let {
                CommentThreadActivity.start(it, bookId)
            }
        }
    }

    override fun showBookDeletingConfirmationMessage(bookId: String) {
        activity?.showBookDeletingConfirmationDialog(bookId, onOk = {
            presenter.deleteBook()
        })
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun isActive() = isAdded

    override fun onTagSelected(tag: Tag) {
        if (viewDownloadedData) {
            val name = tag.name
            context?.copyToClipBoard(name, name)
            Toast.makeText(context, "Copied tag name $name to clipboard", LENGTH_SHORT).show()
        } else {
            activity?.run {
                val intent = intent
                intent.action = Constants.TAG_SELECTED_ACTION
                intent.putExtra(Constants.SELECTED_TAG, tag.name)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun setUpUI(rootView: View) {
        buttonClearDownloadedData = rootView.findViewById(R.id.buttonClearDownloadedData)
        buttonUnSeen = rootView.findViewById(R.id.buttonUnSeen)
        clBookIdClickableArea = rootView.findViewById(R.id.clBookIdClickableArea)
        clDownloadProgress = rootView.findViewById(R.id.clDownloadProgress)
        hsvRecommendList = rootView.findViewById(R.id.hsvRecommendList)
        ibBack = rootView.findViewById(R.id.ibBack)
        ivBookCover = rootView.findViewById(R.id.ivBookCover)
        lastVisitedPage = rootView.findViewById(R.id.lastVisitedPage)
        layoutArtists = rootView.findViewById(R.id.layoutArtists)
        layoutCategories = rootView.findViewById(R.id.layoutCategories)
        layoutCharacters = rootView.findViewById(R.id.layoutCharacters)
        layoutGroups = rootView.findViewById(R.id.layoutGroups)
        layoutLanguages = rootView.findViewById(R.id.layoutLanguages)
        layoutParodies = rootView.findViewById(R.id.layoutParodies)
        layoutTags = rootView.findViewById(R.id.layoutTags)
        mbShowFullList = rootView.findViewById(R.id.mbShowFullList)
        mtvCommentThread = rootView.findViewById(R.id.mtvCommentThread)
        mtvDownload = rootView.findViewById(R.id.mtvDownload)
        mtvDownloaded = rootView.findViewById(R.id.mtvDownloaded)
        mtvFavorite = rootView.findViewById(R.id.mtvFavorite)
        mtvLastVisitedPage = rootView.findViewById(R.id.mtvLastVisitedPage)
        mtvNotFavorite = rootView.findViewById(R.id.mtvNotFavorite)
        mtvRecommendBook = rootView.findViewById(R.id.mtvRecommendBook)
        pbDownloading = rootView.findViewById(R.id.pbDownloading)
        rvCommentList = rootView.findViewById(R.id.rvCommentList)
        rvPreviewList = rootView.findViewById(R.id.rvPreviewList)
        rvRecommendList = rootView.findViewById(R.id.rvRecommendList)
        svBookCover = rootView.findViewById(R.id.svBookCover)
        svPreview = rootView.findViewById(R.id.svPreview)
        tvArtistsLabel = rootView.findViewById(R.id.tvArtistsLabel)
        tvBookId = rootView.findViewById(R.id.tvBookId)
        tvCategoriesLabel = rootView.findViewById(R.id.tvCategoriesLabel)
        tvCharactersLabel = rootView.findViewById(R.id.tvCharactersLabel)
        tvGroupsLabel = rootView.findViewById(R.id.tvGroupsLabel)
        tvLanguagesLabel = rootView.findViewById(R.id.tvLanguagesLabel)
        tvPageCount = rootView.findViewById(R.id.tvPageCount)
        tvParodiesLabel = rootView.findViewById(R.id.tvParodiesLabel)
        tvTagsLabel = rootView.findViewById(R.id.tvTagsLabel)
        tvTitle1 = rootView.findViewById(R.id.tvTitle_1)
        tvTitle2 = rootView.findViewById(R.id.tvTitle_2)
        tvUpdatedAt = rootView.findViewById(R.id.tvUpdatedAt)
        ivPageThumbnail = lastVisitedPage.findViewById(R.id.ivPageThumbnail)
        mtvPageNumber = lastVisitedPage.findViewById(R.id.mtvPageNumber)
        vNavigation = lastVisitedPage.findViewById(R.id.vNavigation)
    }

    private fun loadInfoList(layout: ViewGroup, infoList: List<Tag>): InformationCardAdapter {
        val infoCardLayout = InformationCardAdapter(infoList)
        infoCardLayout.loadInfoList(layout)
        infoCardLayout.setTagSelectedListener(this)
        return infoCardLayout
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

    private fun updateProgressDrawable(progress: Int, max: Int) {
        context?.let {
            pbDownloading.progressDrawable = getProgressDrawableId(it, progress, max)
        }
    }

    private fun getProgressDrawableId(context: Context, progress: Int, max: Int): Drawable? {
        val percentage = (progress * 1f) / (max * 1f)
        return getDrawable(
            context, when {
                percentage >= Constants.DOWNLOAD_GREEN_LEVEL -> R.drawable.bg_download_green
                percentage >= Constants.DOWNLOAD_YELLOW_LEVEL -> R.drawable.bg_download_yellow
                else -> R.drawable.bg_download_red
            }
        )
    }

    private fun closePreviewAfterRemovedBook(bookId: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            activity?.run {
                intent.action = Constants.REFRESH_DOWNLOADED_BOOK_LIST
                intent.putExtra(BOOK_ID, bookId)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }, CLOSE_AFTER_REMOVED_TIME)
    }

    private fun prefetchCommentList() {
        presenter.syncNextPageOfCommentList(commentAdapter?.itemCount ?: 0)
    }

    private fun checkAndRequestStoragePermissionIfNecessary() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && viewDownloadedData) {
            if (context?.isFirstTimeInstall() == true) {
                return
            }
            activity?.let {
                val readExternalStoragePer = android.Manifest.permission.READ_EXTERNAL_STORAGE
                val permissionGranted = checkSelfPermission(
                    it, readExternalStoragePer
                ) == PackageManager.PERMISSION_GRANTED
                if (!permissionGranted) {
                    requestPermissions(it, arrayOf(readExternalStoragePer), 53498)
                }
            }
        }
    }
}
