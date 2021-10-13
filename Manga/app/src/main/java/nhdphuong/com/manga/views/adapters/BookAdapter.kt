package nhdphuong.com.manga.views.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import jp.shts.android.library.TriangleLabelView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.getInsetDrawable

/*
 * Created by nhdphuong on 3/18/18.
 */
class BookAdapter(
    private val itemList: List<Book>,
    private val adapterType: Int,
    private val onBookSelected: (item: Book) -> Unit = {},
    private val onBlockingBook: (bookId: String) -> Unit = {},
    private val onFavoriteBookAdded: (item: Book) -> Unit = {},
    private val onFavoriteBookRemoved: (item: Book) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val HOME_PREVIEW_BOOK = 1
        const val RECOMMEND_BOOK = 2
        const val RECOMMEND_BOOK_WITH_ACTIONS = 3
    }

    private val recentList = ArrayList<String>()
    private val favoriteList = ArrayList<String>()
    private val downloadedThumbnails = ArrayList<Pair<String, String>>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            RECOMMEND_BOOK -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recommend_list, parent, false)
                MainListViewHolder(view)
            }
            RECOMMEND_BOOK_WITH_ACTIONS -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recommendation_with_actions, parent, false)
                RemovableRecommendedBookViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_home_list, parent, false)
                MainListViewHolder(view)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        val mainListViewHolder = holder as MainListViewHolder
        ImageUtils.clear(mainListViewHolder.ivThumbnail)
    }

    override fun getItemCount(): Int = itemList.size

    override fun getItemViewType(position: Int): Int = adapterType

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mainLisViewHolder = holder as MainListViewHolder
        mainLisViewHolder.setData(itemList[position])

        val bookId = itemList[position].bookId
        val isRecent = recentList.contains(bookId)
        val isFavorite = favoriteList.contains(bookId)
        mainLisViewHolder.isFavorite = isFavorite
        when {
            isFavorite -> mainLisViewHolder.showFavoriteLabel()
            isRecent -> mainLisViewHolder.showRecentLabel()
            else -> mainLisViewHolder.hideRecentView()
        }
    }

    fun setRecentList(recentList: List<String>) {
        val toNotifyList = mutableListOf<String>().apply {
            addAll(this@BookAdapter.recentList.filterNot { recentList.contains(it) })
            addAll(recentList.filterNot { this@BookAdapter.recentList.contains(it) })
        }
        this.recentList.clear()
        this.recentList.addAll(recentList)
        toNotifyList.forEach { bookId ->
            itemList.indexOfFirst { it.bookId == bookId }.takeIf { it >= 0 }?.let { index ->
                notifyItemChanged(index)
            }
        }
    }

    fun setFavoriteList(favoriteList: List<String>) {
        val toNotifyList = mutableListOf<String>().apply {
            addAll(this@BookAdapter.favoriteList.filterNot { favoriteList.contains(it) })
            addAll(favoriteList.filterNot { this@BookAdapter.favoriteList.contains(it) })
        }
        this.favoriteList.clear()
        this.favoriteList.addAll(favoriteList)
        toNotifyList.forEach { bookId ->
            itemList.indexOfFirst { it.bookId == bookId }.takeIf { it >= 0 }?.let { index ->
                notifyItemChanged(index)
            }
        }
    }

    fun publishDownloadedThumbnails(thumbnails: List<Pair<String, String>>) {
        downloadedThumbnails.clear()
        downloadedThumbnails.addAll(thumbnails)
        notifyDataSetChanged()
    }

    open inner class MainListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected lateinit var bookPreview: Book
        private val ivItemThumbnail: ImageView = itemView.findViewById(R.id.ivItemThumbnail)
        private val tv1stTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        private val tv2ndTitle: TextView = itemView.findViewById(R.id.tv2ndTitlePart)
        private val tvLanguage: ImageView = itemView.findViewById(R.id.ivLanguage)
        private val tlvRecent: TriangleLabelView = itemView.findViewById(R.id.tlvRecent)
        private val vNavigation: View = itemView.findViewById(R.id.vNavigation)

        private val recentLabel = itemView.context.getString(R.string.recent)
        private val favoriteLabel = itemView.context.getString(R.string.favorite)

        private val recentColor = ContextCompat.getColor(itemView.context, R.color.blue0673B7)
        private val favoriteColor = ContextCompat.getColor(itemView.context, R.color.redED2553)

        private var isTitleModifiable = true
        var isFavorite = false

        val ivThumbnail: ImageView
            get() = ivItemThumbnail

        init {
            vNavigation.setOnClickListener {
                onBookSelected(bookPreview)
            }
        }

        fun showRecentLabel() {
            tlvRecent.visibility = View.VISIBLE
            tlvRecent.primaryText = recentLabel
            tlvRecent.setTriangleBackgroundColor(recentColor)
        }

        fun showFavoriteLabel() {
            tlvRecent.visibility = View.VISIBLE
            tlvRecent.primaryText = favoriteLabel
            tlvRecent.setTriangleBackgroundColor(favoriteColor)
        }

        fun hideRecentView() {
            tlvRecent.visibility = View.GONE
        }

        @SuppressLint("SetTextI18n")
        fun setData(item: Book) {
            isTitleModifiable = true
            bookPreview = item
            when (item.language) {
                Constants.CHINESE_LANG -> R.drawable.ic_lang_cn
                Constants.ENGLISH_LANG -> R.drawable.ic_lang_gb
                Constants.JAPANESE_LANG -> R.drawable.ic_lang_jp
                else -> null
            }?.let(tvLanguage::setImageResource)

            if (!NHentaiApp.instance.isCensored) {
                val downloadedThumbnail = downloadedThumbnails.firstOrNull {
                    it.first == item.bookId
                }?.second.orEmpty()
                val thumbnail = if (downloadedThumbnail.isBlank()) {
                    item.thumbnail
                } else {
                    downloadedThumbnail
                }
                ImageUtils.loadImage(thumbnail, R.drawable.ic_404_not_found, ivItemThumbnail)
            } else {
                ivItemThumbnail.setImageResource(R.drawable.ic_nothing_here_grey)
            }

            tv1stTitle.text = when (item.language) {
                Constants.CHINESE_LANG,
                Constants.ENGLISH_LANG,
                Constants.JAPANESE_LANG -> item.previewTitle
                else -> String.format("[%s] %s", item.language.uppercase(), item.previewTitle)
            }
            tv1stTitle.doOnGlobalLayout {
                if (isTitleModifiable) {
                    val fullText = item.previewTitle
                    val ellipsizedText = SupportUtils.getEllipsizedText(tv1stTitle)
                    val remainText = fullText.replace(ellipsizedText, "")
                    tv1stTitle.text = ellipsizedText
                    tv2ndTitle.text = remainText
                    isTitleModifiable = false
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    inner class RemovableRecommendedBookViewHolder(
        itemView: View
    ) : MainListViewHolder(itemView), PopupMenu.OnMenuItemClickListener {
        private val ibDoNotRecommend: ImageButton = itemView.findViewById(R.id.ibAction)
        private val popUpMenu
            get() = PopupMenu(itemView.context, ibDoNotRecommend).apply {
                val menuResId = if (isFavorite) {
                    R.menu.favorite_book_action_menu
                } else {
                    R.menu.book_action_menu
                }
                menuInflater.inflate(menuResId, menu)
                if (menu is MenuBuilder) {
                    val menuBuilder = menu as MenuBuilder
                    menuBuilder.setOptionalIconsVisible(true)
                    menuBuilder.visibleItems.forEach {
                        setUpItemIcon(it)
                    }
                }
                setOnMenuItemClickListener(this@RemovableRecommendedBookViewHolder)
            }

        init {
            ibDoNotRecommend.setOnClickListener {
                popUpMenu.show()
            }
        }

        private fun setUpItemIcon(item: MenuItemImpl) {
            item.icon = itemView.context.getInsetDrawable(
                item.icon,
                startMarginResId = R.dimen.space_small,
                endMarginResId = R.dimen.space_small
            )
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.actionBlock -> {
                    onBlockingBook.invoke(bookPreview.bookId)
                }

                R.id.actionFavorite -> {
                    onFavoriteBookAdded.invoke(bookPreview)
                }

                R.id.actionRemoveFavorite -> {
                    onFavoriteBookRemoved.invoke(bookPreview)
                }
            }
            return true
        }
    }
}
