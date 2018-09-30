package nhdphuong.com.manga.features.header

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.Tab
import nhdphuong.com.manga.databinding.FragmentHeaderBinding
import nhdphuong.com.manga.features.home.HomeContract
import nhdphuong.com.manga.features.recent.RecentActivity
import nhdphuong.com.manga.features.tags.TagsContract
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.views.DialogHelper
import nhdphuong.com.manga.views.adapters.TabAdapter

/*
 * Created by nhdphuong on 4/10/18.
 */
class HeaderFragment : Fragment(), HeaderContract.View {
    private lateinit var mPresenter: HeaderContract.Presenter
    private lateinit var mBinding: FragmentHeaderBinding
    private lateinit var mTabAdapter: TabAdapter
    private lateinit var mTagChangeListener: TagsContract
    private lateinit var mHomeContract: HomeContract
    override fun setPresenter(presenter: HeaderContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_header, container, false)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.start()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context: Context = context!!
        val activity = activity!!
        mTabAdapter = TabAdapter(context, object : TabAdapter.OnMainTabClick {
            override fun onTabClick(tab: Tab) {
                when (tab) {
                    Tab.RECENT -> {
                        RecentActivity.start(context, Constants.RECENT)
                        resetTabBar()
                        return
                    }
                    Tab.FAVORITE -> {
                        RecentActivity.start(context, Constants.FAVORITE)
                        resetTabBar()
                        return
                    }
                    Tab.INFO -> {
                        mTabAdapter.reset()
                    }
                    Tab.ADMIN -> {
                        DialogHelper.showAdminEntryDialog(activity, onOk = {
                            resetTabBar()
                        }, onDismiss = {
                            resetTabBar()
                        })
                    }
                    else -> {
                        /*if (::mTagChangeListener.isInitialized) {
                            mTagChangeListener.onTagChange(tab.defaultName)
                        } else {
                            mPresenter.goToTagList(tab.defaultName)
                            mTabAdapter.reset()
                        }*/

                        DialogHelper.showTagsNotAvailable(activity) {
                            resetTabBar()
                        }
                    }
                }
            }
        })

        val tabSelector: RecyclerView = mBinding.rvMainTabs
        tabSelector.adapter = mTabAdapter
        tabSelector.addItemDecoration(SpaceItemDecoration(context, R.dimen.dp20, true, true))
        tabSelector.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        mBinding.ibHamburger.setOnClickListener {
            toggleTagsLayout()
        }
        mBinding.ibSearch.setOnClickListener {
            mHomeContract.onSearchInputted(mBinding.edtSearch.text.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        mTabAdapter.reset()
        arguments?.let { data ->
            val tabName = data.getString(Constants.TAG_TYPE) ?: ""
            if (!TextUtils.isEmpty(tabName)) {
                data.remove(Constants.TAG_TYPE)
                val tab = Tab.fromString(tabName)
                mTabAdapter.updateTab(tab)
                mBinding.rvMainTabs.scrollToPosition(tab.ordinal)
            }
        }
    }

    override fun setTagChangeListener(tagsContract: TagsContract) {
        mTagChangeListener = tagsContract
    }

    override fun setSearchInputListener(homeContract: HomeContract) {
        mHomeContract = homeContract
    }

    override fun updateSearchBar(searchContent: String) {
        mBinding.edtSearch.setText(searchContent)
    }

    override fun showLoading() {
    }

    override fun hideLoading() {

    }

    override fun isActive(): Boolean = isAdded

    private fun resetTabBar() {
        mTabAdapter.reset()
        toggleTagsLayout()
    }

    private fun toggleTagsLayout() {
        mBinding.rvMainTabs.let { tabSelector ->
            val isTabHidden = tabSelector.visibility == View.GONE
            tabSelector.visibility = if (!isTabHidden) View.GONE else View.VISIBLE
        }
    }
}