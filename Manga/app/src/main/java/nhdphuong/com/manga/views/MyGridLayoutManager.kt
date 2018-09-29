package nhdphuong.com.manga.views

import android.content.Context
import android.support.v7.widget.GridLayoutManager

/*
 * Created by nhdphuong on 4/29/18.
 */
open class MyGridLayoutManager(context: Context, spanCount: Int) : GridLayoutManager(context, spanCount) {
    override fun canScrollVertically(): Boolean {
        return false
    }
}