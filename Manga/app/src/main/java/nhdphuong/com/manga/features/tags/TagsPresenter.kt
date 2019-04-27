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
class TagsPresenter @Inject constructor(
        private val mView: TagsContract.View,
        private val mTagRepository: TagRepository,
        @Tag private var mTagType: String,
        @IO private val io: CoroutineScope,
        @Main private val main: CoroutineScope
) : TagsContract.Presenter {
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
            val tagList: List<ITag> = when (mTagType) {
                Constants.TAGS -> {
                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mTagRepository.getTagsBySpecialCharactersPrefixAscending(
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            } else {
                                mTagRepository.getTagsByPrefixAscending(
                                        mCurrentPrefixChar,
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            mTagRepository.getTagsByPopularityDescending(
                                    TAGS_PER_PAGE,
                                    mCurrentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.ARTISTS -> {
                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mTagRepository.getArtistsBySpecialCharactersPrefixAscending(
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            } else {
                                mTagRepository.getArtistsByPrefixAscending(
                                        mCurrentPrefixChar,
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            mTagRepository.getArtistsByPopularityDescending(
                                    TAGS_PER_PAGE,
                                    mCurrentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.CHARACTERS -> {
                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mTagRepository.getCharactersBySpecialCharactersPrefixAscending(
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            } else {
                                mTagRepository.getCharactersByPrefixAscending(
                                        mCurrentPrefixChar,
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            mTagRepository.getCharactersByPopularityDescending(
                                    TAGS_PER_PAGE,
                                    mCurrentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.GROUPS -> {
                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mTagRepository.getGroupsBySpecialCharactersPrefixAscending(
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            } else {
                                mTagRepository.getGroupsByPrefixAscending(
                                        mCurrentPrefixChar,
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            mTagRepository.getGroupsByPopularityDescending(
                                    TAGS_PER_PAGE,
                                    mCurrentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.PARODIES -> {
                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mTagRepository.getParodiesBySpecialCharactersPrefixAscending(
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            } else {
                                mTagRepository.getParodiesByPrefixAscending(
                                        mCurrentPrefixChar,
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            mTagRepository.getParodiesByPopularityDescending(
                                    TAGS_PER_PAGE,
                                    mCurrentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                else -> {
                    emptyList()
                }
            }
            Logger.d(TAG, "mCurrentPrefixChar=$mCurrentPrefixChar," +
                    " mCurrentFilteredTagsCount=$mCurrentFilteredTagsCount, tagList=$tagList")
            main.launch {
                mView.refreshTagsList(tagList)
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
            val tagList: List<ITag> = when (mTagType) {
                Constants.TAGS -> {
                    mCurrentTagsCount = mTagRepository.getTagCount()

                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mCurrentFilteredTagsCount =
                                        mTagRepository.getTagCountBySpecialCharactersPrefix()
                                updatePagination()
                                mTagRepository.getTagsBySpecialCharactersPrefixAscending(
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            } else {
                                mCurrentFilteredTagsCount = mTagRepository.getTagsCountByPrefix(
                                        mCurrentPrefixChar
                                )
                                updatePagination()
                                mTagRepository.getTagsByPrefixAscending(
                                        mCurrentPrefixChar,
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            mCurrentFilteredTagsCount = mCurrentTagsCount
                            updatePagination()
                            mTagRepository.getTagsByPopularityDescending(
                                    TAGS_PER_PAGE,
                                    mCurrentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.ARTISTS -> {
                    mCurrentTagsCount = mTagRepository.getArtistsCount()

                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mCurrentFilteredTagsCount =
                                        mTagRepository.getArtistsCountBySpecialCharactersPrefix()
                                updatePagination()
                                mTagRepository.getArtistsBySpecialCharactersPrefixAscending(
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            } else {
                                mCurrentFilteredTagsCount = mTagRepository.getArtistsCountByPrefix(
                                        mCurrentPrefixChar
                                )
                                updatePagination()
                                mTagRepository.getArtistsByPrefixAscending(
                                        mCurrentPrefixChar,
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            mCurrentFilteredTagsCount = mCurrentTagsCount
                            updatePagination()
                            mTagRepository.getArtistsByPopularityDescending(
                                    TAGS_PER_PAGE,
                                    mCurrentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.CHARACTERS -> {
                    mCurrentTagsCount = mTagRepository.getCharactersCount()

                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mCurrentFilteredTagsCount =
                                        mTagRepository.getCharactersCountBySpecialCharactersPrefix()
                                updatePagination()
                                mTagRepository.getCharactersBySpecialCharactersPrefixAscending(
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            } else {
                                mCurrentFilteredTagsCount =
                                        mTagRepository.getCharactersCountByPrefix(
                                                mCurrentPrefixChar
                                        )
                                updatePagination()
                                mTagRepository.getCharactersByPrefixAscending(
                                        mCurrentPrefixChar,
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            mCurrentFilteredTagsCount = mCurrentTagsCount
                            updatePagination()
                            mTagRepository.getCharactersByPopularityDescending(
                                    TAGS_PER_PAGE,
                                    mCurrentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.GROUPS -> {
                    mCurrentTagsCount = mTagRepository.getGroupsCount()

                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mCurrentFilteredTagsCount =
                                        mTagRepository.getGroupsCountBySpecialCharactersPrefix()
                                updatePagination()
                                mTagRepository.getGroupsBySpecialCharactersPrefixAscending(
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            } else {
                                mCurrentFilteredTagsCount =
                                        mTagRepository.getGroupsCountByPrefix(mCurrentPrefixChar)
                                updatePagination()
                                mTagRepository.getGroupsByPrefixAscending(
                                        mCurrentPrefixChar,
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            mCurrentFilteredTagsCount = mCurrentTagsCount
                            updatePagination()
                            mTagRepository.getGroupsByPopularityDescending(
                                    TAGS_PER_PAGE,
                                    mCurrentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.PARODIES -> {
                    mCurrentTagsCount = mTagRepository.getParodiesCount()

                    when (mTagFilter) {
                        TagFilter.ALPHABET -> {
                            if (mCurrentPrefixChar == TAG_PREFIXES[0]) {
                                mCurrentFilteredTagsCount =
                                        mTagRepository.getParodiesCountBySpecialCharactersPrefix()
                                updatePagination()
                                mTagRepository.getParodiesBySpecialCharactersPrefixAscending(
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            } else {
                                mCurrentFilteredTagsCount =
                                        mTagRepository.getParodiesCountByPrefix(mCurrentPrefixChar)
                                updatePagination()
                                mTagRepository.getParodiesByPrefixAscending(
                                        mCurrentPrefixChar,
                                        TAGS_PER_PAGE,
                                        mCurrentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            mCurrentFilteredTagsCount = mCurrentTagsCount
                            updatePagination()
                            mTagRepository.getParodiesByPopularityDescending(
                                    TAGS_PER_PAGE,
                                    mCurrentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                else -> {
                    emptyList()
                }
            }
            Logger.d(TAG, "mCurrentPrefixChar=$mCurrentPrefixChar," +
                    " mCurrentFilteredTagsCount=$mCurrentFilteredTagsCount," +
                    " tagList=$tagList")
            main.launch {
                mView.setUpTagsList(mTagsList, tagList)
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
