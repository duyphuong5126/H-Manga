package nhdphuong.com.manga.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.data.TagDataSource
import nhdphuong.com.manga.data.entity.book.tags.Artist
import nhdphuong.com.manga.data.entity.book.tags.Category
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.entity.book.tags.Character
import nhdphuong.com.manga.data.entity.book.tags.Group
import nhdphuong.com.manga.data.entity.book.tags.Language
import nhdphuong.com.manga.data.entity.book.tags.Parody
import nhdphuong.com.manga.data.entity.book.tags.UnknownTag
import nhdphuong.com.manga.scope.corountine.IO
import javax.inject.Inject

class TagLocalDataSource @Inject constructor(private val mTagDAO: TagDAO,
                                             @IO private val io: CoroutineScope) : TagDataSource.Local {
    override fun insertArtistsList(artistsList: List<Artist>) {
        io.launch {
            mTagDAO.insertArtist(artistsList)
        }
    }

    override suspend fun getArtistsCount(): Int = mTagDAO.getArtistsCount()

    override suspend fun getArtistsCountByPrefix(prefix: String): Int {
        return mTagDAO.getArtistsCountByPrefix(prefix)
    }

    override suspend fun getArtistsCountBySpecialCharactersPrefix(): Int {
        return mTagDAO.getArtistsCountBySpecialCharactersPrefix()
    }

    override suspend fun getArtistsBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
    ): List<Artist> = mTagDAO.getArtistsBySpecialCharactersPrefixAscending(limit, offset)

    override suspend fun getArtistsBySpecialCharactersPrefixDescending(
            limit: Int,
            offset: Int
    ): List<Artist> = mTagDAO.getArtistsBySpecialCharactersPrefixDescending(limit, offset)

    override suspend fun getArtistsByPrefixAscending(
            prefixString: String,
            limit: Int,
            offset: Int
    ): List<Artist> = mTagDAO.getArtistsByPrefixAscending(prefixString, limit, offset)

    override suspend fun getArtistsByPrefixDescending(
            prefixString: String,
            limit: Int,
            offset: Int
    ): List<Artist> = mTagDAO.getArtistsByPrefixDescending(prefixString, limit, offset)

    override suspend fun getArtistsByPopularityAscending(
            limit: Int,
            offset: Int
    ): List<Artist> = mTagDAO.getArtistsByPopularityAscending(limit, offset)

    override suspend fun getArtistsByPopularityDescending(
            limit: Int,
            offset: Int
    ): List<Artist> = mTagDAO.getArtistsByPopularityDescending(limit, offset)

    override fun insertCharactersList(charactersList: List<Character>) {
        io.launch {
            mTagDAO.insertCharacters(charactersList)
        }
    }

    override suspend fun getCharactersCount(): Int = mTagDAO.getCharactersCount()

    override suspend fun getCharactersCountByPrefix(prefix: String): Int {
        return mTagDAO.getCharactersCountByPrefix(prefix)
    }

    override suspend fun getCharactersCountBySpecialCharactersPrefix(): Int {
        return mTagDAO.getCharactersCountBySpecialCharactersPrefix()
    }

    override suspend fun getCharactersBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
    ): List<Character> = mTagDAO.getCharactersBySpecialCharactersPrefixAscending(limit, offset)

    override suspend fun getCharactersBySpecialCharactersPrefixDescending(
            limit: Int,
            offset: Int
    ): List<Character> = mTagDAO.getCharactersBySpecialCharactersPrefixDescending(limit, offset)

    override suspend fun getCharactersByPrefixAscending(
            prefixString: String,
            limit: Int,
            offset: Int
    ): List<Character> = mTagDAO.getCharactersByPrefixAscending(prefixString, limit, offset)

    override suspend fun getCharactersByPrefixDescending(
            prefixString: String,
            limit: Int,
            offset: Int
    ): List<Character> = mTagDAO.getCharactersByPrefixDescending(prefixString, limit, offset)

    override suspend fun getCharactersByPopularityAscending(
            limit: Int,
            offset: Int
    ): List<Character> = mTagDAO.getCharactersByPopularityAscending(limit, offset)

    override suspend fun getCharactersByPopularityDescending(
            limit: Int,
            offset: Int
    ): List<Character> = mTagDAO.getCharactersByPopularityDescending(limit, offset)

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

    override suspend fun getGroupsCount(): Int = mTagDAO.getGroupsCount()

    override suspend fun getGroupsCountByPrefix(prefix: String): Int {
        return mTagDAO.getGroupsCountByPrefix(prefix)
    }

    override suspend fun getGroupsCountBySpecialCharactersPrefix(): Int {
        return mTagDAO.getGroupsCountBySpecialCharactersPrefix()
    }

    override suspend fun getGroupsBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
    ): List<Group> = mTagDAO.getGroupsBySpecialCharactersPrefixAscending(limit, offset)

    override suspend fun getGroupsBySpecialCharactersPrefixDescending(
            limit: Int,
            offset: Int
    ): List<Group> = mTagDAO.getGroupsBySpecialCharactersPrefixDescending(limit, offset)

    override suspend fun getGroupsByPrefixAscending(
            prefixString: String,
            limit: Int,
            offset: Int
    ): List<Group> = mTagDAO.getGroupsByPrefixAscending(prefixString, limit, offset)

    override suspend fun getGroupsByPrefixDescending(
            prefixString: String,
            limit: Int,
            offset: Int
    ): List<Group> = mTagDAO.getGroupsByPrefixDescending(prefixString, limit, offset)

    override suspend fun getGroupsByPopularityAscending(
            limit: Int,
            offset: Int
    ): List<Group> = mTagDAO.getGroupsByPopularityAscending(limit, offset)

    override suspend fun getGroupsByPopularityDescending(
            limit: Int,
            offset: Int
    ): List<Group> = mTagDAO.getGroupsByPopularityDescending(limit, offset)

    override fun insertParodiesList(parodiesList: List<Parody>) {
        io.launch {
            mTagDAO.insertParodies(parodiesList)
        }
    }

    override suspend fun getParodiesCount(): Int = mTagDAO.getParodiesCount()

    override suspend fun getParodiesCountByPrefix(prefix: String): Int {
        return mTagDAO.getParodiesCountByPrefix(prefix)
    }

    override suspend fun getParodiesCountBySpecialCharactersPrefix(): Int {
        return mTagDAO.getParodiesCountBySpecialCharactersPrefix()
    }

    override suspend fun getParodiesBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
    ): List<Parody> = mTagDAO.getParodiesBySpecialCharactersPrefixAscending(limit, offset)

    override suspend fun getParodiesBySpecialCharactersPrefixDescending(
            limit: Int,
            offset: Int
    ): List<Parody> = mTagDAO.getParodiesBySpecialCharactersPrefixDescending(limit, offset)

    override suspend fun getParodiesByPrefixAscending(
            prefixString: String,
            limit: Int,
            offset: Int
    ): List<Parody> = mTagDAO.getParodiesByPrefixAscending(prefixString, limit, offset)

    override suspend fun getParodiesByPrefixDescending(
            prefixString: String,
            limit: Int,
            offset: Int
    ): List<Parody> = mTagDAO.getParodiesByPrefixDescending(prefixString, limit, offset)

    override suspend fun getParodiesByPopularityAscending(
            limit: Int,
            offset: Int
    ): List<Parody> = mTagDAO.getParodiesByPopularityAscending(limit, offset)

    override suspend fun getParodiesByPopularityDescending(
            limit: Int,
            offset: Int
    ): List<Parody> = mTagDAO.getParodiesByPopularityDescending(limit, offset)

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

    override suspend fun getTagCountByPrefix(prefix: String): Int {
        return mTagDAO.getTagCountByPrefix(prefix)
    }

    override suspend fun getTagCountBySpecialCharactersPrefix(): Int {
        return mTagDAO.getTagCountBySpecialCharactersPrefix()
    }

    override suspend fun getTagsBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
    ): List<Tag> = mTagDAO.getTagsBySpecialCharactersPrefixAscending(limit, offset)

    override suspend fun getTagsBySpecialCharactersPrefixDescending(
            limit: Int,
            offset: Int
    ): List<Tag> = mTagDAO.getTagsBySpecialCharactersPrefixDescending(limit, offset)

    override suspend fun getTagsByPrefixAscending(
            prefixString: String,
            limit: Int,
            offset: Int
    ): List<Tag> = mTagDAO.getTagsByPrefixAscending(prefixString, limit, offset)

    override suspend fun getTagsByPrefixDescending(
            prefixString: String,
            limit: Int,
            offset: Int
    ): List<Tag> = mTagDAO.getTagsByPrefixDescending(prefixString, limit, offset)

    override suspend fun getTagsByPopularityAscending(
            limit: Int,
            offset: Int
    ): List<Tag> = mTagDAO.getTagsByPopularityAscending(limit, offset)

    override suspend fun getTagsByPopularityDescending(
            limit: Int,
            offset: Int
    ): List<Tag> = mTagDAO.getTagsByPopularityDescending(limit, offset)

    override fun insertUnknownTypesList(unknownTagsList: List<UnknownTag>) {
        io.launch {
            mTagDAO.insertUnknownTags(unknownTagsList)
        }
    }
}
