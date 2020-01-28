package nhdphuong.com.manga.views.adapters

import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_tab.view.tabClickArea
import kotlinx.android.synthetic.main.item_tab.view.tvTabLabel
import kotlinx.android.synthetic.main.item_tab.view.vTabIndicator
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.Tab
import java.util.LinkedList

/*
 * Created by nhdphuong on 3/17/18.
 */
class TabAdapter(
    context: Context,
    private val onMainTabClick: OnMainTabClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var tabList: LinkedList<Tab> = LinkedList()
    private var currentTab: Tab = Tab.NONE
    private val enableTextColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private val disableTextColor = ContextCompat.getColor(context, R.color.greyBBB)
    private val isDebugVersion: Boolean =
        (NHentaiApp.instance.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private val recentTitle = context.getString(R.string.recent)
    private val favoriteTitle = context.getString(R.string.favorite)
    private val downloadedTitle = context.getString(R.string.downloaded)
    private val randomTitle = context.getString(R.string.random)
    private val artistsTitle = context.getString(R.string.artists)
    private val tagsTitle = context.getString(R.string.tags)
    private val characterTitle = context.getString(R.string.characters)
    private val groupsTitle = context.getString(R.string.groups)
    private val parodiesTitle = context.getString(R.string.parodies)
    private val infoTitle = context.getString(R.string.info)
    private val adminTitle = context.getString(R.string.admin)

    init {
        tabList.clear()
        for (tab in Tab.values()) {
            if (tab != Tab.NONE) {
                if (tab == Tab.ADMIN) {
                    if (isDebugVersion) {
                        tabList.add(tab)
                    }
                } else {
                    tabList.add(tab)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_tab,
            parent,
            false
        )
        return MainTabViewHolder(view, onMainTabClick)
    }

    override fun getItemCount(): Int = tabList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tabViewHolder = holder as MainTabViewHolder
        tabList[position].let { tab ->
            tabViewHolder.setTab(tab)
            tabViewHolder.toggleTab(tab == currentTab && currentTab != Tab.NONE)
        }
    }

    private inner class MainTabViewHolder(
        itemView: View,
        private val mOnMainTabClick: OnMainTabClick
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val mTvLabel = itemView.tvTabLabel
        private val mTabIndicator = itemView.vTabIndicator
        private val mTabClickArea = itemView.tabClickArea
        private lateinit var mTab: Tab

        init {
            mTabClickArea.setOnClickListener { this@MainTabViewHolder.onClick(it) }
        }

        override fun onClick(p0: View?) {
            mOnMainTabClick.onTabClick(mTab)
            updateTab(mTab)
        }

        fun setTab(tab: Tab) {
            mTab = tab
            mTvLabel.text = when (tab) {
                Tab.RECENT -> recentTitle
                Tab.FAVORITE -> favoriteTitle
                Tab.DOWNLOADED -> downloadedTitle
                Tab.RANDOM -> randomTitle
                Tab.ARTISTS -> artistsTitle
                Tab.TAGS -> tagsTitle
                Tab.CHARACTERS -> characterTitle
                Tab.GROUPS -> groupsTitle
                Tab.PARODIES -> parodiesTitle
                Tab.INFO -> infoTitle
                Tab.ADMIN -> adminTitle
                else -> tab.defaultName
            }
        }

        fun toggleTab(selected: Boolean) {
            mTabIndicator.visibility = if (selected) View.VISIBLE else View.INVISIBLE
            val textColor = if (selected) enableTextColor else disableTextColor
            mTvLabel.setTextColor(textColor)
        }
    }

    fun updateTab(tab: Tab) {
        val oldActiveTab = currentTab
        currentTab = tab
        if (tab == Tab.NONE) {
            notifyDataSetChanged()
        } else {
            notifyItemChanged(currentTab.ordinal)
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
