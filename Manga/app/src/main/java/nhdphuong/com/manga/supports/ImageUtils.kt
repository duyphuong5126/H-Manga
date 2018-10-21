package nhdphuong.com.manga.supports

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.Target
import nhdphuong.com.manga.Logger

class ImageUtils {
    companion object {
        private const val TAG = "ImageUtils"
        private const val TIME_OUT = 10000
        @SuppressLint("CheckResult")
        fun <IV : ImageView> loadOriginalImage(url: String, defaultResource: Int, imageView: IV) {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(defaultResource)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .timeout(TIME_OUT)
                    .skipMemoryCache(true)
            Glide.with(imageView).load(url).apply(requestOptions).into(imageView)
        }

        @SuppressLint("CheckResult")
        fun <IV : ImageView> loadImage(url: String, defaultResource: Int, imageView: IV) {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(defaultResource)
                    .timeout(TIME_OUT)
                    .skipMemoryCache(true)
            Glide.with(imageView).load(url).apply(requestOptions).into(imageView)
        }

        fun <IV : ImageView> loadImage(url: String, defaultResource: Int, imageView: IV, onLoadSuccess: () -> Unit, onLoadFailed: () -> Unit) {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(defaultResource)
                    .timeout(TIME_OUT)
                    .skipMemoryCache(true)
            Glide.with(imageView).load(url).apply(requestOptions).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    onLoadFailed()
                    return true
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    onLoadSuccess()
                    return false
                }
            }).into(imageView)
        }

        fun <IV : ImageView> loadGifImage(gifResource: Int, imageView: IV) {
            val ivLoadingTarget = DrawableImageViewTarget(imageView)
            Glide.with(imageView).load(gifResource).into(ivLoadingTarget)
        }

        fun <IV : ImageView> clear(imageView: IV) {
            Glide.with(imageView.context).clear(imageView)
        }

        @SuppressLint("CheckResult")
        fun downloadImage(context: Context, url: String, onBitmapReady: (bitmap: Bitmap?) -> Unit) {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .timeout(TIME_OUT)
                    .skipMemoryCache(true)
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(requestOptions)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                            Logger.d(TAG, "Downloading image: $url failed")
                            onBitmapReady(null)
                            return true
                        }

                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            Logger.d(TAG, "Downloading image: $url succeeded")
                            onBitmapReady(resource)
                            return false
                        }
                    }).submit()
        }

        fun downloadImage(context: Context, url: String, width: Int, height: Int): Bitmap {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .timeout(TIME_OUT)
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