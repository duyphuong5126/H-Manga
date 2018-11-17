package nhdphuong.com.manga.features.tags

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.data.TagFilter
import nhdphuong.com.manga.data.repository.TagRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import javax.inject.Inject

/*
 * Created by nhdphuong on 5/12/18.
 */
class TagsPresenter @Inject constructor(private val mView: TagsContract.View,
                                        private val mTagRepository: TagRepository,
                                        @Tag private var mTagType: String,
                                        @IO private val io: CoroutineScope,
                                        @Main private val main: CoroutineScope) : TagsContract.Presenter {
    companion object {
        private const val TAG = "TagsPresenter"

        private const val TAG_PREFIXES = Constants.TAG_PREFIXES
    }

    private var mTagFilter = TagFilter.ALPHABET

    private var mCurrentTagsCount = 0
    private var mCurrentFilteredTagsCount = 0

    private var mCurrentFirstChar = TAG_PREFIXES[0]

    init {
        mView.setPresenter(this)
    }

    override fun start() {
        notifyTagChanged(mTagType)
    }

    override fun changeCurrentTag(newTag: String) {
        Logger.d(TAG, "Changed from $mTagType to $newTag")
        mTagType = newTag
        notifyTagChanged(mTagType)
    }

    override fun changeTagFilterType(tagFilter: TagFilter) {
        mTagFilter = tagFilter
    }

    override fun filterByCharacter(selectedCharacter: Char) {

    }

    override fun stop() {

    }

    private fun notifyTagChanged(@Tag tagType: String) {
        io.launch {
            when (tagType) {
                Constants.TAGS -> {
                    io.launch {
                        mCurrentTagsCount = mTagRepository.getTagCount()

                        when (mTagFilter) {
                            TagFilter.ALPHABET -> {

                            }
                            else -> {

                            }
                        }
                    }
                }
                else -> {

                }
            }

            main.launch {
                mView.updateTag(tagType, mCurrentTagsCount)
            }
        }
    }
}