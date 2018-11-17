package nhdphuong.com.manga.data

import nhdphuong.com.manga.data.entity.book.tags.*
import nhdphuong.com.manga.data.entity.book.tags.Tag

interface TagDataSource {
    interface Remote {
        suspend fun fetchArtistsList(onSuccess: (List<Artist>?) -> Unit, onError: () -> Unit)
        suspend fun fetchCharactersList(onSuccess: (List<Character>?) -> Unit, onError: () -> Unit)
        suspend fun fetchCategoriesList(onSuccess: (List<Category>?) -> Unit, onError: () -> Unit)
        suspend fun fetchGroupsList(onSuccess: (List<Group>?) -> Unit, onError: () -> Unit)
        suspend fun fetchParodiesList(onSuccess: (List<Parody>?) -> Unit, onError: () -> Unit)
        suspend fun fetchLanguagesList(onSuccess: (List<Language>?) -> Unit, onError: () -> Unit)
        suspend fun fetchTagsList(onSuccess: (List<Tag>?) -> Unit, onError: () -> Unit)
        suspend fun fetchUnknownTypesList(onSuccess: (List<UnknownTag>?) -> Unit, onError: () -> Unit)
    }

    interface Local {
        fun insertArtistsList(artistsList: List<Artist>)
        fun insertCharactersList(charactersList: List<Character>)
        fun insertCategoriesList(categoriesList: List<Category>)
        fun insertGroupsList(groupsList: List<Group>)
        fun insertParodiesList(parodiesList: List<Parody>)
        fun insertLanguagesList(languagesList: List<Language>)
        fun insertTagsList(tagsList: List<Tag>)
        suspend fun getTagCount(): Int
        suspend fun getTagCountByPrefix(prefix: String): Int
        fun getTagsByPrefixAscending(prefixString: String, limit: Int, offset: Int): List<Tag>
        fun getTagsByPrefixDescending(prefixString: String, limit: Int, offset: Int): List<Tag>
        fun getTagsByPopularityAscending(limit: Int, offset: Int): List<Tag>
        fun getTagsByPopularityDescending(limit: Int, offset: Int): List<Tag>
        fun insertUnknownTypesList(unknownTagsList: List<UnknownTag>)
    }
}