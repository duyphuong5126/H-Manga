package nhdphuong.com.manga.views

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.recyclerview.widget.SnapHelper
import kotlin.math.max
import kotlin.math.min

fun View.becomeVisible() {
    visibility = View.VISIBLE
}

fun View.becomeVisibleIf(condition: Boolean, otherWiseVisibility: Int = View.GONE) {
    visibility = if (condition) View.VISIBLE else otherWiseVisibility
}

fun View.becomeInvisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.doOnGlobalLayout(task: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            task.invoke()
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

fun RecyclerView.doOnScrolled(task: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            task()
        }
    })
}

fun RecyclerView.doOnScrollToBottom(distanceFromBottom: Int = 0, task: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val scrolledDown = dy > 0
            val layoutManager = layoutManager
            val adapter = recyclerView.adapter
            if (scrolledDown && layoutManager != null && adapter != null) {
                layoutManager.getChildAt(layoutManager.childCount - 1)?.let { lastChild ->
                    val lastChildPosition =
                        recyclerView.getChildViewHolder(lastChild).absoluteAdapterPosition
                    if (lastChildPosition >= adapter.itemCount - distanceFromBottom - 1) {
                        post(task)
                    }
                }
            }
        }
    })
}

fun NestedScrollView.doOnScrollToBottom(
    linearLayoutManager: LinearLayoutManager,
    distanceFromBottom: Int,
    task: () -> Unit
) {
    setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
        val firstVisible = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
        val lastVisible = linearLayoutManager.findLastCompletelyVisibleItemPosition()
        if (scrollY > oldScrollY && lastVisible - firstVisible >= distanceFromBottom) {
            val onScrollChangeListener: NestedScrollView.OnScrollChangeListener? = null
            this.setOnScrollChangeListener(onScrollChangeListener)
            task()
        }
    })
}

fun RecyclerView.addSnapPositionChangedListener(
    snapHelper: SnapHelper,
    positionChanged: (newPosition: Int) -> Unit
) {
    var lastPosition = RecyclerView.NO_POSITION
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == SCROLL_STATE_IDLE) {
                recyclerView.layoutManager?.let { layoutManager ->
                    snapHelper.findSnapView(layoutManager)?.let(layoutManager::getPosition)?.let {
                        if (lastPosition != it) {
                            lastPosition = it
                            positionChanged.invoke(lastPosition)
                        }
                    }
                }
            }
        }
    })
}

fun RecyclerView.scrollToAroundPosition(position: Int, additionalStep: Int = 0) {
    if (additionalStep <= 0) {
        scrollToPosition(position)
    } else {
        adapter?.itemCount?.takeIf { it > 0 }?.let { itemCount ->
            (layoutManager as? LinearLayoutManager)?.let {
                val firstPos = it.findFirstVisibleItemPosition()
                val lasPos = it.findLastVisibleItemPosition()
                if (firstPos >= 0 && lasPos >= 0) {
                    val middlePos = (firstPos + lasPos) / 2
                    when (position) {
                        in firstPos + 1 until lasPos -> scrollToPosition(position)
                        else -> {
                            if (position > middlePos) {
                                min(itemCount - 1, position + additionalStep)
                            } else {
                                max(0, position - additionalStep)
                            }.let(this::scrollToPosition)
                        }
                    }
                }
            }
        }
    }
}
