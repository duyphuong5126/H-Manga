package nhdphuong.com.manga.data.local

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import nhdphuong.com.manga.Constants
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
    companion object {
        private const val TABLE_TAG = Constants.TABLE_TAG
        private const val TABLE_ARTIST = Constants.TABLE_ARTIST
        private const val TABLE_CHARACTER = Constants.TABLE_CHARACTER
        private const val TABLE_PARODY = Constants.TABLE_PARODY
        private const val TABLE_GROUP = Constants.TABLE_GROUP

        private const val COLUMN_NAME = Constants.NAME
        private const val COLUMN_COUNT = Constants.COUNT
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategories(categories: List<Category>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLanguages(languages: List<Language>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnknownTags(unknownTags: List<UnknownTag>)

    /**
     * Fot Tags table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTags(tags: List<Tag>)

    @Query("SELECT count(*) FROM $TABLE_TAG")
    fun getTagCount(): Int

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
    fun insertArtist(artists: List<Artist>)

    @Query("SELECT count(*) FROM $TABLE_ARTIST")
    fun getArtistsCount(): Int

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
    fun getCharactersByPrefixAscending(prefixString: String, limit: Int, offset: Int): List<Character>

    @Query("SELECT * FROM $TABLE_CHARACTER WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getCharactersByPrefixDescending(prefixString: String, limit: Int, offset: Int): List<Character>

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
