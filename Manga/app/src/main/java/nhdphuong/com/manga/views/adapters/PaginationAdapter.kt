package nhdphuong.com.manga.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.SupportUtils

class PaginationAdapter(
    context: Context,
    private var pageCount: Int,
    private var paginationMode: PaginationMode = PaginationMode.NUMBER
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TAG_PREFIXES = Constants.TAG_PREFIXES
    }

    private val mPageList: List<Int> = (1..pageCount).toList()
    private var mCurrentPage: Int = if (paginationMode == PaginationMode.NUMBER) mPageList[0] else 0
    private val mDefaultTextSize = SupportUtils.dp2Pixel(context, 30)

    var onPageSelectCallback: OnPageSelectCallback? = null
    var onCharacterSelectCallback: OnCharacterSelectCallback? = null

    private var mMaxVisible: Int = 0
    val maxVisible: Int
        get() = mMaxVisible

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_home_pagination,
            parent,
            false
        )
        return if (paginationMode == PaginationMode.NUMBER) {
            NumberPaginationViewHolder(view)
        } else {
            CharacterPaginationViewHolder(view)
        }
    }

    override fun getItemCount(): Int = pageCount

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
                TextViewCompat.setTextAppearance(mTvPageNumber, R.style.PageSelected)
                mTvPageNumber.setBackgroundResource(R.drawable.bg_circle_grey)
            } else {
                TextViewCompat.setTextAppearance(mTvPageNumber, R.style.PageNotSelected)
                mTvPageNumber.setBackgroundResource(0)
            }

            val size = if (pageSelected >= 100) {
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            } else {
                mDefaultTextSize
            }
            mTvPageNumber.layoutParams.width = size
            mTvPageNumber.layoutParams.height = size
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
