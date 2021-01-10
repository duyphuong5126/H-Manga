package nhdphuong.com.manga.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.views.customs.MyTextView

class ReaderNavigationAdapter(
    private val pageUrlList: List<String>,
    private val thumbnailSelectListener: ThumbnailSelectListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SELECTED_THUMBNAIL -> SelectedThumbnailItem(
                layoutInflater.inflate(R.layout.item_selected_thumbnail, parent, false)
            )

            else -> NormalThumbnailItem(
                layoutInflater.inflate(R.layout.item_normal_thumbnail, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NormalThumbnailItem -> {
                holder.bindTo(pageUrlList[position], position)
            }

            is SelectedThumbnailItem -> {
                holder.bindTo(pageUrlList[position], position)
            }
        }
    }

    override fun getItemCount(): Int = pageUrlList.size

    override fun getItemViewType(position: Int): Int {
        return if (selectedPosition == position) SELECTED_THUMBNAIL else NORMAL_THUMBNAIL
    }

    fun updateSelectedIndex(position: Int) {
        if (position != selectedPosition) {
            val lastSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    fun forceNavigate(toPosition: Int) {
        thumbnailSelectListener.onItemSelected(toPosition)
    }

    private inner class NormalThumbnailItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPageThumbnail: ImageView = itemView.findViewById(R.id.ivPageThumbnail)
        private val mtvPageNumber: MyTextView = itemView.findViewById(R.id.mtvPageNumber)
        private val vNavigation: View = itemView.findViewById(R.id.vNavigation)

        init {
            vNavigation.setOnClickListener {
                thumbnailSelectListener.onItemSelected(adapterPosition)
            }
        }

        fun bindTo(thumbnailUrl: String, position: Int) {
            ImageUtils.loadFitImage(thumbnailUrl, R.drawable.ic_404_not_found, ivPageThumbnail)
            mtvPageNumber.text = (position + 1).toString()
        }
    }

    private inner class SelectedThumbnailItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPageThumbnail: ImageView = itemView.findViewById(R.id.ivPageThumbnail)
        private val mtvPageNumber: MyTextView = itemView.findViewById(R.id.mtvPageNumber)
        private val vNavigation: View = itemView.findViewById(R.id.vNavigation)

        init {
            vNavigation.setOnClickListener {
                thumbnailSelectListener.onItemSelected(adapterPosition)
            }
        }

        fun bindTo(thumbnailUrl: String, position: Int) {
            ImageUtils.loadFitImage(thumbnailUrl, R.drawable.ic_404_not_found, ivPageThumbnail)
            mtvPageNumber.text = (position + 1).toString()
        }
    }

    interface ThumbnailSelectListener {
        fun onItemSelected(position: Int)
    }

    companion object {
        private const val NORMAL_THUMBNAIL = 1
        private const val SELECTED_THUMBNAIL = 2
    }
}
