package nhdphuong.com.manga.views

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.widget.NestedScrollView

fun View.becomeVisible() {
    visibility = View.VISIBLE
}

fun View.becomeVisibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
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

fun NestedScrollView.doOnScrollToBottom(task: () -> Unit) {
    setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
        if (!this.canScrollVertically(1)) {
            val onScrollChangeListener: NestedScrollView.OnScrollChangeListener? = null
            this.setOnScrollChangeListener(onScrollChangeListener)
            task()
        }
    })
}
