package nhdphuong.com.manga.supports

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.Target

class GlideUtils {
    companion object {
        @SuppressLint("CheckResult")
        fun <IV : ImageView> loadOriginalImage(url: String, defaultResource: Int, imageView: IV) {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(defaultResource)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .skipMemoryCache(true)
            Glide.with(imageView).load(url).apply(requestOptions).into(imageView)
        }

        @SuppressLint("CheckResult")
        fun <IV : ImageView> loadImage(url: String, defaultResource: Int, imageView: IV) {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(defaultResource)
                    .skipMemoryCache(true)
            Glide.with(imageView).load(url).apply(requestOptions).into(imageView)
        }

        fun <IV : ImageView> loadImage(url: String, defaultResource: Int, imageView: IV, listener: RequestListener<Drawable>) {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(defaultResource)
                    .skipMemoryCache(true)
            Glide.with(imageView).load(url).apply(requestOptions).listener(listener).into(imageView)
        }

        fun <IV : ImageView> loadGifImage(gifResource: Int, imageView: IV) {
            val ivLoadingTarget = DrawableImageViewTarget(imageView)
            Glide.with(imageView).load(gifResource).into(ivLoadingTarget)
        }

        fun <IV : ImageView> clear(imageView: IV) {
            Glide.with(imageView.context).clear(imageView)
        }

        fun downloadImage(context: Context, url: String, width: Int, height: Int): Bitmap {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(true)
            val future = Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(requestOptions)
                    .submit(width, height)
            val bitmap = future.get()
            future.cancel(true)
            return bitmap
        }
    }
}