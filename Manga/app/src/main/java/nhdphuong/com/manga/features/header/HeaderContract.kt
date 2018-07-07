package nhdphuong.com.manga.features.header

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.Tab
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.features.home.HomeContract
import nhdphuong.com.manga.features.tags.TagsContract

/*
 * Created by nhdphuong on 4/10/18.
 */
interface HeaderContract {
    interface View : Base.View<Presenter> {
        fun setTagChangeListener(tagsContract: TagsContract)
        fun setSearchInputListener(homeContract: HomeContract)
        fun updateSearchBar(searchContent: String)
    }

    interface Presenter : Base.Presenter {
        fun goToTagList(@Tag tag: String)
    }
}