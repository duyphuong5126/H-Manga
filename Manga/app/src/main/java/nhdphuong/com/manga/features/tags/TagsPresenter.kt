package nhdphuong.com.manga.features.tags

import android.util.Log
import nhdphuong.com.manga.data.Tag
import javax.inject.Inject

/*
 * Created by nhdphuong on 5/12/18.
 */
class TagsPresenter @Inject constructor(private val mView: TagsContract.View,
                                        @Tag private var mTagType: String) : TagsContract.Presenter {
    companion object {
        private val TAG = TagsPresenter::class.java.simpleName
    }

    init {
        mView.setPresenter(this)
    }

    override fun start() {

    }

    override fun changeCurrentTag(newTag: String) {
        Log.d(TAG, "Changed from $mTagType to $newTag")
        mTagType = newTag
    }

    override fun stop() {

    }
}