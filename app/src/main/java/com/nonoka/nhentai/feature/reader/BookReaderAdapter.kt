package com.nonoka.nhentai.feature.reader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nonoka.nhentai.R
import com.nonoka.nhentai.databinding.ItemBookPageWrappedBinding
import timber.log.Timber

/*
 * Created by nhdphuong on 5/5/18.
 */
class BookReaderAdapter(
    private val pageList: List<ReaderPageModel>,
    private val onTap: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WrapContentPageViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WrapContentPageViewHolder -> holder.bindTo(pageList[position], position + 1)
        }
    }

    override fun getItemCount(): Int {
        return pageList.size
    }

    fun resetPage(page: Int) {
        notifyItemChanged(page)
    }

    private class WrapContentPageViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_book_page_wrapped, parent, false)
    ) {
        private val viewBinding = ItemBookPageWrappedBinding.bind(itemView)

        fun bindTo(page: ReaderPageModel, pageIndex: Int) {
            Timber.d("Ratio: ${page.width}:${page.height}")
            viewBinding.pageTitle.text = pageIndex.toString()
            viewBinding.pageTitle.visibility = View.VISIBLE
            viewBinding.pageImage.load(page.pageUrl) {
                listener(
                    onSuccess = { _, _ ->
                        viewBinding.pageTitle.visibility = View.GONE
                    },
                )
            }
        }
    }
}
