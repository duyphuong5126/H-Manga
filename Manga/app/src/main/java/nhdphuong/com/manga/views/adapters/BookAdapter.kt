package nhdphuong.com.manga.views.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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

/*
 * Created by nhdphuong on 3/18/18.
 */
class BookAdapter(
    private val itemList: List<Book>,
    private val adapterType: Int,
    private val bookClickCallback: OnBookClick,
    private val onBookBlocked: OnBookBlocked? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val HOME_PREVIEW_BOOK = 1
        const val RECOMMEND_BOOK = 2
        const val REMOVABLE_RECOMMEND_BOOK = 3
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
                MainListViewHolder(view, bookClickCallback)
            }
            REMOVABLE_RECOMMEND_BOOK -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_removable_recommendation, parent, false)
                RemovableRecommendedBookViewHolder(view, bookClickCallback)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_home_list, parent, false)
                MainListViewHolder(view, bookClickCallback)
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
        if (isFavorite || isRecent) {
            if (isRecent) {
                mainLisViewHolder.showRecentLabel()
            }
            if (isFavorite) {
                mainLisViewHolder.showFavoriteLabel()
            }
        } else {
            mainLisViewHolder.hideRecentView()
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

    open inner class MainListViewHolder(
        itemView: View,
        private val bookClickCallback: OnBookClick
    ) : RecyclerView.ViewHolder(itemView) {
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

        val ivThumbnail: ImageView
            get() = ivItemThumbnail

        init {
            vNavigation.setOnClickListener {
                bookClickCallback.onItemClick(bookPreview)
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
            val languageIconResId = when (item.language) {
                Constants.CHINESE_LANG -> R.drawable.ic_lang_cn
                Constants.ENGLISH_LANG -> R.drawable.ic_lang_gb
                else -> R.drawable.ic_lang_jp
            }
            tvLanguage.setImageResource(languageIconResId)

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

            tv1stTitle.text = item.previewTitle
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

    inner class RemovableRecommendedBookViewHolder(
        itemView: View,
        bookClickCallback: OnBookClick
    ) : MainListViewHolder(itemView, bookClickCallback) {
        private val ibDoNotRecommend: ImageButton = itemView.findViewById(R.id.ibDoNotRecommend)

        init {
            ibDoNotRecommend.setOnClickListener {
                onBookBlocked?.onBlockingBook(bookPreview.bookId)
            }
        }
    }

    interface OnBookClick {
        fun onItemClick(item: Book)
    }

    interface OnBookBlocked {
        fun onBlockingBook(bookId: String)
    }
}
