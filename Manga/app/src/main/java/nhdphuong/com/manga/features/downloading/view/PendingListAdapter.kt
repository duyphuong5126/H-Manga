package nhdphuong.com.manga.features.downloading.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.R
import nhdphuong.com.manga.databinding.ItemPendingDownloadBinding
import nhdphuong.com.manga.databinding.ItemPendingItemCountBinding
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel.PendingItemUiModel
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel.TotalCountItem
import nhdphuong.com.manga.views.becomeInvisible
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.customs.MyTextView
import nhdphuong.com.manga.views.gone

class PendingListAdapter(
    context: Context,
    pendingDownloadList: List<PendingDownloadItemUiModel>,
    private val actionListener: PendingListActionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val currentPendingList = arrayListOf<PendingDownloadItemUiModel>()
    private val bookIdTemplate = context.getString(R.string.book_id_template)
    private val totalCountTemplate = context.getString(R.string.total_item_count_template)
    private val oneItemCountText = context.getString(R.string.total_one_item)
    private val downloadingProgressTemplate = context.getString(R.string.preview_download_progress)
    private val downloadingFailureTemplate =
        context.getString(R.string.pending_download_failure_template)

    init {
        currentPendingList.addAll(pendingDownloadList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PENDING_ITEM ->
                PendingItemViewHolder(parent, bookIdTemplate, actionListener::removeBook)
            TOTAL_COUNT_ITEM -> TotalCountItemViewHolder(
                parent,
                totalCountTemplate,
                oneItemCountText
            )
            else -> throw RuntimeException("Unsupported view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PendingItemViewHolder ->
                holder.bindTo(currentPendingList[position] as PendingItemUiModel)

            is TotalCountItemViewHolder ->
                holder.bindTo(currentPendingList[position] as TotalCountItem)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        payloads.forEach { payload ->
            if (holder is PendingItemViewHolder) {
                when (payload) {
                    is DownloadingStarted -> holder.updateStartedStatus()
                    is ProgressMessage -> holder.updateProgress(payload.progressText)
                    is DownloadFailureMessage -> holder.updateFailureMessage(payload.failureMessage)
                    is DownloadingCompleted -> holder.updateCompletionStatus()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return currentPendingList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentPendingList[position]) {
            is PendingItemUiModel -> PENDING_ITEM
            is TotalCountItem -> TOTAL_COUNT_ITEM
        }
    }

    fun updateStartedStatus(position: Int) {
        notifyItemChanged(position, DownloadingStarted())
    }

    fun updateProgress(position: Int, progress: Int, total: Int) {
        val progressMessage =
            ProgressMessage(String.format(downloadingProgressTemplate, progress, total))
        notifyItemChanged(position, progressMessage)
    }

    fun updateCompletion(position: Int) {
        notifyItemChanged(position, DownloadingCompleted())
    }

    fun updateFailureMessage(position: Int, failureCount: Int, total: Int) {
        val failureMessage =
            DownloadFailureMessage(String.format(downloadingFailureTemplate, failureCount, total))
        notifyItemChanged(position, failureMessage)
    }

    fun updateList(newPendingList: List<PendingDownloadItemUiModel>) {
        val diffCallback = PendingDownloadItemDiffUtil(currentPendingList, newPendingList)
        val result = DiffUtil.calculateDiff(diffCallback)
        currentPendingList.clear()
        currentPendingList.addAll(newPendingList)
        result.dispatchUpdatesTo(this)
    }

    private class PendingItemViewHolder(
        parent: ViewGroup,
        private val bookIdTemplate: String,
        onBookRemovedListener: (bookId: String) -> Unit
    ) : RecyclerView.ViewHolder(
        ItemPendingDownloadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    ) {
        private val viewBinding: ItemPendingDownloadBinding =
            ItemPendingDownloadBinding.bind(itemView)

        private val bookTitleTextView: MyTextView get() = viewBinding.bookTitle
        private val bookIdTextView: MyTextView get() = viewBinding.bookId
        private val buttonRemove: ImageButton get() = viewBinding.buttonRemove
        private val downloadingProgress: MyTextView get() = viewBinding.downloadingProgress
        private val completeIndicator: ImageView get() = viewBinding.completeIndicator
        private val downloadingProgressBar: ProgressBar get() = viewBinding.downloadingProgressBar

        private var bookId: String? = null

        init {
            buttonRemove.setOnClickListener {
                bookId?.let(onBookRemovedListener)
            }
        }

        fun bindTo(uiModel: PendingItemUiModel) {
            bookId = uiModel.bookId
            bookTitleTextView.text = uiModel.bookTitle
            bookIdTextView.text = String.format(bookIdTemplate, uiModel.bookId)
        }

        fun updateStartedStatus() {
            buttonRemove.becomeInvisible()
            completeIndicator.gone()
            downloadingProgressBar.becomeVisible()
        }

        fun updateProgress(progressText: String) {
            downloadingProgress.text = progressText
            downloadingProgress.becomeVisible()
            buttonRemove.becomeInvisible()
            completeIndicator.gone()
            downloadingProgressBar.becomeVisible()
        }

        fun updateCompletionStatus() {
            downloadingProgress.gone()
            buttonRemove.becomeInvisible()
            completeIndicator.becomeVisible()
            downloadingProgressBar.gone()
        }

        fun updateFailureMessage(failureMessage: String) {
            downloadingProgress.text = failureMessage
            downloadingProgress.becomeVisible()
            buttonRemove.becomeVisible()
            completeIndicator.gone()
            downloadingProgressBar.gone()
        }
    }

    private class TotalCountItemViewHolder(
        parent: ViewGroup,
        private val totalCountTemplate: String,
        private val oneItemCountText: String
    ) : RecyclerView.ViewHolder(
        ItemPendingItemCountBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    ) {
        private val viewBinding: ItemPendingItemCountBinding =
            ItemPendingItemCountBinding.bind(itemView)

        private val totalTextView: MyTextView = viewBinding.root

        fun bindTo(uiModel: TotalCountItem) {
            val totalText = if (uiModel.total > 1) {
                String.format(totalCountTemplate, uiModel.total)
            } else oneItemCountText
            totalTextView.text = totalText
        }
    }

    private class DownloadingStarted

    private data class ProgressMessage(val progressText: String)

    private data class DownloadFailureMessage(val failureMessage: String)

    private class DownloadingCompleted

    interface PendingListActionListener {
        fun removeBook(bookId: String)
    }

    companion object {
        private const val PENDING_ITEM = 1
        private const val TOTAL_COUNT_ITEM = 2
    }
}