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
    private val view: TagsContract.View,
    private val tagRepository: TagRepository,
    @Tag private var tagType: String,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : TagsContract.Presenter {
    companion object {
        private const val TAG = "TagsPresenter"

        private const val TAG_PREFIXES = Constants.TAG_PREFIXES
        private const val TAGS_PER_PAGE = 25
    }

    private var tagFilter = TagFilter.ALPHABET

    private var currentTagsCount = 0
    private var currentFilteredTagsCount = 0
    private var currentPage = 0

    private var currentPrefixChar = TAG_PREFIXES[0]
    private var tagsList = ArrayList<ITag>()

    init {
        view.setPresenter(this)
    }

    override fun start() {
        notifyTagsChanged()
    }

    override fun changeCurrentTag(newTag: String) {
        Logger.d(TAG, "Changed from $tagType to $newTag")
        tagType = newTag
        notifyTagsChanged()
    }

    override fun changeTagFilterType(tagFilter: TagFilter) {
        this.tagFilter = tagFilter
        notifyTagsChanged()
    }

    override fun filterByCharacter(selectedCharacter: Char) {
        currentPrefixChar = selectedCharacter
        notifyTagsChanged()

    }

    override fun jumpToPage(page: Int) {
        currentPage = if (page - 1 > 0) page - 1 else 0
        io.launch {
            val tagList: List<ITag> = when (tagType) {
                Constants.TAGS -> {
                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                tagRepository.getTagsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                tagRepository.getTagsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            tagRepository.getTagsByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.ARTISTS -> {
                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                tagRepository.getArtistsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                tagRepository.getArtistsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            tagRepository.getArtistsByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.CHARACTERS -> {
                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                tagRepository.getCharactersBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                tagRepository.getCharactersByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            tagRepository.getCharactersByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.GROUPS -> {
                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                tagRepository.getGroupsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                tagRepository.getGroupsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            tagRepository.getGroupsByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.PARODIES -> {
                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                tagRepository.getParodiesBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                tagRepository.getParodiesByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            tagRepository.getParodiesByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                else -> {
                    emptyList()
                }
            }
            Logger.d(
                TAG, "currentPrefixChar=$currentPrefixChar," +
                        " currentFilteredTagsCount=$currentFilteredTagsCount, tagList=$tagList"
            )
            main.launch {
                view.refreshTagsList(tagList)
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
        tagsList.clear()
        io.launch {
            val tagList: List<ITag> = when (tagType) {
                Constants.TAGS -> {
                    currentTagsCount = tagRepository.getTagCount()

                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                currentFilteredTagsCount =
                                    tagRepository.getTagCountBySpecialCharactersPrefix()
                                updatePagination()
                                tagRepository.getTagsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                currentFilteredTagsCount = tagRepository.getTagsCountByPrefix(
                                    currentPrefixChar
                                )
                                updatePagination()
                                tagRepository.getTagsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            currentFilteredTagsCount = currentTagsCount
                            updatePagination()
                            tagRepository.getTagsByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.ARTISTS -> {
                    currentTagsCount = tagRepository.getArtistsCount()

                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                currentFilteredTagsCount =
                                    tagRepository.getArtistsCountBySpecialCharactersPrefix()
                                updatePagination()
                                tagRepository.getArtistsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                currentFilteredTagsCount = tagRepository.getArtistsCountByPrefix(
                                    currentPrefixChar
                                )
                                updatePagination()
                                tagRepository.getArtistsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            currentFilteredTagsCount = currentTagsCount
                            updatePagination()
                            tagRepository.getArtistsByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.CHARACTERS -> {
                    currentTagsCount = tagRepository.getCharactersCount()

                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                currentFilteredTagsCount =
                                    tagRepository.getCharactersCountBySpecialCharactersPrefix()
                                updatePagination()
                                tagRepository.getCharactersBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                currentFilteredTagsCount =
                                    tagRepository.getCharactersCountByPrefix(
                                        currentPrefixChar
                                    )
                                updatePagination()
                                tagRepository.getCharactersByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            currentFilteredTagsCount = currentTagsCount
                            updatePagination()
                            tagRepository.getCharactersByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.GROUPS -> {
                    currentTagsCount = tagRepository.getGroupsCount()

                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                currentFilteredTagsCount =
                                    tagRepository.getGroupsCountBySpecialCharactersPrefix()
                                updatePagination()
                                tagRepository.getGroupsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                currentFilteredTagsCount =
                                    tagRepository.getGroupsCountByPrefix(currentPrefixChar)
                                updatePagination()
                                tagRepository.getGroupsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            currentFilteredTagsCount = currentTagsCount
                            updatePagination()
                            tagRepository.getGroupsByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.PARODIES -> {
                    currentTagsCount = tagRepository.getParodiesCount()

                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                currentFilteredTagsCount =
                                    tagRepository.getParodiesCountBySpecialCharactersPrefix()
                                updatePagination()
                                tagRepository.getParodiesBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                currentFilteredTagsCount =
                                    tagRepository.getParodiesCountByPrefix(currentPrefixChar)
                                updatePagination()
                                tagRepository.getParodiesByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            currentFilteredTagsCount = currentTagsCount
                            updatePagination()
                            tagRepository.getParodiesByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                else -> {
                    emptyList()
                }
            }
            Logger.d(
                TAG, "currentPrefixChar=$currentPrefixChar," +
                        " currentFilteredTagsCount=$currentFilteredTagsCount," +
                        " tagList=$tagList"
            )
            main.launch {
                view.setUpTagsList(tagsList, tagList)
            }

            main.launch {
                view.updateTag(tagType, currentTagsCount)
            }
        }
    }

    private fun updatePagination() {
        currentPage = 0
        var pagesCount = currentFilteredTagsCount / TAGS_PER_PAGE
        if (currentFilteredTagsCount % TAGS_PER_PAGE > 0) {
            pagesCount++
        }
        main.launch {
            view.refreshPages(pagesCount)
        }
    }
}
