package nhdphuong.com.manga.features.tags

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.data.TagFilter
import nhdphuong.com.manga.data.entity.book.tags.ITag
import nhdphuong.com.manga.data.repository.MasterDataRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import javax.inject.Inject

/*
 * Created by nhdphuong on 5/12/18.
 */
class TagsPresenter @Inject constructor(
    private val view: TagsContract.View,
    private val masterDataRepository: MasterDataRepository,
    @Tag private var tagType: String,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : TagsContract.Presenter {
    companion object {
        private const val TAG_PREFIXES = Constants.TAG_PREFIXES
        private const val TAGS_PER_PAGE = 25
    }

    private val logger: Logger by lazy {
        Logger("TagsPresenter")
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
        logger.d("Changed from $tagType to $newTag")
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
                                masterDataRepository.getTagsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                masterDataRepository.getTagsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            masterDataRepository.getTagsByPopularityDescending(
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
                                masterDataRepository.getArtistsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                masterDataRepository.getArtistsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            masterDataRepository.getArtistsByPopularityDescending(
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
                                masterDataRepository.getCharactersBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                masterDataRepository.getCharactersByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            masterDataRepository.getCharactersByPopularityDescending(
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
                                masterDataRepository.getGroupsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                masterDataRepository.getGroupsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            masterDataRepository.getGroupsByPopularityDescending(
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
                                masterDataRepository.getParodiesBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                masterDataRepository.getParodiesByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            masterDataRepository.getParodiesByPopularityDescending(
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
            logger.d("currentPrefixChar=$currentPrefixChar, currentFilteredTagsCount=$currentFilteredTagsCount, tagList=$tagList")
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
                    currentTagsCount = masterDataRepository.getTagCount()

                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                currentFilteredTagsCount =
                                    masterDataRepository.getTagCountBySpecialCharactersPrefix()
                                updatePagination()
                                masterDataRepository.getTagsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                currentFilteredTagsCount =
                                    masterDataRepository.getTagsCountByPrefix(
                                        currentPrefixChar
                                    )
                                updatePagination()
                                masterDataRepository.getTagsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            currentFilteredTagsCount = currentTagsCount
                            updatePagination()
                            masterDataRepository.getTagsByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.ARTISTS -> {
                    currentTagsCount = masterDataRepository.getArtistsCount()

                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                currentFilteredTagsCount =
                                    masterDataRepository.getArtistsCountBySpecialCharactersPrefix()
                                updatePagination()
                                masterDataRepository.getArtistsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                currentFilteredTagsCount =
                                    masterDataRepository.getArtistsCountByPrefix(
                                        currentPrefixChar
                                    )
                                updatePagination()
                                masterDataRepository.getArtistsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            currentFilteredTagsCount = currentTagsCount
                            updatePagination()
                            masterDataRepository.getArtistsByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.CHARACTERS -> {
                    currentTagsCount = masterDataRepository.getCharactersCount()

                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                currentFilteredTagsCount =
                                    masterDataRepository.getCharactersCountBySpecialCharactersPrefix()
                                updatePagination()
                                masterDataRepository.getCharactersBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                currentFilteredTagsCount =
                                    masterDataRepository.getCharactersCountByPrefix(
                                        currentPrefixChar
                                    )
                                updatePagination()
                                masterDataRepository.getCharactersByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            currentFilteredTagsCount = currentTagsCount
                            updatePagination()
                            masterDataRepository.getCharactersByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.GROUPS -> {
                    currentTagsCount = masterDataRepository.getGroupsCount()

                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                currentFilteredTagsCount =
                                    masterDataRepository.getGroupsCountBySpecialCharactersPrefix()
                                updatePagination()
                                masterDataRepository.getGroupsBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                currentFilteredTagsCount =
                                    masterDataRepository.getGroupsCountByPrefix(currentPrefixChar)
                                updatePagination()
                                masterDataRepository.getGroupsByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            currentFilteredTagsCount = currentTagsCount
                            updatePagination()
                            masterDataRepository.getGroupsByPopularityDescending(
                                TAGS_PER_PAGE,
                                currentPage * TAGS_PER_PAGE
                            )
                        }
                    }
                }
                Constants.PARODIES -> {
                    currentTagsCount = masterDataRepository.getParodiesCount()

                    when (tagFilter) {
                        TagFilter.ALPHABET -> {
                            if (currentPrefixChar == TAG_PREFIXES[0]) {
                                currentFilteredTagsCount =
                                    masterDataRepository.getParodiesCountBySpecialCharactersPrefix()
                                updatePagination()
                                masterDataRepository.getParodiesBySpecialCharactersPrefixAscending(
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            } else {
                                currentFilteredTagsCount =
                                    masterDataRepository.getParodiesCountByPrefix(currentPrefixChar)
                                updatePagination()
                                masterDataRepository.getParodiesByPrefixAscending(
                                    currentPrefixChar,
                                    TAGS_PER_PAGE,
                                    currentPage * TAGS_PER_PAGE
                                )
                            }
                        }
                        else -> {
                            currentFilteredTagsCount = currentTagsCount
                            updatePagination()
                            masterDataRepository.getParodiesByPopularityDescending(
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
            logger.d("currentPrefixChar=$currentPrefixChar, currentFilteredTagsCount=$currentFilteredTagsCount, tagList=$tagList")
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
