package nhdphuong.com.manga.views.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.views.customs.MyTextView
import nhdphuong.com.manga.views.doOnGlobalLayout

/*
 * Created by nhdphuong on 4/28/18.
 */
class PreviewAdapter(
    private val numOfRows: Int,
    previewList: List<String>,
    private val callback: ThumbnailClickCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val isMaximumItemExceeded: Boolean = previewList.size > MAX_ITEM_COUNT
    private val previewUrlList = mutableListOf<String>()

    init {
        val totalItemCount = if (isMaximumItemExceeded) MAX_ITEM_COUNT else previewList.size
        previewUrlList.addAll(previewList.subList(0, totalItemCount))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_PREVIEW -> {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_preview,
                    parent,
                    false
                )
                PreviewViewHolder(view, callback)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_preview_show_more,
                    parent,
                    false
                )
                PreviewShowMoreViewHolder(view, callback)
            }
        }
    }

    override fun getItemCount(): Int = previewUrlList.size

    override fun getItemViewType(position: Int): Int = when {
        isMaximumItemExceeded && position == MAX_ITEM_COUNT - 1 -> ITEM_SHOW_MORE
        else -> ITEM_PREVIEW
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val zigzagPosition = getDisplayPositionByZigzag(position)
        when (getItemViewType(position)) {
            ITEM_PREVIEW -> {
                (holder as PreviewViewHolder)
                    .setData(previewUrlList[zigzagPosition], zigzagPosition)
            }
            else -> {
                val pagesLeft = previewUrlList.size - MAX_ITEM_COUNT
                (holder as PreviewShowMoreViewHolder)
                    .setData(previewUrlList[zigzagPosition], zigzagPosition, pagesLeft)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        Logger.d(TAG, "View is recycled")
        when (holder) {
            is PreviewViewHolder -> {
                ImageUtils.clear(holder.ivPageThumbnail)
            }
            is PreviewShowMoreViewHolder -> {
                ImageUtils.clear(holder.ivPageThumbnail)
            }
        }
    }

    private class PreviewViewHolder(
        itemView: View,
        private val thumbnailClickCallback: ThumbnailClickCallback
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val ivPageThumbnail: ImageView = itemView.findViewById(R.id.ivPageThumbnail)
        private val mtvPageNumber: MyTextView = itemView.findViewById(R.id.mtvPageNumber)
        private val vNavigation: View = itemView.findViewById(R.id.vNavigation)

        private var pageNumber: Int = -1

        init {
            ivPageThumbnail.setOnClickListener(this)
            mtvPageNumber.setOnClickListener(this)
            vNavigation.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            thumbnailClickCallback.onThumbnailClicked(pageNumber)
        }

        fun setData(url: String, pageNumber: Int) {
            this.pageNumber = pageNumber
            mtvPageNumber.text = (pageNumber + 1).toString()
            if (!NHentaiApp.instance.isCensored) {
                ivPageThumbnail.doOnGlobalLayout {
                    ImageUtils.loadFitImage(url, R.drawable.ic_404_not_found, ivPageThumbnail)
                }
            } else {
                ivPageThumbnail.setImageResource(R.drawable.ic_nothing_here_grey)
            }
        }
    }

    private class PreviewShowMoreViewHolder(
        itemView: View,
        private val thumbnailClickCallback: ThumbnailClickCallback
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val ivPageThumbnail: ImageView = itemView.findViewById(R.id.ivPageThumbnail)
        private val mtvMoreItem: MyTextView = itemView.findViewById(R.id.mtvMoreItem)

        private var pageNumber: Int = -1

        init {
            ivPageThumbnail.setOnClickListener(this)
            mtvMoreItem.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            thumbnailClickCallback.onThumbnailClicked(pageNumber)
        }

        @SuppressLint("SetTextI18n")
        fun setData(url: String, pageNumber: Int, pagesLeft: Int) {
            this.pageNumber = pageNumber
            mtvMoreItem.text = "+$pagesLeft"
            if (!NHentaiApp.instance.isCensored) {
                ivPageThumbnail.doOnGlobalLayout {
                    ImageUtils.loadFitImage(url, R.drawable.ic_404_not_found, ivPageThumbnail)
                }
            } else {
                ivPageThumbnail.setImageResource(R.drawable.ic_nothing_here_grey)
            }
        }
    }

    private fun getDisplayPositionByZigzag(position: Int): Int {
        var currentSpanCount = previewUrlList.size / numOfRows
        if (previewUrlList.size % numOfRows != 0) {
            currentSpanCount++
        }

        return if (position < currentSpanCount) {
            position * 2
        } else {
            ((position - currentSpanCount) * 2) + 1
        }
    }

    interface ThumbnailClickCallback {
        fun onThumbnailClicked(page: Int)
    }

    companion object {
        private const val TAG = "PreviewAdapter"
        private const val MAX_ITEM_COUNT = 60

        private const val ITEM_PREVIEW = 1
        private const val ITEM_SHOW_MORE = 2
    }
}
