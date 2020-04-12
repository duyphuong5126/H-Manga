package nhdphuong.com.manga.views

import android.view.View
import android.view.ViewTreeObserver

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
