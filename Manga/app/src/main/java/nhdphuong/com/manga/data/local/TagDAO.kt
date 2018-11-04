package nhdphuong.com.manga.data.local

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import nhdphuong.com.manga.data.entity.book.tags.*

@Dao
interface TagDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTags(vararg tags: Tag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArtist(vararg artists: Artist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCharacters(vararg characters: Character)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategories(vararg categories: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParodies(vararg parodies: Parody)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroups(vararg groups: Group)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLanguages(vararg languages: Language)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnknownTags(vararg languages: UnknownTag)
}