package nhdphuong.com.manga.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.util.Log
import jp.shts.android.library.TriangleLabelView
import nhdphuong.com.manga.R
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.supports.GlideUtils
import nhdphuong.com.manga.supports.SupportUtils
import java.util.*

/*
 * Created by nhdphuong on 3/18/18.
 */
class BookAdapter(private val mItemList: List<Book>, private val mAdapterType: Int, private val mBookClickCallback: OnBookClick)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private val TAG = BookAdapter::class.java.simpleName
        const val HOME_PREVIEW_BOOK = 1
        const val RECOMMEND_BOOK = 2
    }

    private val mRecentList = LinkedList<Int>()
    private val mFavoriteList = LinkedList<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutResId = when (viewType) {
            HOME_PREVIEW_BOOK -> R.layout.item_home_list
            RECOMMEND_BOOK -> R.layout.item_recommend_list
            else -> R.layout.item_home_list
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return MainListViewHolder(view, mBookClickCallback)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
        super.onViewRecycled(holder)
        val mainListViewHolder = holder as MainListViewHolder
        GlideUtils.clear(mainListViewHolder.ivThumbnail)
    }

    override fun getItemCount(): Int = mItemList.size

    override fun getItemViewType(position: Int): Int = mAdapterType

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val mainLisViewHolder = holder as MainListViewHolder
        mainLisViewHolder.setData(mItemList[position])

        val isRecent = mRecentList.contains(position)
        val isFavorite = mFavoriteList.contains(position)
        if (isFavorite || isRecent) {
            if (isRecent) {
                mRecentList.remove(position)
                mainLisViewHolder.showRecentLabel()
            }
            if (isFavorite) {
                mFavoriteList.remove(position)
                mainLisViewHolder.showFavoriteLabel()
            }
        } else {
            mainLisViewHolder.hideRecentView()
        }
    }

    fun setRecentList(recentList: List<Int>) {
        mRecentList.clear()
        mRecentList.addAll(recentList)
        for (recentId in recentList) {
            notifyItemChanged(recentId)
        }
    }

    fun setFavoriteList(favoriteList: List<Int>) {
        mFavoriteList.clear()
        mFavoriteList.addAll(favoriteList)
        for (favoriteId in favoriteList) {
            notifyItemChanged(favoriteId)
        }
    }

    inner class MainListViewHolder(itemView: View, private val mBookClickCallback: OnBookClick) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var mBookPreview: Book
        private val mIvItemThumbnail: ImageView = itemView.findViewById(R.id.ivItemThumbnail)
        private val mTv1stTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        private val mTv2ndTitle: TextView = itemView.findViewById(R.id.tv2ndTitlePart)
        private val mIvLanguage: ImageView = itemView.findViewById(R.id.ivLanguage)
        private val mTlvRecent: TriangleLabelView = itemView.findViewById(R.id.tlvRecent)
        private val mContext: Context = itemView.context
        private var mIsTitleModifiable = true

        val ivThumbnail: ImageView
            get() = mIvItemThumbnail

        init {
            mIvItemThumbnail.setOnClickListener(this)
            mTv1stTitle.setOnClickListener(this)
            mTv2ndTitle.setOnClickListener(this)
            mIvLanguage.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            mBookClickCallback.onItemClick(mBookPreview)
        }

        fun showRecentLabel() {
            mTlvRecent.visibility = View.VISIBLE
            mTlvRecent.primaryText = mContext.getString(R.string.recent)
            mTlvRecent.setTriangleBackgroundColor(ContextCompat.getColor(mContext, R.color.blue0673B7))
        }

        fun showFavoriteLabel() {
            mTlvRecent.visibility = View.VISIBLE
            mTlvRecent.primaryText = mContext.getString(R.string.favorite)
            mTlvRecent.setTriangleBackgroundColor(ContextCompat.getColor(mContext, R.color.redED2553))
        }

        fun hideRecentView() {
            mTlvRecent.visibility = View.GONE
        }

        @SuppressLint("SetTextI18n")
        fun setData(item: Book) {
            mIsTitleModifiable = true
            mBookPreview = item
            val languageIconResId = when (item.language) {
                Constants.CHINESE_LANG -> R.drawable.ic_lang_cn
                Constants.ENGLISH_LANG -> R.drawable.ic_lang_gb
                else -> R.drawable.ic_lang_jp
            }
            mIvLanguage.setImageResource(languageIconResId)

            Log.d(TAG, "Thumbnail: ${item.thumbnail}")
            GlideUtils.loadImage(item.thumbnail, R.drawable.ic_404_not_found, mIvItemThumbnail)

            mTv1stTitle.text = item.previewTitle
            mTv1stTitle.viewTreeObserver.addOnGlobalLayoutListener({
                if (mIsTitleModifiable) {
                    val fullText = item.previewTitle
                    val ellipsizedText = SupportUtils.getEllipsizedText(mTv1stTitle)
                    val remainText = fullText.replace(ellipsizedText, "")
                    mTv1stTitle.text = ellipsizedText
                    mTv2ndTitle.text = remainText
                    mIsTitleModifiable = false
                }
            })
        }
    }

    interface OnBookClick {
        fun onItemClick(item: Book)
    }
}