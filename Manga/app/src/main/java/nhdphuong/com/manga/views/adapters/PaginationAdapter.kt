package nhdphuong.com.manga.views.adapters

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.SupportUtils

class PaginationAdapter(context: Context, pageCount: Int, private val onPageSelectCallback: OnPageSelectCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mPageList: List<Int> = (1..pageCount).toList()
    private var mCurrentPage = mPageList[0]
    private val mDefaultTextSize = SupportUtils.dp2Pixel(context, 30)

    private var mMaxVisible: Int = 0
    val maxVisible: Int
        get() = mMaxVisible

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_pagination, parent, false)
        return HomePaginationViewHolder(view, onPageSelectCallback)
    }

    override fun getItemCount(): Int = mPageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val homePaginationViewHolder = holder as HomePaginationViewHolder
        homePaginationViewHolder.setData(mPageList[position])
        homePaginationViewHolder.setPageSelected(mPageList[position])
        mMaxVisible++
    }

    private inner class HomePaginationViewHolder(itemView: View, private val onPageSelectCallback: OnPageSelectCallback) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val mTvPageNumber: TextView = itemView.findViewById(R.id.tvPageNumber)
        private var mPage = -1

        init {
            itemView.setOnClickListener(this)
            mTvPageNumber.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            onPageSelectCallback.onPageSelected(mPage)
            val lastSelectedPage = mCurrentPage
            mCurrentPage = mPage
            notifyItemChanged(mPageList.indexOf(lastSelectedPage))
            notifyItemChanged(mPageList.indexOf(mCurrentPage))
        }

        fun setData(pageNumber: Int) {
            mPage = pageNumber
            mTvPageNumber.text = mPage.toString()
        }

        fun setPageSelected(pageSelected: Int) {
            val selected = pageSelected == mCurrentPage
            if (selected) {
                TextViewCompat.setTextAppearance(mTvPageNumber, R.style.PageSelected)
                mTvPageNumber.setBackgroundResource(R.drawable.bg_circle_grey_1)
            } else {
                TextViewCompat.setTextAppearance(mTvPageNumber, R.style.PageNotSelected)
                mTvPageNumber.setBackgroundResource(0)
            }

            val size = if (pageSelected >= 100) ConstraintLayout.LayoutParams.WRAP_CONTENT else mDefaultTextSize
            mTvPageNumber.layoutParams.width = size
            mTvPageNumber.layoutParams.height = size
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

    interface OnPageSelectCallback {
        fun onPageSelected(page: Int)
    }
}