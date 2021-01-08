package nhdphuong.com.manga.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ortiz.touchview.TouchImageView
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.customs.MyTextView
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.gone

/*
 * Created by nhdphuong on 5/5/18.
 */
class BookReaderAdapter(
    private val pageUrlList: List<String>,
    private val onTapListener: View.OnClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TAG = "BookReaderAdapter"
    }

    private val pageMap: HashMap<Int, Boolean> = HashMap()

    init {
        pageMap.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BookReaderViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_book_page, parent, false),
            onTapListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BookReaderViewHolder).bindTo(pageUrlList[position], position + 1)
    }

    override fun getItemCount(): Int {
        return pageUrlList.size
    }

    fun resetPageToNormal(page: Int) {
        pageMap[page]?.let { isZoomed ->
            if (isZoomed) {
                pageMap[page] = false
                notifyItemChanged(page)
            }
        }
    }

    fun resetPage(page: Int) {
        notifyItemChanged(page)
    }

    private class BookReaderViewHolder(
        view: View,
        onTapListener: View.OnClickListener
    ) : RecyclerView.ViewHolder(view) {
        private val ivPage: TouchImageView = view.findViewById(R.id.ivPage)
        private val mtvPageTitle: MyTextView = view.findViewById(R.id.mtvPageTitle)

        init {
            ivPage.setOnClickListener(onTapListener)
            mtvPageTitle.setOnClickListener(onTapListener)
        }

        fun bindTo(pageUrl: String, page: Int) {
            mtvPageTitle.text = page.toString()
            mtvPageTitle.becomeVisible()
            ivPage.resetZoom()
            reloadImage(pageUrl)
        }

        fun reloadImage(pageUrl: String) {
            ivPage.doOnGlobalLayout {
                ImageUtils.loadImage(pageUrl, R.drawable.ic_404_not_found, ivPage, onLoadSuccess = {
                    mtvPageTitle.gone()
                    Logger.d(TAG, "$pageUrl is loaded successfully")
                }, onLoadFailed = {
                    Logger.d(TAG, "$pageUrl loading failed")
                })
            }
        }
    }
}
