package nhdphuong.com.manga.features.tags

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.data.TagFilter
import nhdphuong.com.manga.data.entity.book.tags.ITag
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
        private const val TAGS_PER_PAGE = 25
    }

    private var mTagFilter = TagFilter.ALPHABET

    private var mCurrentTagsCount = 0
    private var mCurrentFilteredTagsCount = 0
    private var mCurrentPage = 0

    private var mCurrentPrefixChar = TAG_PREFIXES[0]
    private var mTagsList = ArrayList<ITag>()

    init {
        mView.setPresenter(this)
    }

    override fun start() {
        notifyTagsChanged()
    }

    override fun changeCurrentTag(newTag: String) {
        Logger.d(TAG, "Changed from $mTagType to $newTag")
        mTagType = newTag
        notifyTagsChanged()
    }

    override fun changeTagFilterType(tagFilter: TagFilter) {
        mTagFilter = tagFilter
        notifyTagsChanged()
    }

    override fun filterByCharacter(selectedCharacter: Char) {
        mCurrentPrefixChar = selectedCharacter
        notifyTagsChanged()

    }

    override fun jumpToPage(page: Int) {
        mCurrentPage = if (page - 1 > 0) page - 1 else 0
        io.launch {
            when (mTagType) {
                Constants.TAGS -> {
                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            val tagList: List<ITag> = if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mTagRepository.getTagsBySpecialCharactersPrefixAscending(TAGS_PER_PAGE, mCurrentPage * TAGS_PER_PAGE)
                            } else {
                                mTagRepository.getTagsByPrefixAscending(mCurrentPrefixChar, TAGS_PER_PAGE, mCurrentPage * TAGS_PER_PAGE)
                            }
                            Logger.d(TAG, "mCurrentPrefixChar=$mCurrentPrefixChar, " +
                                    "mCurrentFilteredTagsCount=$mCurrentFilteredTagsCount, " +
                                    "tagList=$tagList")
                            main.launch {
                                mView.setUpTagsList(mTagsList, tagList)
                            }
                        }
                        else -> {
                            val tagList: List<ITag> = mTagRepository.getTagsByPopularityDescending(TAGS_PER_PAGE, mCurrentPage * TAGS_PER_PAGE)
                            Logger.d(TAG, "mCurrentFilteredTagsCount=$mCurrentFilteredTagsCount," +
                                    "tagList=$tagList")
                            main.launch {
                                mView.setUpTagsList(mTagsList, tagList)
                            }
                        }
                    }
                }
                else -> {

                }
            }
        }
    }

    override fun stop() {

    }

    /**
     * Usage: used to setup data list, pages count and so on when user
     * - change the tag type (tag, artists, parodies and so on)
     * - change the tag filtering type (alphabet, popularity)
     * - change the character prefix (alphabet mode)
     */
    private fun notifyTagsChanged() {
        mTagsList.clear()
        io.launch {
            when (mTagType) {
                Constants.TAGS -> {
                    mCurrentTagsCount = mTagRepository.getTagCount()

                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            val tagList: List<ITag> = if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mCurrentFilteredTagsCount = mTagRepository.getTagCountBySpecialCharactersPrefix()
                                updatePagination()
                                mTagRepository.getTagsBySpecialCharactersPrefixAscending(TAGS_PER_PAGE, mCurrentPage * TAGS_PER_PAGE)
                            } else {
                                mCurrentFilteredTagsCount = mTagRepository.getTagsCountByPrefix(mCurrentPrefixChar)
                                updatePagination()
                                mTagRepository.getTagsByPrefixAscending(mCurrentPrefixChar, TAGS_PER_PAGE, mCurrentPage * TAGS_PER_PAGE)
                            }
                            Logger.d(TAG, "mCurrentPrefixChar=$mCurrentPrefixChar, " +
                                    "mCurrentFilteredTagsCount=$mCurrentFilteredTagsCount, " +
                                    "tagList=$tagList")
                            main.launch {
                                mView.setUpTagsList(mTagsList, tagList)
                            }
                        }
                        else -> {
                            mCurrentFilteredTagsCount = mCurrentTagsCount
                            updatePagination()
                            val tagList: List<ITag> = mTagRepository.getTagsByPopularityDescending(TAGS_PER_PAGE, mCurrentPage * TAGS_PER_PAGE)
                            Logger.d(TAG, "mCurrentFilteredTagsCount=$mCurrentFilteredTagsCount," +
                                    "tagList=$tagList")
                            main.launch {
                                mView.setUpTagsList(mTagsList, tagList)
                            }
                        }
                    }
                }
                else -> {

                }
            }

            main.launch {
                mView.updateTag(mTagType, mCurrentTagsCount)
            }
        }
    }

    private fun updatePagination() {
        mCurrentPage = 0
        var pagesCount = mCurrentFilteredTagsCount / TAGS_PER_PAGE
        if (mCurrentFilteredTagsCount % TAGS_PER_PAGE > 0) {
            pagesCount++
        }
        main.launch {
            mView.refreshPages(pagesCount)
        }
    }
}