package nhdphuong.com.manga.views

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Logger

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
                        recyclerView.getChildViewHolder(lastChild).adapterPosition
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
        Logger.d(
            "doOnScrollToBottom",
            "firstVisible=$firstVisible, lastVisible=$lastVisible, scrollY=$scrollY, oldScrollY=$oldScrollY"
        )
        if (scrollY > oldScrollY && lastVisible - firstVisible >= distanceFromBottom) {
            val onScrollChangeListener: NestedScrollView.OnScrollChangeListener? = null
            this.setOnScrollChangeListener(onScrollChangeListener)
            task()
        }
    })
}
