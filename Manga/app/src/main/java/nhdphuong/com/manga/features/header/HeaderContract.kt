package nhdphuong.com.manga.features.header

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.Tab
import nhdphuong.com.manga.features.RandomContract
import nhdphuong.com.manga.features.SearchContract
import nhdphuong.com.manga.features.tags.TagsContract

/*
 * Created by nhdphuong on 4/10/18.
 */
interface HeaderContract {
    interface View : Base.View<Presenter> {
        fun setPresenter(presenter: Presenter)
        fun setTagChangeListener(tagsContract: TagsContract)
        fun setSearchInputListener(searchContract: SearchContract)
        fun setRandomContract(randomContract: RandomContract)
        fun updateSearchBar(searchContent: String)
        fun goToTagsList(tab: Tab)
        fun goToRecentList()
        fun goToFavoriteList()
        fun goToRandomBook()
        fun showNoNetworkPopup()
        fun showTagsDownloadingPopup()
        fun setUpSuggestionList()
        fun updateSuggestionList(suggestionList: List<String>)
        fun navigateToFeedbackForm(formUrl: String)
        fun showSuggestionDeletedMessage(searchContent: String)
        fun showGeneralError()
    }

    interface Presenter : Base.Presenter {
        fun goToTagsList(tab: Tab)
        fun processSelectedTab(tab: Tab)
        fun saveSearchInfo(searchContent: String)
        fun refreshTagData()
        fun requestFeedbackForm()
        fun removeSuggestion(suggestion: String)
    }
}
