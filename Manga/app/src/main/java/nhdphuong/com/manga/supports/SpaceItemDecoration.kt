package nhdphuong.com.manga.supports

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/*
 * Created by nhdphuong on 3/17/18.
 */
class SpaceItemDecoration() : RecyclerView.ItemDecoration() {
    private var mSpace: Int = 0
    private var mShowFirstDivider = false
    private var mShowLastDivider = false

    private var mOrientation = -1

    constructor(context: Context, attributeSet: AttributeSet) : this() {
        mSpace = 0
    }

    @Suppress("unused")
    constructor(
        context: Context,
        attributeSet: AttributeSet,
        showFirstDivider: Boolean,
        showLastDivider: Boolean
    ) : this(context, attributeSet) {
        mShowFirstDivider = showFirstDivider
        mShowLastDivider = showLastDivider
    }

    constructor(spaceInDp: Int, context: Context) : this() {
        val r = context.resources
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            spaceInDp.toFloat(),
            r.displayMetrics
        )
        mSpace = px.toInt()
    }

    @Suppress("unused")
    constructor(
        spaceInDp: Int,
        context: Context,
        showFirstDivider: Boolean,
        showLastDivider: Boolean
    ) : this(spaceInDp, context) {
        mShowFirstDivider = showFirstDivider
        mShowLastDivider = showLastDivider
    }

    constructor(context: Context, resId: Int) : this() {
        mSpace = context.resources.getDimensionPixelSize(resId)
    }

    constructor(
        context: Context,
        resId: Int,
        showFirstDivider: Boolean,
        showLastDivider: Boolean
    ) : this(context, resId) {
        mShowFirstDivider = showFirstDivider
        mShowLastDivider = showLastDivider
    }

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (mSpace == 0) {
            return
        }

        if (mOrientation == -1)
            getOrientation(parent)

        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION || position == 0 && !mShowFirstDivider) {
            return
        }

        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.top = mSpace
            if (mShowLastDivider && position == state.itemCount - 1) {
                outRect.bottom = outRect.top
            }
        } else if (mOrientation == LinearLayoutManager.HORIZONTAL) {
            outRect.left = mSpace
            if (mShowLastDivider && position == state.itemCount - 1) {
                outRect.right = outRect.left
            }
        } else {
            outRect.top = mSpace
            if (mShowLastDivider && position == state.itemCount - 1) {
                outRect.bottom = outRect.top
            }
            outRect.left = mSpace
            if (mShowLastDivider && position == state.itemCount - 1) {
                outRect.right = outRect.left
            }
        }
    }

    private fun getOrientation(parent: RecyclerView): Int {
        if (mOrientation == -1) {
            mOrientation = when (parent.layoutManager) {
                is GridLayoutManager -> 3
                is LinearLayoutManager -> {
                    val layoutManager = parent.layoutManager as LinearLayoutManager
                    layoutManager.orientation
                }
                else -> throw IllegalStateException(
                    "DividerItemDecoration can only be used with a LinearLayoutManager."
                )
            }
        }
        return mOrientation
    }
}
