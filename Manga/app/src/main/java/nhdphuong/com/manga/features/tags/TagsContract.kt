package nhdphuong.com.manga.features.tags

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.Tag

/*
 * Created by nhdphuong on 5/12/18.
 */
interface TagsContract {
    fun onTagChange(@Tag tag: String)

    interface View : Base.View<Presenter> {

    }

    interface Presenter : Base.Presenter {
        fun changeCurrentTag(@Tag newTag: String)
    }
}