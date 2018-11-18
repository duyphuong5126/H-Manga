package nhdphuong.com.manga.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.data.TagDataSource
import nhdphuong.com.manga.data.entity.book.tags.*
import nhdphuong.com.manga.scope.corountine.IO
import javax.inject.Inject

class TagLocalDataSource @Inject constructor(private val mTagDAO: TagDAO,
                                             @IO private val io: CoroutineScope) : TagDataSource.Local {
    override fun insertArtistsList(artistsList: List<Artist>) {
        io.launch {
            mTagDAO.insertArtist(artistsList)
        }
    }

    override fun insertCharactersList(charactersList: List<Character>) {
        io.launch {
            mTagDAO.insertCharacters(charactersList)
        }
    }

    override fun insertCategoriesList(categoriesList: List<Category>) {
        io.launch {
            mTagDAO.insertCategories(categoriesList)
        }
    }

    override fun insertGroupsList(groupsList: List<Group>) {
        io.launch {
            mTagDAO.insertGroups(groupsList)
        }
    }

    override fun insertParodiesList(parodiesList: List<Parody>) {
        io.launch {
            mTagDAO.insertParodies(parodiesList)
        }
    }

    override fun insertLanguagesList(languagesList: List<Language>) {
        io.launch {
            mTagDAO.insertLanguages(languagesList)
        }
    }

    override fun insertTagsList(tagsList: List<Tag>) {
        io.launch {
            mTagDAO.insertTags(tagsList)
        }
    }

    override suspend fun getTagCount(): Int = mTagDAO.getTagCount()

    override suspend fun getTagCountByPrefix(prefix: String): Int = mTagDAO.getTagCountByPrefix(prefix)

    override suspend fun getTagCountBySpecialCharactersPrefix(): Int = mTagDAO.getTagCountBySpecialCharactersPrefix()

    override suspend fun getTagsBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Tag> = mTagDAO.getTagsBySpecialCharactersPrefixAscending(limit, offset)

    override suspend fun getTagsBySpecialCharactersPrefixDescending(limit: Int, offset: Int): List<Tag> = mTagDAO.getTagsBySpecialCharactersPrefixDescending(limit, offset)

    override suspend fun getTagsByPrefixAscending(prefixString: String, limit: Int, offset: Int): List<Tag> = mTagDAO.getTagsByPrefixAscending(prefixString, limit, offset)

    override suspend fun getTagsByPrefixDescending(prefixString: String, limit: Int, offset: Int): List<Tag> = mTagDAO.getTagsByPrefixDescending(prefixString, limit, offset)

    override suspend fun getTagsByPopularityAscending(limit: Int, offset: Int): List<Tag> = mTagDAO.getTagsByPopularityAscending(limit, offset)

    override suspend fun getTagsByPopularityDescending(limit: Int, offset: Int): List<Tag> = mTagDAO.getTagsByPopularityDescending(limit, offset)

    override fun insertUnknownTypesList(unknownTagsList: List<UnknownTag>) {
        io.launch {
            mTagDAO.insertUnknownTags(unknownTagsList)
        }
    }
}