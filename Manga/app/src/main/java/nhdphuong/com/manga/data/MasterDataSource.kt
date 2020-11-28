package nhdphuong.com.manga.data

import io.reactivex.Single
import nhdphuong.com.manga.data.entity.appversion.AppVersionInfo
import nhdphuong.com.manga.data.entity.appversion.LatestAppVersion
import nhdphuong.com.manga.data.entity.book.tags.Artist
import nhdphuong.com.manga.data.entity.book.tags.Category
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.entity.book.tags.Character
import nhdphuong.com.manga.data.entity.book.tags.Group
import nhdphuong.com.manga.data.entity.book.tags.Language
import nhdphuong.com.manga.data.entity.book.tags.Parody
import nhdphuong.com.manga.data.entity.book.tags.UnknownTag

interface MasterDataSource {
    interface Remote {
        suspend fun fetchArtistsList(onSuccess: (List<Artist>?) -> Unit, onError: () -> Unit)
        suspend fun fetchCharactersList(onSuccess: (List<Character>?) -> Unit, onError: () -> Unit)
        suspend fun fetchCategoriesList(onSuccess: (List<Category>?) -> Unit, onError: () -> Unit)
        suspend fun fetchGroupsList(onSuccess: (List<Group>?) -> Unit, onError: () -> Unit)
        suspend fun fetchParodiesList(onSuccess: (List<Parody>?) -> Unit, onError: () -> Unit)
        suspend fun fetchLanguagesList(onSuccess: (List<Language>?) -> Unit, onError: () -> Unit)
        suspend fun fetchTagsList(onSuccess: (List<Tag>?) -> Unit, onError: () -> Unit)
        suspend fun fetchUnknownTypesList(
            onSuccess: (List<UnknownTag>?) -> Unit,
            onError: () -> Unit
        )

        suspend fun fetchTagDataVersion(onSuccess: (Long) -> Unit, onError: () -> Unit)

        suspend fun fetchAppVersion(
            onSuccess: (LatestAppVersion) -> Unit,
            onError: (error: Throwable) -> Unit
        )

        fun getVersionHistory(): Single<List<AppVersionInfo>>
    }

    interface Local {
        /**
         * Fot Artists table
         */
        fun insertArtistsList(artistsList: List<Artist>)

        suspend fun getArtistsCount(): Int
        suspend fun getArtistsCountByPrefix(prefix: String): Int
        suspend fun getArtistsCountBySpecialCharactersPrefix(): Int
        suspend fun getArtistsBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
        ): List<Artist>

        suspend fun getArtistsBySpecialCharactersPrefixDescending(
            limit: Int,
            offset: Int
        ): List<Artist>

        suspend fun getArtistsByPrefixAscending(
            prefixString: String,
            limit: Int,
            offset: Int
        ): List<Artist>

        suspend fun getArtistsByPrefixDescending(
            prefixString: String,
            limit: Int,
            offset: Int
        ): List<Artist>

        suspend fun getArtistsByPopularityAscending(limit: Int, offset: Int): List<Artist>
        suspend fun getArtistsByPopularityDescending(limit: Int, offset: Int): List<Artist>

        /**
         * Fot Characters table
         */
        fun insertCharactersList(charactersList: List<Character>)

        suspend fun getCharactersCount(): Int
        suspend fun getCharactersCountByPrefix(prefix: String): Int
        suspend fun getCharactersCountBySpecialCharactersPrefix(): Int
        suspend fun getCharactersBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
        ): List<Character>

        suspend fun getCharactersBySpecialCharactersPrefixDescending(
            limit: Int,
            offset: Int
        ): List<Character>

        suspend fun getCharactersByPrefixAscending(
            prefixString: String,
            limit: Int,
            offset: Int
        ): List<Character>

        suspend fun getCharactersByPrefixDescending(
            prefixString: String,
            limit: Int,
            offset: Int
        ): List<Character>

        suspend fun getCharactersByPopularityAscending(limit: Int, offset: Int): List<Character>
        suspend fun getCharactersByPopularityDescending(limit: Int, offset: Int): List<Character>

        /**
         * Fot Categories table
         */
        fun insertCategoriesList(categoriesList: List<Category>)

        /**
         * Fot Groups table
         */
        fun insertGroupsList(groupsList: List<Group>)

        suspend fun getGroupsCount(): Int
        suspend fun getGroupsCountByPrefix(prefix: String): Int
        suspend fun getGroupsCountBySpecialCharactersPrefix(): Int
        suspend fun getGroupsBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
        ): List<Group>

        suspend fun getGroupsBySpecialCharactersPrefixDescending(
            limit: Int,
            offset: Int
        ): List<Group>

        suspend fun getGroupsByPrefixAscending(
            prefixString: String,
            limit: Int,
            offset: Int
        ): List<Group>

        suspend fun getGroupsByPrefixDescending(
            prefixString: String,
            limit: Int,
            offset: Int
        ): List<Group>

        suspend fun getGroupsByPopularityAscending(limit: Int, offset: Int): List<Group>
        suspend fun getGroupsByPopularityDescending(limit: Int, offset: Int): List<Group>

        /**
         * Fot Parodies table
         */
        fun insertParodiesList(parodiesList: List<Parody>)

        suspend fun getParodiesCount(): Int
        suspend fun getParodiesCountByPrefix(prefix: String): Int
        suspend fun getParodiesCountBySpecialCharactersPrefix(): Int
        suspend fun getParodiesBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
        ): List<Parody>

        suspend fun getParodiesBySpecialCharactersPrefixDescending(
            limit: Int,
            offset: Int
        ): List<Parody>

        suspend fun getParodiesByPrefixAscending(
            prefixString: String,
            limit: Int,
            offset: Int
        ): List<Parody>

        suspend fun getParodiesByPrefixDescending(
            prefixString: String,
            limit: Int,
            offset: Int
        ): List<Parody>

        suspend fun getParodiesByPopularityAscending(limit: Int, offset: Int): List<Parody>
        suspend fun getParodiesByPopularityDescending(limit: Int, offset: Int): List<Parody>

        /**
         * Fot Languages table
         */
        fun insertLanguagesList(languagesList: List<Language>)

        /**
         * Fot Tags table
         */
        fun insertTagsList(tagsList: List<Tag>)

        suspend fun getTagCount(): Int
        suspend fun getTagCountByPrefix(prefix: String): Int
        suspend fun getTagCountBySpecialCharactersPrefix(): Int
        suspend fun getTagsBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
        ): List<Tag>

        suspend fun getTagsBySpecialCharactersPrefixDescending(
            limit: Int,
            offset: Int
        ): List<Tag>

        suspend fun getTagsByPrefixAscending(
            prefixString: String,
            limit: Int,
            offset: Int
        ): List<Tag>

        suspend fun getTagsByPrefixDescending(
            prefixString: String,
            limit: Int,
            offset: Int
        ): List<Tag>

        suspend fun getTagsByPopularityAscending(limit: Int, offset: Int): List<Tag>
        suspend fun getTagsByPopularityDescending(limit: Int, offset: Int): List<Tag>

        /**
         * Fot UnknownTypes table
         */
        fun insertUnknownTypesList(unknownTagsList: List<UnknownTag>)
    }
}
