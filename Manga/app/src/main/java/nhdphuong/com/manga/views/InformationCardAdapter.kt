package nhdphuong.com.manga.views

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.supports.SupportUtils
import java.util.LinkedList
import java.util.Collections

/*
 * Created by nhdphuong on 4/15/18.
 */
class InformationCardAdapter(private val tagList: List<Tag>) {
    private val logger: Logger by lazy {
        Logger("InformationCardAdapter")
    }

    private var countTemplate = ""

    @SuppressLint("InflateParams")
    fun loadInfoList(viewGroup: ViewGroup) {
        viewGroup.removeAllViews()
        if (tagList.isEmpty()) {
            return
        }
        val context = viewGroup.context
        countTemplate = context.getString(R.string.count)
        val layoutInflater = LayoutInflater.from(context)
        var tagLine = layoutInflater.inflate(
            R.layout.item_tag_line,
            viewGroup,
            false
        ).findViewById<LinearLayout>(R.id.lineRoot)
        viewGroup.addView(tagLine)
        val viewList = ArrayList<View>()
        for (tag in tagList) {
            val view = InfoCardViewHolder(
                layoutInflater.inflate(
                    R.layout.item_tag,
                    viewGroup,
                    false
                ), tag
            ).view
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            viewList.add(view)
        }
        val totalWidth = viewGroup.measuredWidth
        val totalMargin = SupportUtils.dp2Pixel(context, 6)
        if (viewList.size >= 3) {
            sortViewList(viewList, totalWidth, totalMargin / 2)
        }
        logger.d("Total width: $totalWidth")
        var widthCount = 0
        for (view in viewList) {
            if (view.measuredWidth > totalWidth - totalMargin) {
                view.layoutParams.width = totalWidth - totalMargin
            }
            val itemWidth = view.measuredWidth + totalMargin
            widthCount += itemWidth
            logger.d("Item: $itemWidth, widthCount: $widthCount, total: $totalWidth")
            if (widthCount > totalWidth) {
                tagLine = layoutInflater.inflate(
                    R.layout.item_tag_line,
                    viewGroup,
                    false
                ).findViewById(R.id.lineRoot)
                viewGroup.addView(tagLine)
                widthCount = itemWidth
            }
            tagLine.addView(view)
        }
    }

    private inner class InfoCardViewHolder(
        val view: View,
        private val tag: Tag
    ) : View.OnClickListener {
        private val mTvLabel: TextView = view.findViewById(R.id.tvLabel)
        private val mClickableArea: ConstraintLayout = view.findViewById(R.id.clClickableArea)

        init {
            val context = view.context
            val label = if (NHentaiApp.instance.isCensored) "Censored" else tag.name
            val count = if (tag.count > 0) tag.count else 0
            val countText = String.format(countTemplate, SupportUtils.formatBigNumber(count))
            val finalText = "$label $countText"
            val spannableText = SpannableString(finalText)
            spannableText.setSpan(
                TextAppearanceSpan(context, R.style.InfoCardLabel),
                0,
                label.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableText.setSpan(
                TextAppearanceSpan(context, R.style.InfoCardCount),
                label.length + 1,
                finalText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            mTvLabel.text = spannableText
            mClickableArea.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            mTagSelectedListener.onTagSelected(tag)
        }
    }

    private fun sortViewList(viewList: ArrayList<View>, totalWidth: Int, totalMargin: Int) {
        var anchorId = 0
        val size = viewList.size
        while (anchorId < size - 1) {
            var widthSum = viewList[anchorId].measuredWidth + totalMargin
            var viewId = anchorId + 1
            val suitableViews = LinkedList<Int>()
            while (widthSum <= totalWidth && viewId < size) {
                widthSum += viewList[viewId].measuredWidth + totalMargin
                if (widthSum <= totalWidth) {
                    suitableViews.add(viewId)
                } else {
                    widthSum += viewList[viewId].measuredWidth + totalMargin
                }
                viewId++
            }
            if (suitableViews.isEmpty()) {
                anchorId++
            } else {
                var idOffset = 1
                for (i in suitableViews) {
                    Collections.swap(viewList, i, anchorId + idOffset)
                    idOffset++
                }
                anchorId += suitableViews.size + 1
            }
        }
    }

    interface TagSelectedListener {
        fun onTagSelected(tag: Tag)
    }

    private lateinit var mTagSelectedListener: TagSelectedListener

    fun setTagSelectedListener(tagSelectedListener: TagSelectedListener) {
        mTagSelectedListener = tagSelectedListener
    }
}
