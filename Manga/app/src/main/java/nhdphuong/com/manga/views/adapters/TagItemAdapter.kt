package nhdphuong.com.manga.views.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.tags.ITag
import nhdphuong.com.manga.databinding.ItemTagListBinding

class TagItemAdapter(private val mTagList: ArrayList<ITag>, private val mOnTagClickListener: OnTagClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemTagListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return TagViewHolder(binding)
    }

    override fun getItemCount(): Int = mTagList.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val tagViewHolder = viewHolder as TagViewHolder
        tagViewHolder.setData(mTagList[position])
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_tag_list

    fun submitList(tagList: List<ITag>) {
        mTagList.clear()
        mTagList.addAll(tagList)
        notifyDataSetChanged()
    }

    private inner class TagViewHolder(itemTagListBinding: ItemTagListBinding) : RecyclerView.ViewHolder(itemTagListBinding.root), View.OnClickListener {
        private val mtvTagLabel = itemTagListBinding.tvLabel
        private val mtvTagCount = itemTagListBinding.tvCount
        private var mTag: ITag? = null

        init {
            itemTagListBinding.root.setOnClickListener(this)
            mtvTagLabel.setOnClickListener(this)
            mtvTagCount.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            mOnTagClickListener.onTagClick(mTag!!)
        }

        fun setData(tag: ITag) {
            mtvTagLabel.text = tag.name()
            mtvTagCount.text = "${tag.count()}"
            mTag = tag
        }
    }

    interface OnTagClickListener {
        fun onTagClick(iTag: ITag)
    }
}