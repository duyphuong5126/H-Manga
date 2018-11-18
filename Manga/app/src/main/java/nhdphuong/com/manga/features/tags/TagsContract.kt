package nhdphuong.com.manga.features.tags

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.data.TagFilter
import nhdphuong.com.manga.data.entity.book.tags.ITag
import nhdphuong.com.manga.features.SearchContract

/*
 * Created by nhdphuong on 5/12/18.
 */
interface TagsContract {
    fun onTagChange(@Tag tag: String)

    interface View : Base.View<Presenter> {
        fun updateTag(@Tag tagType: String, tagCount: Int)
        fun refreshPages(pagesCount: Int)
        fun setUpTagsList(source: ArrayList<ITag>, tags: List<ITag>)
        fun refreshTagsList(tags: List<ITag>)
        fun setSearchInputListener(searchContract: SearchContract)
    }

    interface Presenter : Base.Presenter {
        fun changeCurrentTag(@Tag newTag: String)
        fun filterByCharacter(selectedCharacter: Char)
        fun changeTagFilterType(tagFilter: TagFilter)
        fun jumpToPage(page: Int)
    }
}