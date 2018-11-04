package nhdphuong.com.manga.data.local

import kotlinx.coroutines.experimental.launch
import nhdphuong.com.manga.data.TagDataSource
import nhdphuong.com.manga.data.entity.book.tags.*
import javax.inject.Inject

class TagLocalDataSource @Inject constructor(private val mTagDAO: TagDAO) : TagDataSource.Local {
    override fun insertArtistsList(artistsList: List<Artist>) {
        launch {
            mTagDAO.insertArtist(*artistsList.toTypedArray())
        }
    }

    override fun insertCharactersList(charactersList: List<Character>) {
        launch {
            mTagDAO.insertCharacters(*charactersList.toTypedArray())
        }
    }

    override fun insertCategoriesList(categoriesList: List<Category>) {
        launch {
            mTagDAO.insertCategories(*categoriesList.toTypedArray())
        }
    }

    override fun insertGroupsList(groupsList: List<Group>) {
        launch {
            mTagDAO.insertGroups(*groupsList.toTypedArray())
        }
    }

    override fun insertParodiesList(parodiesList: List<Parody>) {
        launch {
            mTagDAO.insertParodies(*parodiesList.toTypedArray())
        }
    }

    override fun insertLanguagesList(languagesList: List<Language>) {
        launch {
            mTagDAO.insertLanguages(*languagesList.toTypedArray())
        }
    }

    override fun insertTagsList(tagsList: List<Tag>) {
        launch {
            mTagDAO.insertTags(*tagsList.toTypedArray())
        }
    }

    override fun insertUnknownTypesList(unknownTagsList: List<UnknownTag>) {
        launch {
            mTagDAO.insertUnknownTags(*unknownTagsList.toTypedArray())
        }
    }
}