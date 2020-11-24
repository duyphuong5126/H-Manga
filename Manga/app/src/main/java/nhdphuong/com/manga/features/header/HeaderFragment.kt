package nhdphuong.com.manga.features.header

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.Tab
import nhdphuong.com.manga.features.RandomContract
import nhdphuong.com.manga.features.SearchContract
import nhdphuong.com.manga.features.about.AboutUsActivity
import nhdphuong.com.manga.features.admin.AdminActivity
import nhdphuong.com.manga.features.downloaded.DownloadedBooksActivity
import nhdphuong.com.manga.features.recent.RecentActivity
import nhdphuong.com.manga.features.tags.TagsActivity
import nhdphuong.com.manga.features.tags.TagsContract
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.views.adapters.TabAdapter
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.showAdminEntryDialog
import nhdphuong.com.manga.views.showInternetRequiredDialog
import nhdphuong.com.manga.views.showTagsDownloadingDialog
import nhdphuong.com.manga.views.showTagsNotAvailable

/*
 * Created by nhdphuong on 4/10/18.
 */
class HeaderFragment : Fragment(), HeaderContract.View, View.OnClickListener {
    companion object {
        private const val TAG_REQUEST_CODE = 10007
        const val ICON_TYPE_CODE = "IconTypeCode"
    }

    private lateinit var presenter: HeaderContract.Presenter
    private lateinit var tabAdapter: TabAdapter
    private var tagChangeListener: TagsContract? = null
    private var searchContract: SearchContract? = null
    private var randomContract: RandomContract? = null

    private var iconType: HeaderIconType = HeaderIconType.Logo

    private var suggestionAdapter: ArrayAdapter<String>? = null

    private lateinit var edtSearch: AutoCompleteTextView
    private lateinit var ibHamburger: ImageButton
    private lateinit var ibSearch: ImageButton
    private lateinit var ibClearSearch: ImageButton
    private lateinit var ibMainLogo: ImageButton
    private lateinit var rvMainTabs: RecyclerView

    override fun setPresenter(presenter: HeaderContract.Presenter) {
        this.presenter = presenter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_header, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.start()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI(view)
        val context: Context = context!!
        iconType = arguments?.run {
            HeaderIconType.fromTypeCode(getInt(ICON_TYPE_CODE, HeaderIconType.Logo.typeCode))
        } ?: HeaderIconType.Logo

        val iconResId = when (iconType) {
            HeaderIconType.Back -> R.drawable.ic_back_white
            HeaderIconType.Logo -> R.drawable.ic_nhentai_logo_main
        }
        ibMainLogo.setImageResource(iconResId)

        tabAdapter = TabAdapter(context, object : TabAdapter.OnMainTabClick {
            override fun onTabClick(tab: Tab) {
                when (tab) {
                    Tab.RECENT,
                    Tab.FAVORITE -> {
                        presenter.processSelectedTab(tab)
                        return
                    }
                    Tab.DOWNLOADED -> {
                        DownloadedBooksActivity.start(context)
                        resetTabBar()
                        return
                    }
                    Tab.ADMIN -> {
                        activity?.showAdminEntryDialog(onOk = {
                            AdminActivity.start(context)
                            resetTabBar()
                        }, onDismiss = {
                            resetTabBar()
                        })
                    }
                    Tab.ARTISTS,
                    Tab.CHARACTERS,
                    Tab.GROUPS,
                    Tab.PARODIES,
                    Tab.TAGS -> {
                        presenter.goToTagsList(tab)
                    }
                    Tab.RANDOM -> {
                        presenter.processSelectedTab(tab)
                    }
                    Tab.INFO -> {
                        AboutUsActivity.start(context)
                        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                    else -> {
                        activity?.showTagsNotAvailable {
                            resetTabBar()
                        }
                    }
                }
            }
        })

        val tabSelector: RecyclerView = rvMainTabs
        tabSelector.adapter = tabAdapter
        tabSelector.addItemDecoration(
            SpaceItemDecoration(context, R.dimen.dp20, true, showLastDivider = true)
        )
        tabSelector.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        ibMainLogo.setOnClickListener(this)
        ibHamburger.setOnClickListener(this)
        ibSearch.setOnClickListener(this)
        ibClearSearch.setOnClickListener(this)

        edtSearch.setOnEditorActionListener { _, actionId, _ ->
            when (actionId and EditorInfo.IME_MASK_ACTION) {
                EditorInfo.IME_ACTION_DONE -> {
                    searchContract?.onSearchInputted(edtSearch.text.toString())
                }
            }
            false
        }
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                ibClearSearch.becomeVisibleIf(s?.isNotBlank() == true)
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    override fun onResume() {
        super.onResume()
        tabAdapter.reset()
        arguments?.let { data ->
            val tabName = data.getString(Constants.TAG_TYPE).orEmpty()
            if (!TextUtils.isEmpty(tabName)) {
                data.remove(Constants.TAG_TYPE)
                val tab = Tab.fromString(tabName)
                tabAdapter.updateTab(tab)
                rvMainTabs.scrollToPosition(tab.ordinal)
            }
        }
        presenter.refreshTagData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        resetTabBar()
        if (resultCode == Activity.RESULT_OK && requestCode == TAG_REQUEST_CODE) {
            val searchData = data?.getStringExtra(Constants.TAG_RESULT).orEmpty()
            searchContract?.onSearchInputted(searchData)
            updateSearchBar(searchData)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ibMainLogo -> {
                edtSearch.setText("")
                searchContract?.onSearchInputted("")
            }

            R.id.ibHamburger -> {
                toggleTagsLayout()
            }

            R.id.ibSearch -> {
                val searchContent = edtSearch.text.toString()
                presenter.saveSearchInfo(searchContent)
                searchContract?.onSearchInputted(searchContent)
            }

            R.id.ibClearSearch -> {
                edtSearch.setText("")
            }
        }
    }

    override fun setTagChangeListener(tagsContract: TagsContract) {
        tagChangeListener = tagsContract
    }

    override fun setSearchInputListener(searchContract: SearchContract) {
        this.searchContract = searchContract
    }

    override fun setRandomContract(randomContract: RandomContract) {
        this.randomContract = randomContract
    }

    override fun updateSearchBar(searchContent: String) {
        edtSearch.setText(searchContent)
        presenter.saveSearchInfo(searchContent)
    }

    override fun showTagsDownloadingPopup() {
        activity?.showTagsDownloadingDialog(this::resetTabBar)
    }

    override fun goToTagsList(tab: Tab) {
        if (tagChangeListener != null) {
            tagChangeListener?.onTagChange(tab.defaultName)
        } else {
            TagsActivity.start(this@HeaderFragment, tab.defaultName, TAG_REQUEST_CODE)
        }
    }

    override fun goToFavoriteList() {
        RecentActivity.start(this@HeaderFragment, Constants.FAVORITE)
    }

    override fun goToRecentList() {
        RecentActivity.start(this@HeaderFragment, Constants.RECENT)
    }

    override fun goToRandomBook() {
        randomContract?.onRandomSelected()
    }

    override fun showNoNetworkPopup() {
        activity?.showInternetRequiredDialog(this::resetTabBar)
    }

    override fun setUpSuggestionList(suggestionList: List<String>) {
        context?.let {
            suggestionAdapter =
                ArrayAdapter(it, android.R.layout.simple_dropdown_item_1line, suggestionList)
            edtSearch.setAdapter(suggestionAdapter)
        }
    }

    override fun updateSuggestionList() {
        suggestionAdapter?.notifyDataSetChanged()
    }

    override fun showLoading() {
    }

    override fun hideLoading() {

    }

    override fun isActive(): Boolean = isAdded

    private fun setUpUI(rootView: View) {
        edtSearch = rootView.findViewById(R.id.edtSearch)
        ibHamburger = rootView.findViewById(R.id.ibHamburger)
        ibSearch = rootView.findViewById(R.id.ibSearch)
        ibClearSearch = rootView.findViewById(R.id.ibClearSearch)
        ibMainLogo = rootView.findViewById(R.id.ibMainLogo)
        rvMainTabs = rootView.findViewById(R.id.rvMainTabs)
    }

    private fun resetTabBar() {
        tabAdapter.reset()
        toggleTagsLayout()
    }

    private fun toggleTagsLayout() {
        rvMainTabs.let { tabSelector ->
            val isTabHidden = tabSelector.visibility == View.GONE
            tabSelector.visibility = if (!isTabHidden) View.GONE else View.VISIBLE
        }
    }
}
