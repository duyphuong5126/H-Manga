package nhdphuong.com.manga.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import jp.shts.android.library.TriangleLabelView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.supports.SupportUtils
import java.util.*


/*
 * Created by nhdphuong on 3/18/18.
 */
class BookAdapter(
    private val itemList: List<Book>,
    private val adapterType: Int,
    private val bookClickCallback: OnBookClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TAG = "BookAdapter"
        const val HOME_PREVIEW_BOOK = 1
        const val RECOMMEND_BOOK = 2
    }

    private val recentList = ArrayList<String>()
    private val favoriteList = ArrayList<String>()
    private val downloadedThumbnails = ArrayList<Pair<String, String>>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val layoutResId = when (viewType) {
            HOME_PREVIEW_BOOK -> R.layout.item_home_list
            RECOMMEND_BOOK -> R.layout.item_recommend_list
            else -> R.layout.item_home_list
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutResId, parent, false)
        return MainListViewHolder(view, bookClickCallback)
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

    inner class MainListViewHolder(
        itemView: View,
        private val bookClickCallback: OnBookClick
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var bookPreview: Book
        private val ivItemThumbnail: ImageView = itemView.findViewById(R.id.ivItemThumbnail)
        private val tv1stTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        private val tv2ndTitle: TextView = itemView.findViewById(R.id.tv2ndTitlePart)
        private val tvLanguage: ImageView = itemView.findViewById(R.id.ivLanguage)
        private val tlvRecent: TriangleLabelView = itemView.findViewById(R.id.tlvRecent)
        private val vNavigation: View = itemView.findViewById(R.id.vNavigation)
        private val context: Context = itemView.context
        private var isTitleModifiable = true

        val ivThumbnail: ImageView
            get() = ivItemThumbnail

        init {
            vNavigation.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            bookClickCallback.onItemClick(bookPreview)
        }

        fun showRecentLabel() {
            tlvRecent.visibility = View.VISIBLE
            tlvRecent.primaryText = context.getString(R.string.recent)
            tlvRecent.setTriangleBackgroundColor(
                ContextCompat.getColor(context, R.color.blue0673B7)
            )
        }

        fun showFavoriteLabel() {
            tlvRecent.visibility = View.VISIBLE
            tlvRecent.primaryText = context.getString(R.string.favorite)
            tlvRecent.setTriangleBackgroundColor(
                ContextCompat.getColor(context, R.color.redED2553)
            )
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

            Logger.d(TAG, "Thumbnail: ${item.thumbnail}")
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
            tv1stTitle.viewTreeObserver.addOnGlobalLayoutListener {
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

    interface OnBookClick {
        fun onItemClick(item: Book)
    }
}
