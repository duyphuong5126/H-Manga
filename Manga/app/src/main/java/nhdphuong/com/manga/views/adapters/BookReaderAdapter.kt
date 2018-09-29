package nhdphuong.com.manga.views.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ortiz.touchview.TouchImageView
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.GlideUtils
import nhdphuong.com.manga.views.customs.MyTextView

/*
 * Created by nhdphuong on 5/5/18.
 */
class BookReaderAdapter(private val mContext: Context, private val mPageUrlList: List<String>,
                        private val mOnTapListener: View.OnClickListener) : PagerAdapter() {
    companion object {
        private const val TAG = "BookReaderAdapter"
    }

    private val mPageMap: HashMap<Int, BookReaderViewHolder> = HashMap()

    init {
        mPageMap.clear()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val readerViewHolder = BookReaderViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_book_page, container, false),
                mPageUrlList[position],
                position + 1
        )
        mPageMap[position] = readerViewHolder
        container.addView(readerViewHolder.view)
        readerViewHolder.ivPage.let { ivPage ->
            ivPage.setOnClickListener {
                mOnTapListener.onClick(ivPage)
            }
        }
        return readerViewHolder.view
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun getCount(): Int = mPageUrlList.size

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        Logger.d(TAG, "Remove item $position")
        mPageMap[position]?.ivPage?.let { ivPage ->
            GlideUtils.clear(ivPage)
        }
        container.removeView(`object` as View)
    }

    override fun getPageTitle(position: Int): CharSequence = "Page number ${position + 1}"

    fun resetPageToNormal(page: Int) {
        mPageMap[page]?.ivPage?.let { ivPage ->
            if (ivPage.isZoomed) {
                ivPage.resetZoom()
            }
        }
    }

    fun resetPage(page: Int) {
        mPageMap[page]?.reloadImage()
    }

    private class BookReaderViewHolder(val view: View, private val pageUrl: String, page: Int) {
        val ivPage: TouchImageView = view.findViewById(R.id.ivPage)
        val mtvPageTitle: MyTextView = view.findViewById(R.id.mtvPageTitle)

        init {
            mtvPageTitle.text = page.toString()
            mtvPageTitle.visibility = View.VISIBLE
            reloadImage()
        }

        fun reloadImage() {
            GlideUtils.loadImage(pageUrl, R.drawable.ic_404_not_found, ivPage, object : RequestListener<Drawable> {
                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    mtvPageTitle.visibility = View.GONE
                    Logger.d(TAG, "Page is loaded successfully")
                    return false
                }

                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    Logger.d(TAG, "Page loading failed")
                    return true
                }
            })
        }
    }
}