package nhdphuong.com.manga.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_tag_list.view.tvCount
import kotlinx.android.synthetic.main.item_tag_list.view.tvLabel
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.tags.ITag

class TagItemAdapter(
    private val mTagList: ArrayList<ITag>,
    private val mOnTagClickListener: OnTagClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_tag_list, viewGroup, false)
        return TagViewHolder(view)
    }

    override fun getItemCount(): Int = mTagList.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val tagViewHolder = viewHolder as TagViewHolder
        tagViewHolder.setData(mTagList[position])
    }

    override fun getItemViewType(position: Int): Int = TAG_ITEM_TYPE

    fun submitList(tagList: List<ITag>) {
        mTagList.clear()
        mTagList.addAll(tagList)
        notifyDataSetChanged()
    }

    private inner class TagViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val mtvTagLabel = itemView.tvLabel
        private val mtvTagCount = itemView.tvCount
        private var mTag: ITag? = null

        init {
            itemView.setOnClickListener(this)
            mtvTagLabel.setOnClickListener(this)
            mtvTagCount.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            mOnTagClickListener.onTagClick(mTag!!)
        }

        fun setData(tag: ITag) {
            mtvTagLabel.text = tag.name
            mtvTagCount.text = "${tag.count}"
            mTag = tag
        }
    }

    interface OnTagClickListener {
        fun onTagClick(iTag: ITag)
    }

    companion object {
        private const val TAG_ITEM_TYPE = 1
    }
}
