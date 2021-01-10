package nhdphuong.com.manga.views.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
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

    private val pageMap: HashMap<Int, Boolean> = HashMap()

    init {
        pageMap.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Logger.d(TAG, "onCreateViewHolder viewType: $viewType")
        val layoutResId = if (readerType == VerticalScroll) {
            R.layout.item_book_page_wrapped
        } else {
            R.layout.item_book_page
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return BookReaderViewHolder(view, onTapListener, readerType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Logger.d(TAG, "onBindViewHolder position: $position")
        (holder as BookReaderViewHolder).bindTo(pageUrlList[position], position + 1)
    }

    override fun getItemCount(): Int {
        return pageUrlList.size
    }

    fun resetPageToNormal(page: Int) {
        Logger.d(TAG, "resetPageToNormal page: $page")
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
        onTapListener: View.OnClickListener,
        private val readerType: ReaderType
    ) : RecyclerView.ViewHolder(view) {
        private val ivPage: TouchImageView = view.findViewById(R.id.ivPage)
        private val mtvPageTitle: MyTextView = view.findViewById(R.id.mtvPageTitle)

        init {
            ivPage.setOnClickListener(onTapListener)
            mtvPageTitle.setOnClickListener(onTapListener)
        }

        @SuppressLint("ClickableViewAccessibility")
        fun bindTo(pageUrl: String, page: Int) {
            mtvPageTitle.text = page.toString()
            mtvPageTitle.becomeVisible()
            if (readerType == VerticalScroll) {
                ivPage.setOnTouchListener { _, event ->
                    return@setOnTouchListener if (event.pointerCount >= 2
                        || (ivPage.canScrollHorizontally(1) && ivPage.canScrollHorizontally(-1))
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
            } else {
                ivPage.resetZoom()
            }
            reloadImage(pageUrl)
        }

        private fun reloadImage(pageUrl: String) {
            if (readerType == VerticalScroll) {
                ImageUtils.loadImage(pageUrl, R.drawable.ic_404_not_found, ivPage, onLoadSuccess = {
                    mtvPageTitle.gone()
                    Logger.d(TAG, "$pageUrl is loaded successfully")
                }, onLoadFailed = {
                    Logger.d(TAG, "$pageUrl loading failed")
                })
            } else {
                ivPage.doOnGlobalLayout {
                    ImageUtils.loadImage(
                        pageUrl,
                        R.drawable.ic_404_not_found,
                        ivPage,
                        onLoadSuccess = {
                            mtvPageTitle.gone()
                            Logger.d(TAG, "$pageUrl is loaded successfully")
                        },
                        onLoadFailed = {
                            Logger.d(TAG, "$pageUrl loading failed")
                        })
                }
            }
        }
    }

    companion object {
        private const val TAG = "BookReaderAdapter"
    }
}
