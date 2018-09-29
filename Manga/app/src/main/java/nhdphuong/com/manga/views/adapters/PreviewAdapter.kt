package nhdphuong.com.manga.views.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.GlideUtils
import nhdphuong.com.manga.views.customs.MyTextView

/*
 * Created by nhdphuong on 4/28/18.
 */
class PreviewAdapter(private val mNumOfRows: Int, private val mPreviewUrlList: List<String>,
                     private val callback: ThumbnailClickCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_preview, parent, false)
        return PreviewViewHolder(view, callback)
    }

    override fun getItemCount(): Int = mPreviewUrlList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vhPreview = holder as PreviewViewHolder
        val zigzagPosition = getDisplayPositionByZigzag(position)
        vhPreview.setData(mPreviewUrlList[zigzagPosition], zigzagPosition)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        val previewViewHolder = holder as PreviewViewHolder
        GlideUtils.clear(previewViewHolder.ivPageThumbnail)
    }

    private inner class PreviewViewHolder(itemView: View, private val thumbnailClickCallback: ThumbnailClickCallback)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {
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
            GlideUtils.loadOriginalImage(url, R.drawable.ic_404_not_found, ivPageThumbnail)
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