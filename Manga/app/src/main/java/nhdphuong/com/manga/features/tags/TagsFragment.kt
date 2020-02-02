package nhdphuong.com.manga.features.tags

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_tags.btn_first
import kotlinx.android.synthetic.main.fragment_tags.btn_first_page
import kotlinx.android.synthetic.main.fragment_tags.btn_last
import kotlinx.android.synthetic.main.fragment_tags.btn_last_page
import kotlinx.android.synthetic.main.fragment_tags.cl_alphabet_navigation
import kotlinx.android.synthetic.main.fragment_tags.ib_back
import kotlinx.android.synthetic.main.fragment_tags.mb_alphabet
import kotlinx.android.synthetic.main.fragment_tags.mb_popularity
import kotlinx.android.synthetic.main.fragment_tags.mtv_count
import kotlinx.android.synthetic.main.fragment_tags.mtv_title
import kotlinx.android.synthetic.main.fragment_tags.nsv_container
import kotlinx.android.synthetic.main.fragment_tags.rv_alphabet_pagination
import kotlinx.android.synthetic.main.fragment_tags.rv_pagination
import kotlinx.android.synthetic.main.fragment_tags.rv_tags_list
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.data.TagFilter
import nhdphuong.com.manga.data.entity.book.tags.ITag
import nhdphuong.com.manga.features.SearchContract
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.views.adapters.PaginationAdapter
import nhdphuong.com.manga.views.adapters.TagItemAdapter
import nhdphuong.com.manga.views.customs.MyButton

/*
 * Created by nhdphuong on 5/12/18.
 */
class TagsFragment : Fragment(), TagsContract, TagsContract.View {
    companion object {
        private const val TAG = "TagsFragment"
    }

    private lateinit var mPresenter: TagsContract.Presenter

    private val mCharacterCount = Constants.TAG_PREFIXES.length
    private val mTagCountString = NHentaiApp.instance.getString(R.string.tags_count)

    private lateinit var mCharacterAdapter: PaginationAdapter
    private lateinit var mNumberAdapter: PaginationAdapter
    private lateinit var mTagItemAdapter: TagItemAdapter
    private var mSearchContract: SearchContract? = null

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
        mCharacterAdapter = PaginationAdapter(
            mCharacterCount,
            PaginationAdapter.PaginationMode.CHARACTER
        )
        mCharacterAdapter.onCharacterSelectCallback =
            object : PaginationAdapter.OnCharacterSelectCallback {
                override fun onPageSelected(character: Char) {
                    Logger.d(TAG, "character=$character")
                    mPresenter.filterByCharacter(character)
                }
            }
        rv_alphabet_pagination.run {
            adapter = mCharacterAdapter
            visibility = View.VISIBLE
            layoutManager = LinearLayoutManager(
                activity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = mCharacterAdapter
        }
        mb_alphabet.setOnClickListener {
            changeTagFilterType(TagFilter.ALPHABET)
            toggleTabButton(true, mb_alphabet)
            toggleTabButton(false, mb_popularity)
        }
        mb_popularity.setOnClickListener {
            changeTagFilterType(TagFilter.POPULARITY)
            toggleTabButton(false, mb_alphabet)
            toggleTabButton(true, mb_popularity)
        }
        btn_first.setOnClickListener {
            mCharacterAdapter.jumpToFirst()
            if (mCharacterAdapter.itemCount > 0) {
                rv_alphabet_pagination.scrollToPosition(0)
            }
        }
        btn_last.setOnClickListener {
            mCharacterAdapter.jumpToLast()
            if (mCharacterAdapter.itemCount > 0) {
                rv_alphabet_pagination.scrollToPosition(mCharacterAdapter.itemCount - 1)
            }
        }
        btn_first_page.setOnClickListener {
            mNumberAdapter.jumpToFirst()
            if (mNumberAdapter.itemCount > 0) {
                rv_pagination.scrollToPosition(0)
            }
        }
        btn_last_page.setOnClickListener {
            mNumberAdapter.jumpToLast()
            if (mNumberAdapter.itemCount > 0) {
                rv_pagination.scrollToPosition(mNumberAdapter.itemCount - 1)
            }
        }
        ib_back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.start()
    }

    override fun onTagChange(@Tag tag: String) {
        mPresenter.changeCurrentTag(tag)
    }

    override fun setSearchInputListener(searchContract: SearchContract) {
        mSearchContract = searchContract
    }

    override fun updateTag(tagType: String, tagCount: Int) {
        mtv_title.text = tagType
        mtv_count.text = String.format(
            mTagCountString,
            SupportUtils.formatBigNumber(tagCount.toLong())
        )
    }

    override fun refreshPages(pagesCount: Int) {
        if (pagesCount == 0) {
            btn_first_page.visibility = View.GONE
            btn_last_page.visibility = View.GONE
            rv_pagination.visibility = View.GONE
            return
        }
        mNumberAdapter = PaginationAdapter(pagesCount)
        mNumberAdapter.onPageSelectCallback = object : PaginationAdapter.OnPageSelectCallback {
            override fun onPageSelected(page: Int) {
                Logger.d(TAG, "Page $page is selected")
                mPresenter.jumpToPage(page)
            }
        }
        rv_pagination.visibility = View.VISIBLE
        rv_pagination.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rv_pagination.adapter = mNumberAdapter
    }

    override fun setUpTagsList(source: ArrayList<ITag>, tags: List<ITag>) {
        if (!this::mTagItemAdapter.isInitialized) {
            mTagItemAdapter = TagItemAdapter(source, object : TagItemAdapter.OnTagClickListener {
                override fun onTagClick(iTag: ITag) {
                    Logger.d(TAG, "Tag: ${iTag.name()}")
                    mSearchContract?.onSearchInputted(iTag.name())
                }
            })
            rv_tags_list.apply {
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
            nsv_container.scrollTo(0, 0)
        }
        toggleTagList(tags.isEmpty())
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun isActive(): Boolean = isAdded

    private fun changeTagFilterType(tagFilter: TagFilter) {
        mPresenter.changeTagFilterType(tagFilter)
        cl_alphabet_navigation.visibility = if (tagFilter == TagFilter.ALPHABET) {
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
        rv_pagination.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
