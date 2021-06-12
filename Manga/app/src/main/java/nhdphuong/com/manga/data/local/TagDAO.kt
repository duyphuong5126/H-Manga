package nhdphuong.com.manga.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import nhdphuong.com.manga.Constants.Companion.NAME as COLUMN_NAME
import nhdphuong.com.manga.Constants.Companion.COUNT as COLUMN_COUNT
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.TABLE_ARTIST
import nhdphuong.com.manga.Constants.Companion.TABLE_CHARACTER
import nhdphuong.com.manga.Constants.Companion.TABLE_GROUP
import nhdphuong.com.manga.Constants.Companion.TABLE_PARODY
import nhdphuong.com.manga.Constants.Companion.TABLE_TAG
import nhdphuong.com.manga.Constants.Companion.TABLE_CATEGORY
import nhdphuong.com.manga.Constants.Companion.TABLE_LANGUAGE
import nhdphuong.com.manga.data.entity.book.tags.Artist
import nhdphuong.com.manga.data.entity.book.tags.Category
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.entity.book.tags.Character
import nhdphuong.com.manga.data.entity.book.tags.Group
import nhdphuong.com.manga.data.entity.book.tags.Language
import nhdphuong.com.manga.data.entity.book.tags.Parody
import nhdphuong.com.manga.data.entity.book.tags.UnknownTag

@Dao
interface TagDAO {

    /**
     * Fot Category table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategories(categories: List<Category>)

    @Query("SELECT * FROM $TABLE_CATEGORY where $ID in (:categoryIds)")
    fun getCategoriesByIds(categoryIds: List<Long>): List<Category>

    /**
     * Fot Language table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLanguages(languages: List<Language>)

    @Query("SELECT * FROM $TABLE_LANGUAGE where $ID in (:languageIds)")
    fun getLanguagesByIds(languageIds: List<Long>): List<Language>

    /**
     * Fot UnknownTag table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnknownTags(unknownTags: List<UnknownTag>)

    /**
     * Fot Tags table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTags(tags: List<Tag>)

    @Query("SELECT count(*) FROM $TABLE_TAG")
    fun getTagCount(): Int

    @Query("SELECT * FROM $TABLE_TAG where $ID in (:tagIds)")
    fun getTagsByIds(tagIds: List<Long>): List<Tag>

    // From A-Z only
    @Query("SELECT count(*) FROM $TABLE_TAG WHERE $COLUMN_NAME LIKE :prefixString")
    fun getTagCountByPrefix(prefixString: String): Int

    // From special characters only
    @Query("SELECT count(*) FROM $TABLE_TAG WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME")
    fun getTagCountBySpecialCharactersPrefix(): Int

    @Query("SELECT * FROM $TABLE_TAG WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getTagsBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Tag>

    @Query("SELECT * FROM $TABLE_TAG WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getTagsBySpecialCharactersPrefixDescending(limit: Int, offset: Int): List<Tag>

    @Query("SELECT * FROM $TABLE_TAG WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getTagsByPrefixAscending(prefixString: String, limit: Int, offset: Int): List<Tag>

    @Query("SELECT * FROM $TABLE_TAG WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getTagsByPrefixDescending(prefixString: String, limit: Int, offset: Int): List<Tag>

    @Query("SELECT * FROM $TABLE_TAG ORDER BY $COLUMN_COUNT ASC LIMIT :limit OFFSET :offset")
    fun getTagsByPopularityAscending(limit: Int, offset: Int): List<Tag>

    @Query("SELECT * FROM $TABLE_TAG ORDER BY $COLUMN_COUNT DESC LIMIT :limit OFFSET :offset")
    fun getTagsByPopularityDescending(limit: Int, offset: Int): List<Tag>

    /**
     * For Artists table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArtists(artists: List<Artist>)

    @Query("SELECT count(*) FROM $TABLE_ARTIST")
    fun getArtistsCount(): Int

    @Query("SELECT * FROM $TABLE_ARTIST where $ID in (:artistIds)")
    fun getArtistsByIds(artistIds: List<Long>): List<Artist>

    // From A-Z only
    @Query("SELECT count(*) FROM $TABLE_ARTIST WHERE $COLUMN_NAME LIKE :prefixString")
    fun getArtistsCountByPrefix(prefixString: String): Int

    // From special characters only
    @Query("SELECT count(*) FROM $TABLE_ARTIST WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME")
    fun getArtistsCountBySpecialCharactersPrefix(): Int

    @Query("SELECT * FROM $TABLE_ARTIST WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getArtistsBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Artist>

    @Query("SELECT * FROM $TABLE_ARTIST WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getArtistsBySpecialCharactersPrefixDescending(limit: Int, offset: Int): List<Artist>

    @Query("SELECT * FROM $TABLE_ARTIST WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getArtistsByPrefixAscending(prefixString: String, limit: Int, offset: Int): List<Artist>

    @Query("SELECT * FROM $TABLE_ARTIST WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getArtistsByPrefixDescending(prefixString: String, limit: Int, offset: Int): List<Artist>

    @Query("SELECT * FROM $TABLE_ARTIST ORDER BY $COLUMN_COUNT ASC LIMIT :limit OFFSET :offset")
    fun getArtistsByPopularityAscending(limit: Int, offset: Int): List<Artist>

    @Query("SELECT * FROM $TABLE_ARTIST ORDER BY $COLUMN_COUNT DESC LIMIT :limit OFFSET :offset")
    fun getArtistsByPopularityDescending(limit: Int, offset: Int): List<Artist>

    /**
     * For Characters table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCharacters(characters: List<Character>)

    @Query("SELECT count(*) FROM $TABLE_CHARACTER")
    fun getCharactersCount(): Int

    @Query("SELECT * FROM $TABLE_CHARACTER where $ID in (:characterIds)")
    fun getCharactersByIds(characterIds: List<Long>): List<Character>

    // From A-Z only
    @Query("SELECT count(*) FROM $TABLE_CHARACTER WHERE $COLUMN_NAME LIKE :prefixString")
    fun getCharactersCountByPrefix(prefixString: String): Int

    // From special characters only
    @Query("SELECT count(*) FROM $TABLE_CHARACTER WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME")
    fun getCharactersCountBySpecialCharactersPrefix(): Int

    @Query("SELECT * FROM $TABLE_CHARACTER WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getCharactersBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Character>

    @Query("SELECT * FROM $TABLE_CHARACTER WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getCharactersBySpecialCharactersPrefixDescending(limit: Int, offset: Int): List<Character>

    @Query("SELECT * FROM $TABLE_CHARACTER WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getCharactersByPrefixAscending(
        prefixString: String,
        limit: Int,
        offset: Int
    ): List<Character>

    @Query("SELECT * FROM $TABLE_CHARACTER WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getCharactersByPrefixDescending(
        prefixString: String,
        limit: Int,
        offset: Int
    ): List<Character>

    @Query("SELECT * FROM $TABLE_CHARACTER ORDER BY $COLUMN_COUNT ASC LIMIT :limit OFFSET :offset")
    fun getCharactersByPopularityAscending(limit: Int, offset: Int): List<Character>

    @Query("SELECT * FROM $TABLE_CHARACTER ORDER BY $COLUMN_COUNT DESC LIMIT :limit OFFSET :offset")
    fun getCharactersByPopularityDescending(limit: Int, offset: Int): List<Character>

    /**
     * For Parodies table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParodies(parodies: List<Parody>)

    @Query("SELECT count(*) FROM $TABLE_PARODY")
    fun getParodiesCount(): Int

    @Query("SELECT * FROM $TABLE_PARODY where $ID in (:parodyIds)")
    fun getParodiesByIds(parodyIds: List<Long>): List<Parody>

    // From A-Z only
    @Query("SELECT count(*) FROM $TABLE_PARODY WHERE $COLUMN_NAME LIKE :prefixString")
    fun getParodiesCountByPrefix(prefixString: String): Int

    // From special characters only
    @Query("SELECT count(*) FROM $TABLE_PARODY WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME")
    fun getParodiesCountBySpecialCharactersPrefix(): Int

    @Query("SELECT * FROM $TABLE_PARODY WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getParodiesBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Parody>

    @Query("SELECT * FROM $TABLE_PARODY WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getParodiesBySpecialCharactersPrefixDescending(limit: Int, offset: Int): List<Parody>

    @Query("SELECT * FROM $TABLE_PARODY WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getParodiesByPrefixAscending(prefixString: String, limit: Int, offset: Int): List<Parody>

    @Query("SELECT * FROM $TABLE_PARODY WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getParodiesByPrefixDescending(prefixString: String, limit: Int, offset: Int): List<Parody>

    @Query("SELECT * FROM $TABLE_PARODY ORDER BY $COLUMN_COUNT ASC LIMIT :limit OFFSET :offset")
    fun getParodiesByPopularityAscending(limit: Int, offset: Int): List<Parody>

    @Query("SELECT * FROM $TABLE_PARODY ORDER BY $COLUMN_COUNT DESC LIMIT :limit OFFSET :offset")
    fun getParodiesByPopularityDescending(limit: Int, offset: Int): List<Parody>

    /**
     * For Groups table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroups(groups: List<Group>)

    @Query("SELECT count(*) FROM $TABLE_GROUP")
    fun getGroupsCount(): Int

    @Query("SELECT * FROM $TABLE_GROUP where $ID in (:groupIds)")
    fun getGroupsByIds(groupIds: List<Long>): List<Group>

    // From A-Z only
    @Query("SELECT count(*) FROM $TABLE_GROUP WHERE $COLUMN_NAME LIKE :prefixString")
    fun getGroupsCountByPrefix(prefixString: String): Int

    // From special characters only
    @Query("SELECT count(*) FROM $TABLE_GROUP WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME")
    fun getGroupsCountBySpecialCharactersPrefix(): Int

    @Query("SELECT * FROM $TABLE_GROUP WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getGroupsBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Group>

    @Query("SELECT * FROM $TABLE_GROUP WHERE SUBSTR(LOWER($COLUMN_NAME), 1, 1) NOT BETWEEN 'a' AND 'z' ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getGroupsBySpecialCharactersPrefixDescending(limit: Int, offset: Int): List<Group>

    @Query("SELECT * FROM $TABLE_GROUP WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getGroupsByPrefixAscending(prefixString: String, limit: Int, offset: Int): List<Group>

    @Query("SELECT * FROM $TABLE_GROUP WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getGroupsByPrefixDescending(prefixString: String, limit: Int, offset: Int): List<Group>

    @Query("SELECT * FROM $TABLE_GROUP ORDER BY $COLUMN_COUNT ASC LIMIT :limit OFFSET :offset")
    fun getGroupsByPopularityAscending(limit: Int, offset: Int): List<Group>

    @Query("SELECT * FROM $TABLE_GROUP ORDER BY $COLUMN_COUNT DESC LIMIT :limit OFFSET :offset")
    fun getGroupsByPopularityDescending(limit: Int, offset: Int): List<Group>
}
