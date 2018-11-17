package nhdphuong.com.manga.features.tags

import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.data.TagFilter
import nhdphuong.com.manga.databinding.FragmentTagsBinding
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.views.adapters.PaginationAdapter
import nhdphuong.com.manga.views.customs.MyButton

/*
 * Created by nhdphuong on 5/12/18.
 */
class TagsFragment : Fragment(), TagsContract, TagsContract.View {
    companion object {
        private const val TAG = "TagsFragment"
    }

    private lateinit var mPresenter: TagsContract.Presenter
    private lateinit var mBinding: FragmentTagsBinding

    private val mCharacterCount = Constants.TAG_PREFIXES.length
    private val mTagCountString = NHentaiApp.instance.getString(R.string.tags_count)

    private lateinit var mPaginationAdapter: PaginationAdapter
    override fun setPresenter(presenter: TagsContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tags, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPaginationAdapter = PaginationAdapter(context!!, mCharacterCount, PaginationAdapter.PaginationMode.CHARACTER)
        mPaginationAdapter.onCharacterSelectCallback = object : PaginationAdapter.OnCharacterSelectCallback {
            override fun onPageSelected(character: Char) {
                Logger.d(TAG, "character=$character")
            }
        }
        mBinding.run {
            rvAlphabetPagination.run {
                adapter = mPaginationAdapter
                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                adapter = mPaginationAdapter
            }

            mbAlphabet.setOnClickListener {
                changeTagFilterType(TagFilter.ALPHABET)
                toggleTabButton(true, mbAlphabet)
                toggleTabButton(false, mbPopularity)
            }
            mbPopularity.setOnClickListener {
                changeTagFilterType(TagFilter.POPULARITY)
                toggleTabButton(false, mbAlphabet)
                toggleTabButton(true, mbPopularity)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.start()
    }

    override fun onTagChange(@Tag tag: String) {
        mPresenter.changeCurrentTag(tag)
    }

    override fun updateTag(tagType: String, tagCount: Int) {
        mBinding.mtvTitle.text = tagType
        mBinding.mtvCount.text = String.format(mTagCountString, SupportUtils.formatBigNumber(tagCount.toLong()))
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun isActive(): Boolean = isAdded

    private fun changeTagFilterType(tagFilter: TagFilter) {
        mPresenter.changeTagFilterType(tagFilter)
        mBinding.run {
            rvAlphabetPagination.visibility = if (tagFilter == TagFilter.ALPHABET) View.VISIBLE else View.GONE
            btnFirst.visibility = if (tagFilter == TagFilter.ALPHABET) View.VISIBLE else View.GONE
            btnLast.visibility = if (tagFilter == TagFilter.ALPHABET) View.VISIBLE else View.GONE
        }
    }

    private fun toggleTabButton(enabled: Boolean, button: MyButton) {
        context?.run {
            if (enabled) {
                button.setBackgroundResource(R.drawable.bg_black_bottom_border_white)
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundResource(android.R.color.transparent)
                button.setTextColor(ContextCompat.getColor(this, R.color.grey_1))
            }
        }
    }
}