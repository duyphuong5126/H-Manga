package nhdphuong.com.manga.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
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

    init {
        if (paginationMode == PaginationMode.CHARACTER && pageCount != TAG_PREFIXES.length) {
            pageCount = TAG_PREFIXES.length
        }
    }

    private var mCurrentPage: Int = 0

    var onPageSelectCallback: OnPageSelectCallback? = null
    var onCharacterSelectCallback: OnCharacterSelectCallback? = null

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
            }
            else -> {
                val numberPaginationViewHolder = holder as NumberPaginationViewHolder
                numberPaginationViewHolder.setData(position)
                numberPaginationViewHolder.setPageSelected(position)
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
            mTvPageNumber.text = (mPage + 1).toString()
        }

        fun setPageSelected(pageSelected: Int) {
            val selected = pageSelected == mCurrentPage
            if (selected) {
                val bgRes = if (getItemViewType(pageSelected) == ROUNDED_RECT) {
                    R.drawable.ripple_rounded_grey
                } else {
                    R.drawable.ripple_circle_grey
                }
                TextViewCompat.setTextAppearance(mTvPageNumber, R.style.PageSelected)
                mTvPageNumber.setBackgroundResource(bgRes)
            } else {
                val bgRes = if (getItemViewType(pageSelected) == ROUNDED_RECT) {
                    R.drawable.ripple_rounded_transparent
                } else {
                    R.drawable.ripple_circle_transparent
                }
                TextViewCompat.setTextAppearance(mTvPageNumber, R.style.PageNotSelected)
                mTvPageNumber.setBackgroundResource(bgRes)
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
        mCurrentPage = 0
        notifyItemChanged(lastSelectedPage)
        notifyItemChanged(mCurrentPage)
    }

    fun selectLastPage() {
        val lastSelectedPage = mCurrentPage
        mCurrentPage = pageCount - 1
        notifyItemChanged(lastSelectedPage)
        notifyItemChanged(mCurrentPage)
    }

    private fun moveToItem(index: Int) {
        val lastSelectedPage = mCurrentPage
        mCurrentPage = index
        if (paginationMode == PaginationMode.NUMBER) {
            onPageSelectCallback?.onPageSelected(index)
        } else {
            onCharacterSelectCallback?.onPageSelected(TAG_PREFIXES[index])
        }
        notifyItemChanged(lastSelectedPage)
        notifyItemChanged(mCurrentPage)
    }

    fun jumpToFirst() {
        if (itemCount > 0) {
            moveToItem(0)
        }
    }

    fun jumpToLast() {
        if (itemCount > 0) {
            moveToItem(itemCount - 1)
        }
    }

    fun jumpToIndex(index: Int) {
        Logger.d("", "Move to index: $index")
        if (paginationMode == PaginationMode.NUMBER) {
            if (pageCount > 0 && index in 0 until pageCount) {
                moveToItem(index)
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
