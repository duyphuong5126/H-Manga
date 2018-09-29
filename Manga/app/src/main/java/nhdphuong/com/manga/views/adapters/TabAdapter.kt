package nhdphuong.com.manga.views.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.Tab
import java.util.*

/*
 * Created by nhdphuong on 3/17/18.
 */
class TabAdapter(context: Context, private val mOnMainTabClick: OnMainTabClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mTabList: LinkedList<Tab> = LinkedList()
    private var mCurrentTab: Tab = Tab.NONE
    private val mEnableTextColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private val mDisableTextColor = ContextCompat.getColor(context, R.color.greyBBB)

    init {
        mTabList.clear()
        for (tab in Tab.values()) {
            if (tab != Tab.NONE) {
                mTabList.add(tab)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tab, parent, false)
        return MainTabViewHolder(view, mOnMainTabClick)
    }

    override fun getItemCount(): Int = mTabList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tabViewHolder = holder as MainTabViewHolder
        mTabList[position].let { tab ->
            tabViewHolder.setTab(tab)
            tabViewHolder.toggleTab(tab == mCurrentTab && mCurrentTab != Tab.NONE)
        }
    }

    private inner class MainTabViewHolder(itemView: View, private val mOnMainTabClick: OnMainTabClick) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val mTvLabel = itemView.findViewById<TextView>(R.id.tvTabLabel)
        private val mTabIndicator = itemView.findViewById<View>(R.id.vTabIndicator)
        private lateinit var mTab: Tab

        init {
            mTvLabel.setOnClickListener { this@MainTabViewHolder.onClick(it) }
            mTabIndicator.setOnClickListener { this@MainTabViewHolder.onClick(it) }
            itemView.setOnClickListener { this@MainTabViewHolder.onClick(it) }
        }

        override fun onClick(p0: View?) {
            mOnMainTabClick.onTabClick(mTab)
            updateTab(mTab)
        }

        fun setTab(tab: Tab) {
            mTab = tab
            mTvLabel.text = tab.defaultName
        }

        fun toggleTab(selected: Boolean) {
            mTabIndicator.visibility = if (selected) View.VISIBLE else View.INVISIBLE
            val textColor = if (selected) mEnableTextColor else mDisableTextColor
            mTvLabel.setTextColor(textColor)
        }
    }

    fun updateTab(tab: Tab) {
        val oldActiveTab = mCurrentTab
        mCurrentTab = tab
        if (tab == Tab.NONE) {
            notifyDataSetChanged()
        } else {
            notifyItemChanged(mCurrentTab.ordinal)
            notifyItemChanged(oldActiveTab.ordinal)
        }
    }

    fun reset() {
        updateTab(Tab.NONE)
    }

    interface OnMainTabClick {
        fun onTabClick(tab: Tab)
    }
}