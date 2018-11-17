package nhdphuong.com.manga.features.tags

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.data.TagFilter

/*
 * Created by nhdphuong on 5/12/18.
 */
interface TagsContract {
    fun onTagChange(@Tag tag: String)

    interface View : Base.View<Presenter> {
        fun updateTag(@Tag tagType: String, tagCount: Int)
    }

    interface Presenter : Base.Presenter {
        fun changeCurrentTag(@Tag newTag: String)
        fun filterByCharacter(selectedCharacter: Char)
        fun changeTagFilterType(tagFilter: TagFilter)
    }
}