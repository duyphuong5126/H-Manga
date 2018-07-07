package nhdphuong.com.manga.features.tags

import dagger.Module
import dagger.Provides

/*
 * Created by nhdphuong on 5/12/18.
 */
@Module
class TagsModule(private val mTagsView: TagsContract.View, private val mTagType: String) {
    @Provides
    fun providesTagsView(): TagsContract.View = mTagsView

    @Provides
    fun providesTagType(): String = mTagType
}