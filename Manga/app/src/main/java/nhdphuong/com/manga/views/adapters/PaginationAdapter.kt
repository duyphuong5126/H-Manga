package nhdphuong.com.manga.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.R

class PaginationAdapter(
    private var pageCount: Int,
    private var paginationMode: PaginationMode = PaginationMode.NUMBER
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TAG_PREFIXES = Constants.TAG_PREFIXES
        private const val ROUNDED_RECT = 1
        private const val CIRCLE = 2
    }

    private val mPageList: List<Int> = (1..pageCount).toList()
    private var mCurrentPage: Int = if (paginationMode == PaginationMode.NUMBER) mPageList[0] else 0

    var onPageSelectCallback: OnPageSelectCallback? = null
    var onCharacterSelectCallback: OnCharacterSelectCallback? = null

    private var mMaxVisible: Int = 0
    val maxVisible: Int
        get() = mMaxVisible

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutRes = if (viewType == ROUNDED_RECT) {
            R.layout.item_home_pagination_rect
        } else {
            R.layout.item_home_pagination
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return if (paginationMode == PaginationMode.NUMBER) {
            NumberPaginationViewHolder(view)
        } else {
            CharacterPaginationViewHolder(view)
        }
    }

    override fun getItemCount(): Int = pageCount

    override fun getItemViewType(position: Int): Int {
        return if (position >= 100 && paginationMode == PaginationMode.NUMBER) {
            ROUNDED_RECT
        } else {
            CIRCLE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (paginationMode) {
            PaginationMode.CHARACTER -> {
                val characterPaginationViewHolder = holder as CharacterPaginationViewHolder
                characterPaginationViewHolder.setData(position, TAG_PREFIXES[position])
                characterPaginationViewHolder.setPageSelected(position)
                mMaxVisible++
            }
            else -> {
                val numberPaginationViewHolder = holder as NumberPaginationViewHolder
                numberPaginationViewHolder.setData(mPageList[position])
                numberPaginationViewHolder.setPageSelected(mPageList[position])
                mMaxVisible++
            }
        }
    }

    private inner class NumberPaginationViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val mTvPageNumber: TextView = itemView.findViewById(R.id.tvPageNumber)
        private var mPage = -1

        init {
            itemView.setOnClickListener(this)
            mTvPageNumber.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            moveToItem(mPage)
        }

        fun setData(pageNumber: Int) {
            mPage = pageNumber
            mTvPageNumber.text = mPage.toString()
        }

        fun setPageSelected(pageSelected: Int) {
            val selected = pageSelected == mCurrentPage
            if (selected) {
                val bgRes = if (getItemViewType(pageSelected) == ROUNDED_RECT) {
                    R.drawable.bg_rounded_grey
                } else {
                    R.drawable.bg_circle_grey
                }
                TextViewCompat.setTextAppearance(mTvPageNumber, R.style.PageSelected)
                mTvPageNumber.setBackgroundResource(bgRes)
            } else {
                TextViewCompat.setTextAppearance(mTvPageNumber, R.style.PageNotSelected)
                mTvPageNumber.setBackgroundResource(0)
            }
        }
    }

    private inner class CharacterPaginationViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val mTvPageNumber: TextView = itemView.findViewById(R.id.tvPageNumber)
        private var mPage = -1

        init {
            itemView.setOnClickListener(this)
            mTvPageNumber.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            moveToItem(mPage)
        }

        fun setData(pageNumber: Int, character: Char) {
            mPage = pageNumber
            mTvPageNumber.text = character.toString()
        }

        fun setPageSelected(pageSelected: Int) {
            val selected = pageSelected == mCurrentPage
            if (selected) {
                TextViewCompat.setTextAppearance(mTvPageNumber, R.style.PageSelected)
                mTvPageNumber.setBackgroundResource(R.drawable.bg_circle_grey)
            } else {
                TextViewCompat.setTextAppearance(mTvPageNumber, R.style.PageNotSelected)
                mTvPageNumber.setBackgroundResource(0)
            }
        }
    }

    fun selectFirstPage() {
        val lastSelectedPage = mCurrentPage
        mCurrentPage = 1
        notifyItemChanged(mPageList.indexOf(lastSelectedPage))
        notifyItemChanged(mPageList.indexOf(mCurrentPage))
    }

    fun selectLastPage() {
        val lastSelectedPage = mCurrentPage
        mCurrentPage = mPageList[mPageList.size - 1]
        notifyItemChanged(mPageList.indexOf(lastSelectedPage))
        notifyItemChanged(mPageList.indexOf(mCurrentPage))
    }

    private fun moveToItem(index: Int) {
        val lastSelectedPage = mCurrentPage
        mCurrentPage = index
        if (paginationMode == PaginationMode.NUMBER) {
            onPageSelectCallback?.onPageSelected(index)
            notifyItemChanged(mPageList.indexOf(lastSelectedPage))
            notifyItemChanged(mPageList.indexOf(mCurrentPage))
        } else {
            onCharacterSelectCallback?.onPageSelected(TAG_PREFIXES[index])
            notifyItemChanged(lastSelectedPage)
            notifyItemChanged(mCurrentPage)
        }
    }

    fun jumpToFirst() {
        if (paginationMode == PaginationMode.NUMBER) {
            if (mPageList.isNotEmpty()) {
                moveToItem(mPageList[0])
            }
        } else {
            moveToItem(0)
        }
    }

    fun jumpToLast() {
        if (paginationMode == PaginationMode.NUMBER) {
            if (mPageList.isNotEmpty()) {
                moveToItem(mPageList[mPageList.size - 1])
            }
        } else {
            moveToItem(itemCount - 1)
        }
    }

    interface OnPageSelectCallback {
        fun onPageSelected(page: Int)
    }

    interface OnCharacterSelectCallback {
        fun onPageSelected(character: Char)
    }

    enum class PaginationMode {
        NUMBER,
        CHARACTER
    }
}
