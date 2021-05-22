package nhdphuong.com.manga.views.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ortiz.touchview.TouchImageView
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.customs.MyTextView
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.uimodel.ReaderType
import nhdphuong.com.manga.views.uimodel.ReaderType.VerticalScroll

/*
 * Created by nhdphuong on 5/5/18.
 */
class BookReaderAdapter(
    private val pageUrlList: List<String>,
    private var readerType: ReaderType,
    private val onTapListener: View.OnClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (readerType == VerticalScroll) {
            val view = layoutInflater.inflate(R.layout.item_book_page_wrapped, parent, false)
            WrapContentPageViewHolder(view)
        } else {
            val view = layoutInflater.inflate(R.layout.item_book_page, parent, false)
            FullScreenPageViewHolder(view, onTapListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FullScreenPageViewHolder -> holder.bindTo(pageUrlList[position], position + 1)
            is WrapContentPageViewHolder -> holder.bindTo(pageUrlList[position], position + 1)
        }
    }

    override fun getItemCount(): Int {
        return pageUrlList.size
    }

    fun resetPage(page: Int) {
        notifyItemChanged(page)
    }

    private class FullScreenPageViewHolder(
        view: View,
        onTapListener: View.OnClickListener
    ) : RecyclerView.ViewHolder(view) {
        private val ivPage: TouchImageView = view.findViewById(R.id.ivPage)
        private val mtvPageTitle: MyTextView = view.findViewById(R.id.mtvPageTitle)

        private val logger: Logger by lazy {
            Logger("FullScreenPageViewHolder")
        }

        init {
            ivPage.setOnClickListener(onTapListener)
            mtvPageTitle.setOnClickListener(onTapListener)
            setupOnTouchEvent()
        }

        fun bindTo(pageUrl: String, page: Int) {
            mtvPageTitle.text = page.toString()
            mtvPageTitle.becomeVisible()
            reloadImage(pageUrl)
            ivPage.resetZoom()
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupOnTouchEvent() {
            ivPage.setOnTouchListener { _, event ->
                if (event.pointerCount >= 2 || (ivPage.canScrollHorizontally(1) && ivPage.canScrollHorizontally(
                        -1
                    ))
                ) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE -> {
                            ivPage.parent.requestDisallowInterceptTouchEvent(true)
                            false
                        }


                        MotionEvent.ACTION_UP -> {
                            ivPage.parent.requestDisallowInterceptTouchEvent(false)
                            true
                        }

                        else -> true
                    }
                } else true
            }
        }

        private fun reloadImage(pageUrl: String) {
            ivPage.doOnGlobalLayout {
                ImageUtils.loadImage(
                    pageUrl,
                    R.drawable.ic_404_not_found,
                    ivPage,
                    onLoadSuccess = {
                        mtvPageTitle.gone()
                        logger.d("$pageUrl is loaded successfully")
                    },
                    onLoadFailed = {
                        logger.e("$pageUrl loading failed")
                    })
            }
        }
    }

    private class WrapContentPageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivPage: ImageView = view.findViewById(R.id.ivPage)
        private val mtvPageTitle: MyTextView = view.findViewById(R.id.mtvPageTitle)

        private val logger: Logger by lazy {
            Logger("WrapContentPageViewHolder")
        }

        fun bindTo(pageUrl: String, page: Int) {
            mtvPageTitle.text = page.toString()
            mtvPageTitle.becomeVisible()
            reloadImage(pageUrl)
        }

        private fun reloadImage(pageUrl: String) {
            ImageUtils.loadImage(pageUrl, R.drawable.ic_404_not_found, ivPage, onLoadSuccess = {
                mtvPageTitle.gone()
                logger.d("$pageUrl is loaded successfully")
            }, onLoadFailed = {
                logger.e("$pageUrl loading failed")
            })
        }
    }
}
