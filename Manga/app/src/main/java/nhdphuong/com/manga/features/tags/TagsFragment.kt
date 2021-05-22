package nhdphuong.com.manga.features.tags

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.data.TagFilter
import nhdphuong.com.manga.data.entity.book.tags.ITag
import nhdphuong.com.manga.features.SearchContract
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.views.adapters.PaginationAdapter
import nhdphuong.com.manga.views.adapters.TagItemAdapter
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.customs.MyButton
import nhdphuong.com.manga.views.customs.MyTextView
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.gone
import kotlin.math.abs

/*
 * Created by nhdphuong on 5/12/18.
 */
class TagsFragment : Fragment(), TagsContract, TagsContract.View, View.OnClickListener {

    private lateinit var mPresenter: TagsContract.Presenter

    private val mCharacterCount = Constants.TAG_PREFIXES.length
    private var mTagCountString = ""

    private lateinit var mCharacterAdapter: PaginationAdapter
    private lateinit var mNumberAdapter: PaginationAdapter
    private lateinit var mTagItemAdapter: TagItemAdapter
    private var mSearchContract: SearchContract? = null

    private lateinit var buttonFirstCharacter: ImageView
    private lateinit var buttonFirstPage: ImageView
    private lateinit var buttonLastCharacter: ImageView
    private lateinit var buttonLastPage: ImageView
    private lateinit var layoutNavigation: ConstraintLayout
    private lateinit var buttonTabAlphabet: MyButton
    private lateinit var buttonTabPopularity: MyButton
    private lateinit var labelCount: MyTextView
    private lateinit var title: MyTextView
    private lateinit var tagLayoutRoot: NestedScrollView
    private lateinit var listAlphabet: RecyclerView
    private lateinit var listPages: RecyclerView
    private lateinit var listTags: RecyclerView

    private val logger = Logger("TagsFragment")

    override fun setPresenter(presenter: TagsContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTagCountString = getString(R.string.tags_count)
        setUpUI(view)
        mCharacterAdapter = PaginationAdapter(
            mCharacterCount,
            PaginationAdapter.PaginationMode.CHARACTER
        )
        mCharacterAdapter.onCharacterSelectCallback =
            object : PaginationAdapter.OnCharacterSelectCallback {
                override fun onPageSelected(character: Char) {
                    logger.d("character=$character")
                    mPresenter.filterByCharacter(character)
                }
            }
        listAlphabet.run {
            adapter = mCharacterAdapter
            becomeVisible()
            val layoutManager = LinearLayoutManager(
                activity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            this.layoutManager = layoutManager
            adapter = mCharacterAdapter

            val updateNavigationButtons = {
                listAlphabet.post {
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val visibilityDistance =
                        abs(lastVisibleItemPosition - firstVisibleItemPosition) + 1
                    if (firstVisibleItemPosition >= 0) {
                        buttonFirstCharacter.becomeVisibleIf(firstVisibleItemPosition > 0 && visibilityDistance < mCharacterCount)
                    }
                    if (lastVisibleItemPosition >= 0) {
                        buttonLastCharacter.becomeVisibleIf(lastVisibleItemPosition < mCharacterCount - 1 && visibilityDistance < mCharacterCount)
                    }
                }
            }
            doOnGlobalLayout {
                updateNavigationButtons.invoke()
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    updateNavigationButtons.invoke()
                }
            })
        }
        buttonTabAlphabet.setOnClickListener(this)
        buttonTabPopularity.setOnClickListener(this)
        buttonFirstCharacter.setOnClickListener(this)
        buttonLastCharacter.setOnClickListener(this)
        buttonFirstPage.setOnClickListener(this)
        buttonLastPage.setOnClickListener(this)

        mPresenter.start()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mb_alphabet -> {
                changeTagFilterType(TagFilter.ALPHABET)
                toggleTabButton(true, buttonTabAlphabet)
                toggleTabButton(false, buttonTabPopularity)
            }

            R.id.mb_popularity -> {
                changeTagFilterType(TagFilter.POPULARITY)
                toggleTabButton(false, buttonTabAlphabet)
                toggleTabButton(true, buttonTabPopularity)
            }

            R.id.btn_first -> {
                mCharacterAdapter.jumpToFirst()
                if (mCharacterAdapter.itemCount > 0) {
                    listAlphabet.scrollToPosition(0)
                }
            }

            R.id.btn_last -> {
                mCharacterAdapter.jumpToLast()
                if (mCharacterAdapter.itemCount > 0) {
                    listAlphabet.scrollToPosition(mCharacterAdapter.itemCount - 1)
                }
            }

            R.id.btn_first_page -> {
                mNumberAdapter.jumpToFirst()
                if (mNumberAdapter.itemCount > 0) {
                    listPages.scrollToPosition(0)
                }
            }

            R.id.btn_last_page -> {
                mNumberAdapter.jumpToLast()
                if (mNumberAdapter.itemCount > 0) {
                    listPages.scrollToPosition(mNumberAdapter.itemCount - 1)
                }
            }
        }
    }

    override fun onTagChange(@Tag tag: String) {
        mPresenter.changeCurrentTag(tag)
    }

    override fun setSearchInputListener(searchContract: SearchContract) {
        mSearchContract = searchContract
    }

    override fun updateTag(tagType: String, tagCount: Int) {
        title.text = tagType
        labelCount.text = String.format(
            mTagCountString,
            SupportUtils.formatBigNumber(tagCount.toLong())
        )
    }

    override fun refreshPages(pageCount: Int) {
        if (pageCount == 0) {
            buttonFirstPage.gone()
            buttonLastPage.gone()
            listPages.gone()
            return
        }
        mNumberAdapter = PaginationAdapter(pageCount)
        mNumberAdapter.onPageSelectCallback = object : PaginationAdapter.OnPageSelectCallback {
            override fun onPageSelected(page: Int) {
                logger.d("Page $page is selected")
                mPresenter.jumpToPage(page)
            }
        }
        listPages.becomeVisible()
        val layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        listPages.layoutManager = layoutManager
        listPages.adapter = mNumberAdapter

        val updateNavigationButtons = {
            listPages.post {
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val visibilityDistance = abs(lastVisibleItemPosition - firstVisibleItemPosition) + 1
                if (firstVisibleItemPosition >= 0) {
                    buttonFirstPage.becomeVisibleIf(firstVisibleItemPosition > 0 && visibilityDistance < pageCount)
                }
                if (lastVisibleItemPosition >= 0) {
                    buttonLastPage.becomeVisibleIf(lastVisibleItemPosition < pageCount - 1 && visibilityDistance < pageCount)
                }
            }
        }
        listPages.doOnGlobalLayout {
            updateNavigationButtons.invoke()
        }
        listPages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateNavigationButtons.invoke()
            }
        })
    }

    override fun setUpTagsList(source: ArrayList<ITag>, tags: List<ITag>) {
        if (!this::mTagItemAdapter.isInitialized) {
            mTagItemAdapter = TagItemAdapter(source, object : TagItemAdapter.OnTagClickListener {
                override fun onTagClick(iTag: ITag) {
                    logger.d("Tag: ${iTag.name}")
                    mSearchContract?.onSearchInputted(iTag.name)
                }
            })
            listTags.apply {
                val linearLayoutManager = object : LinearLayoutManager(context) {
                    override fun isAutoMeasureEnabled(): Boolean {
                        return true
                    }
                }
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                layoutManager = linearLayoutManager
                adapter = mTagItemAdapter
            }
        }
        mTagItemAdapter.submitList(tags)
        toggleTagList(tags.isEmpty())
    }

    override fun refreshTagsList(tags: List<ITag>) {
        mTagItemAdapter.submitList(tags)
        if (mTagItemAdapter.itemCount > 0) {
            tagLayoutRoot.scrollTo(0, 0)
        }
        toggleTagList(tags.isEmpty())
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun isActive(): Boolean = isAdded

    private fun setUpUI(rootView: View) {
        buttonFirstCharacter = rootView.findViewById(R.id.btn_first)
        buttonFirstPage = rootView.findViewById(R.id.btn_first_page)
        buttonLastCharacter = rootView.findViewById(R.id.btn_last)
        buttonLastPage = rootView.findViewById(R.id.btn_last_page)
        layoutNavigation = rootView.findViewById(R.id.cl_alphabet_navigation)
        buttonTabAlphabet = rootView.findViewById(R.id.mb_alphabet)
        buttonTabPopularity = rootView.findViewById(R.id.mb_popularity)
        labelCount = rootView.findViewById(R.id.mtv_count)
        title = rootView.findViewById(R.id.mtv_title)
        tagLayoutRoot = rootView.findViewById(R.id.nsv_container)
        listAlphabet = rootView.findViewById(R.id.rv_alphabet_pagination)
        listPages = rootView.findViewById(R.id.rv_pagination)
        listTags = rootView.findViewById(R.id.rv_tags_list)
    }

    private fun changeTagFilterType(tagFilter: TagFilter) {
        mPresenter.changeTagFilterType(tagFilter)
        layoutNavigation.visibility = if (tagFilter == TagFilter.ALPHABET) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun toggleTabButton(enabled: Boolean, button: MyButton) {
        context?.run {
            if (enabled) {
                button.setBackgroundResource(R.drawable.bg_black_bottom_border_white)
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundResource(android.R.color.transparent)
                button.setTextColor(ContextCompat.getColor(this, R.color.grey767676))
            }
        }
    }

    private fun toggleTagList(isEmpty: Boolean) {
        listPages.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
