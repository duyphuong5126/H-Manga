package nhdphuong.com.manga.views.customs

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import android.graphics.Typeface
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R


/*
 * Created by nhdphuong on 4/21/18.
 */
class MyTextView(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
) : TextView(context, attrs, defStyle) {
    companion object {
        private const val TAG = "MyTextView"
    }

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        setCustomFont(context, attrs)
    }

    private fun setCustomFont(context: Context, attrs: AttributeSet?) {
        val attribute = context.obtainStyledAttributes(attrs, R.styleable.MyTextView)
        val customFont = attribute.getString(R.styleable.MyTextView_myFont)
        setCustomFont(context, customFont)
        attribute.recycle()
    }

    private fun setCustomFont(ctx: Context, asset: String?): Boolean {
        val tf: Typeface?
        try {
            tf = Typeface.createFromAsset(ctx.assets, asset)
        } catch (e: Exception) {
            Logger.e(TAG, "Could not get typeface: " + e.message)
            return false
        }

        typeface = tf
        return true
    }
}
