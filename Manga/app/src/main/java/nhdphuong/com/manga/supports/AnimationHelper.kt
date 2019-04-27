package nhdphuong.com.manga.supports

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import nhdphuong.com.manga.R
import nhdphuong.com.manga.views.customs.MyTextView

/*
 * Created by nhdphuong on 5/5/18.
 */
class AnimationHelper {
    companion object {
        private var mSlideOutTop: Animation? = null
        private var mSlideInTop: Animation? = null
        private var mSlideOutBottom: Animation? = null
        private var mSlideInBottom: Animation? = null

        fun startSlideOutTop(activity: Activity, view: View, onAnimationEnd: () -> Unit) {
            if (mSlideOutTop == null) {
                mSlideOutTop = AnimationUtils.loadAnimation(activity, R.anim.slide_out_top)
            }

            mSlideOutTop?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {

                }

                override fun onAnimationStart(p0: Animation?) {
                    onAnimationEnd()
                }
            })

            view.startAnimation(mSlideOutTop)
        }

        fun startSlideOutBottom(activity: Activity, view: View, onAnimationEnd: () -> Unit) {
            if (mSlideOutBottom == null) {
                mSlideOutBottom = AnimationUtils.loadAnimation(activity, R.anim.slide_out_bottom)
            }

            mSlideOutBottom?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {

                }

                override fun onAnimationStart(p0: Animation?) {
                    onAnimationEnd()
                }
            })

            view.startAnimation(mSlideOutBottom)
        }

        fun startSlideInTop(activity: Activity, view: View, onAnimationEnd: () -> Unit) {
            if (mSlideInTop == null) {
                mSlideInTop = AnimationUtils.loadAnimation(activity, R.anim.slide_in_top)
            }

            mSlideInTop?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {

                }

                override fun onAnimationStart(p0: Animation?) {
                    onAnimationEnd()
                }
            })

            view.startAnimation(mSlideInTop)
        }

        fun startSlideInBottom(activity: Activity, view: View, onAnimationEnd: () -> Unit) {
            if (mSlideInBottom == null) {
                mSlideInBottom = AnimationUtils.loadAnimation(activity, R.anim.slide_in_bttom)
            }

            mSlideInBottom?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {

                }

                override fun onAnimationStart(p0: Animation?) {
                    onAnimationEnd()
                }
            })

            view.startAnimation(mSlideInBottom)
        }

        fun startTextRunning(textView: MyTextView) {
            val runningText = AnimationUtils.loadAnimation(textView.context, R.anim.text_running_1)
            textView.startAnimation(runningText)
        }

        fun getRotationAnimation(context: Context): Animation {
            return AnimationUtils.loadAnimation(context, R.anim.rotation)
        }
    }
}
