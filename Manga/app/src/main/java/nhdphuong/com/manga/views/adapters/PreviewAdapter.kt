package nhdphuong.com.manga.views.adapters

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

/*
 * Created by nhdphuong on 4/28/18.
 */
class PreviewAdapter(
    private val mNumOfRows: Int,
    private val mPreviewUrlList: List<String>,
    private val callback: ThumbnailClickCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TAG = "PreviewAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_preview,
            parent,
            false
        )
        return PreviewViewHolder(view, callback)
    }

    override fun getItemCount(): Int {
        Logger.d(TAG, "Item count: ${mPreviewUrlList.size}")
        return mPreviewUrlList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vhPreview = holder as PreviewViewHolder
        val zigzagPosition = getDisplayPositionByZigzag(position)
        vhPreview.setData(mPreviewUrlList[zigzagPosition], zigzagPosition)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        val previewViewHolder = holder as PreviewViewHolder
        ImageUtils.clear(previewViewHolder.ivPageThumbnail)
    }

    private inner class PreviewViewHolder(
        itemView: View,
        private val thumbnailClickCallback: ThumbnailClickCallback
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val ivPageThumbnail: ImageView = itemView.findViewById(R.id.ivPageThumbnail)
        private val mtvPageNumber: MyTextView = itemView.findViewById(R.id.mtvPageNumber)
        private var mPageNumber: Int = -1

        init {
            ivPageThumbnail.setOnClickListener(this)
            mtvPageNumber.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            thumbnailClickCallback.onThumbnailClicked(mPageNumber)
        }

        fun setData(url: String, pageNumber: Int) {
            mPageNumber = pageNumber
            mtvPageNumber.text = (pageNumber + 1).toString()
            if (!NHentaiApp.instance.isCensored) {
                ImageUtils.loadOriginalImage(url, R.drawable.ic_404_not_found, ivPageThumbnail)
            } else {
                ivPageThumbnail.setImageResource(R.drawable.ic_nothing_here_grey)
            }
        }
    }

    private fun getDisplayPositionByZigzag(position: Int): Int {
        var currentSpanCount = mPreviewUrlList.size / mNumOfRows
        if (mPreviewUrlList.size % mNumOfRows != 0) {
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
}
