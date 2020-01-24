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
    private val mOnMainTabClick: OnMainTabClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mTabList: LinkedList<Tab> = LinkedList()
    private var mCurrentTab: Tab = Tab.NONE
    private val mEnableTextColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private val mDisableTextColor = ContextCompat.getColor(context, R.color.greyBBB)
    private val isDebugVersion: Boolean =
        (NHentaiApp.instance.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private val mRecentTitle = context.getString(R.string.recent)
    private val mFavoriteTitle = context.getString(R.string.favorite)
    private val mRandomTitle = context.getString(R.string.random)
    private val mArtistsTitle = context.getString(R.string.artists)
    private val mTagsTitle = context.getString(R.string.tags)
    private val mCharacterTitle = context.getString(R.string.characters)
    private val mGroupsTitle = context.getString(R.string.groups)
    private val mParodiesTitle = context.getString(R.string.parodies)
    private val mInfoTitle = context.getString(R.string.info)
    private val mAdminTitle = context.getString(R.string.admin)

    init {
        mTabList.clear()
        for (tab in Tab.values()) {
            if (tab != Tab.NONE) {
                if (tab == Tab.ADMIN) {
                    if (isDebugVersion) {
                        mTabList.add(tab)
                    }
                } else {
                    mTabList.add(tab)
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
                Tab.RECENT -> mRecentTitle
                Tab.FAVORITE -> mFavoriteTitle
                Tab.RANDOM -> mRandomTitle
                Tab.ARTISTS -> mArtistsTitle
                Tab.TAGS -> mTagsTitle
                Tab.CHARACTERS -> mCharacterTitle
                Tab.GROUPS -> mGroupsTitle
                Tab.PARODIES -> mParodiesTitle
                Tab.INFO -> mInfoTitle
                Tab.ADMIN -> mAdminTitle
                else -> tab.defaultName
            }
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
