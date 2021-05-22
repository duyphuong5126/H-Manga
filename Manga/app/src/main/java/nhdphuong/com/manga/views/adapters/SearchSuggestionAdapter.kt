package nhdphuong.com.manga.views.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import kotlinx.android.synthetic.main.item_search_suggestion.view.ivRemoveSuggestion
import kotlinx.android.synthetic.main.item_search_suggestion.view.tvSuggestion
import nhdphuong.com.manga.R
import java.util.Locale

class SearchSuggestionAdapter(
    context: Context,
    private val suggestions: ArrayList<String>,
    private val suggestionCallback: SuggestionCallback
) : ArrayAdapter<String>(context, R.layout.item_search_suggestion, R.id.tvSuggestion) {
    private val locale = Locale.getDefault()
    private val suggestionList: ArrayList<String> = ArrayList()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        view.tvSuggestion?.setOnClickListener {
            suggestionCallback.onSuggestionSelected(suggestionList[position])
        }
        view.ivRemoveSuggestion?.setOnClickListener {
            suggestionCallback.onSuggestionRemoved(position, suggestionList[position])
        }
        return view
    }

    override fun getCount(): Int {
        return suggestionList.size
    }

    override fun getItem(position: Int): String {
        return suggestionList[position]
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            // Assign the data to the FilterResults
            suggestionList.clear()
            suggestionList.addAll(getAutoComplete(constraint?.toString().orEmpty()))

            filterResults.values = suggestionList
            filterResults.count = suggestionList.size
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            results?.let {
                if (it.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    private fun getAutoComplete(constraint: String): List<String> {
        return suggestions.filter {
            it.lowercase(locale).startsWith(constraint.lowercase(locale))
        }
    }

    fun updateList(suggestionList: List<String>) {
        suggestions.clear()
        suggestions.addAll(suggestionList)
        notifyDataSetChanged()
    }

    fun removeItemAt(position: Int) {
        val suggestion = suggestionList.removeAt(position)
        suggestions.removeAll { it == suggestion }
        notifyDataSetChanged()
    }

    fun reset() {
        suggestionList.clear()
        suggestionList.addAll(suggestions)
        notifyDataSetChanged()
    }

    interface SuggestionCallback {
        fun onSuggestionRemoved(position: Int, suggestionValue: String)
        fun onSuggestionSelected(suggestion: String)
    }
}