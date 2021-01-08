package nhdphuong.com.manga.views

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

class PreLoadingLinearLayoutManager(
    context: Context,
    orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false,
    private val pages: Int = 1
) : LinearLayoutManager(context, orientation, reverseLayout) {
    private var orientationHelper: OrientationHelper =
        OrientationHelper.createOrientationHelper(this, orientation)

    override fun getExtraLayoutSpace(state: RecyclerView.State?): Int {
        return orientationHelper.totalSpace * pages
    }
}