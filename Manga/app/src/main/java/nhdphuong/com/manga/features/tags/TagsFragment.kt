package nhdphuong.com.manga.features.tags

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.databinding.FragmentTagsBinding

/*
 * Created by nhdphuong on 5/12/18.
 */
class TagsFragment : Fragment(), TagsContract, TagsContract.View {
    private lateinit var mPresenter: TagsContract.Presenter
    private lateinit var mBinding: FragmentTagsBinding
    override fun setPresenter(presenter: TagsContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tags, container, false)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.start()
    }

    override fun onTagChange(@Tag tag: String) {
        mPresenter.changeCurrentTag(tag)
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun isActive(): Boolean = isAdded
}