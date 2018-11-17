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
    @Query("SELECT count(*) FROM $TABLE_TAG WHERE $COLUMN_NAME like :prefixString")
    fun getTagCountByPrefix(prefixString: String): Int

    @Query("SELECT * FROM $TABLE_TAG WHERE $COLUMN_NAME like :prefixString ORDER BY $COLUMN_NAME ASC limit :limit offset :offset")
    fun getTagsByPrefixAscending(prefixString: String, limit: Int, offset: Int): List<Tag>

    @Query("SELECT * FROM $TABLE_TAG WHERE $COLUMN_NAME like :prefixString ORDER BY $COLUMN_NAME DESC limit :limit offset :offset")
    fun getTagsByPrefixDescending(prefixString: String, limit: Int, offset: Int): List<Tag>

    @Query("SELECT * FROM $TABLE_TAG ORDER BY $COLUMN_COUNT ASC limit :limit offset :offset")
    fun getTagsByPopularityAscending(limit: Int, offset: Int): List<Tag>

    @Query("SELECT * FROM $TABLE_TAG ORDER BY $COLUMN_COUNT DESC limit :limit offset :offset")
    fun getTagsByPopularityDescending(limit: Int, offset: Int): List<Tag>
}