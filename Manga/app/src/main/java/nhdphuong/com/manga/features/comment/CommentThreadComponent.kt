package nhdphuong.com.manga.features.comment

import dagger.Subcomponent

@Subcomponent(modules = [CommentThreadModule::class])
interface CommentThreadComponent {
    fun inject(commentThreadActivity: CommentThreadActivity)
}
