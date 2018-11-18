package nhdphuong.com.manga.data.local

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.data.entity.book.tags.*

@Dao
interface TagDAO {
    companion object {
        private const val TABLE_TAG = Constants.TABLE_TAG

        private const val COLUMN_NAME = Constants.NAME
        private const val COLUMN_COUNT = Constants.COUNT

        private const val ALPHABET_CHARACTERS_CONDITION = "('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', " +
                "'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')"
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArtist(artists: List<Artist>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCharacters(characters: List<Character>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategories(categories: List<Category>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParodies(parodies: List<Parody>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroups(groups: List<Group>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLanguages(languages: List<Language>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnknownTags(unknownTags: List<UnknownTag>)

    /**
     * Fot Tag table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTags(tags: List<Tag>)
    @Query("SELECT count(*) FROM $TABLE_TAG")
    fun getTagCount(): Int
    // From A-Z only
    @Query("SELECT count(*) FROM $TABLE_TAG WHERE $COLUMN_NAME LIKE :prefixString")
    fun getTagCountByPrefix(prefixString: String): Int
    // From special characters only
    @Query("SELECT count(*) FROM $TABLE_TAG WHERE SUBSTR($COLUMN_NAME, 1, 1) NOT IN $ALPHABET_CHARACTERS_CONDITION")
    fun getTagCountBySpecialCharactersPrefix(): Int
    @Query("SELECT * FROM $TABLE_TAG WHERE SUBSTR($COLUMN_NAME, 1, 1) NOT IN $ALPHABET_CHARACTERS_CONDITION ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getTagsBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Tag>
    @Query("SELECT * FROM $TABLE_TAG WHERE SUBSTR($COLUMN_NAME, 1, 1) NOT IN $ALPHABET_CHARACTERS_CONDITION ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getTagsBySpecialCharactersPrefixDescending(limit: Int, offset: Int): List<Tag>
    @Query("SELECT * FROM $TABLE_TAG WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME ASC LIMIT :limit OFFSET :offset")
    fun getTagsByPrefixAscending(prefixString: String, limit: Int, offset: Int): List<Tag>
    @Query("SELECT * FROM $TABLE_TAG WHERE $COLUMN_NAME LIKE :prefixString ORDER BY $COLUMN_NAME DESC LIMIT :limit OFFSET :offset")
    fun getTagsByPrefixDescending(prefixString: String, limit: Int, offset: Int): List<Tag>
    @Query("SELECT * FROM $TABLE_TAG ORDER BY $COLUMN_COUNT ASC LIMIT :limit OFFSET :offset")
    fun getTagsByPopularityAscending(limit: Int, offset: Int): List<Tag>
    @Query("SELECT * FROM $TABLE_TAG ORDER BY $COLUMN_COUNT DESC LIMIT :limit OFFSET :offset")
    fun getTagsByPopularityDescending(limit: Int, offset: Int): List<Tag>
}