package nhdphuong.com.manga.features.downloading.view

import androidx.recyclerview.widget.DiffUtil
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel.PendingItemUiModel
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel.TotalCountItem

class PendingDownloadItemDiffUtil(
    private val oldPendingList: List<PendingDownloadItemUiModel>,
    private val newPendingList: List<PendingDownloadItemUiModel>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldPendingList.size

    override fun getNewListSize(): Int = newPendingList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldPendingList[oldItemPosition]
        val newItem = newPendingList[newItemPosition]
        return when {
            oldItem is PendingItemUiModel && newItem is PendingItemUiModel -> oldItem.bookId == newItem.bookId
            oldItem is TotalCountItem && newItem is TotalCountItem -> true
            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldPendingList[oldItemPosition]
        val newItem = newPendingList[newItemPosition]
        return when {
            oldItem is PendingItemUiModel && newItem is PendingItemUiModel -> {
                oldItem.bookTitle == newItem.bookTitle &&
                        oldItem.bookId == newItem.bookId
            }
            oldItem is TotalCountItem && newItem is TotalCountItem -> oldItem.total == newItem.total
            else -> false
        }
    }
}