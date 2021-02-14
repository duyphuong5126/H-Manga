package nhdphuong.com.manga.supports

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.TypedValue
import android.widget.TextView
import nhdphuong.com.manga.Constants
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale
import android.graphics.BitmapFactory
import androidx.annotation.WorkerThread
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import java.io.BufferedInputStream
import java.io.OutputStreamWriter
import java.net.URL

interface AppSupportUtils {
    fun downloadAndSaveImage(
        fromUrl: String,
        directory: String,
        fileName: String,
        format: String
    ): String
}

class SupportUtils : AppSupportUtils {
    companion object {
        private val logger: Logger by lazy {
            Logger("SupportUtils")
        }

        private const val MILLISECOND: Long = 1000
        private const val MINUTE: Long = MILLISECOND * 60
        private const val HOUR: Long = MINUTE * 60
        private const val DAY: Long = HOUR * 24
        private const val WEEK: Long = DAY * 7
        private const val MONTH: Long = DAY * 30
        private const val YEAR: Long = DAY * 365

        fun dp2Pixel(context: Context, dp: Int): Int = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp * 1F,
            context.resources.displayMetrics
        ).toInt()

        fun formatBigNumber(number: Long): String {
            return NumberFormat.getNumberInstance(Locale.US).format(number)
        }

        fun getEllipsizedText(textView: TextView): String {
            val text = textView.text.toString()
            val lines = textView.lineCount
            val width = textView.width
            val len = text.length
            val where = TextUtils.TruncateAt.END
            val paint = textView.paint

            val result = StringBuffer()

            var startPosition = 0
            var cnt: Int
            var tmp: Int
            var hasLines = 0

            while (hasLines < lines - 1) {
                cnt = paint.breakText(
                    text,
                    startPosition,
                    len,
                    true,
                    width.toFloat(),
                    null
                )
                if (cnt >= len - startPosition) {
                    result.append(text.substring(startPosition))
                    break
                }

                tmp = text.lastIndexOf('\n', startPosition + cnt - 1)

                if (tmp >= 0 && tmp < startPosition + cnt) {
                    result.append(text.substring(startPosition, tmp + 1))
                    startPosition += tmp + 1
                } else {
                    tmp = text.lastIndexOf(' ', startPosition + cnt - 1)
                    startPosition += if (tmp >= startPosition) {
                        result.append(text.substring(startPosition, tmp + 1))
                        tmp + 1
                    } else {
                        result.append(text.substring(startPosition, cnt))
                        cnt
                    }
                }

                hasLines++
            }

            if (startPosition < len) {
                result.append(
                    TextUtils.ellipsize(
                        text.subSequence(startPosition, len),
                        paint,
                        width.toFloat(),
                        where
                    )
                )
            }

            val ellipsizedText = result.toString()
            return ellipsizedText.substring(0, ellipsizedText.length - 1)
        }

        fun saveImage(bitmap: Bitmap, filePath: String, filename: String, format: String): String {
            val dirs = File(filePath)
            if (!dirs.exists()) {
                dirs.mkdirs()
            }

            val imageFormat = if (format == Constants.PNG_TYPE) {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            val fileType = if (imageFormat == Bitmap.CompressFormat.PNG) {
                Constants.PNG
            } else {
                Constants.JPG
            }
            val resultPath = "$filePath/$filename.$fileType"
            val output = File(resultPath)
            if (!output.exists()) {
                output.createNewFile()
            }

            val outputStream = FileOutputStream(output)
            bitmap.compress(imageFormat, 100, outputStream)
            bitmap.recycle()
            return resultPath
        }

        fun downloadImageBitmap(urlString: String): Bitmap {
            val bitmap: Bitmap
            val connectTimeOut = 10000
            val readTimeOut = 20000
            try {
                val url = URL(urlString)
                val conn = url.openConnection()
                conn.connectTimeout = connectTimeOut
                conn.readTimeout = readTimeOut
                conn.connect()
                val inputStream = conn.getInputStream()
                val bufferedInputStream = BufferedInputStream(inputStream)
                bitmap = BitmapFactory.decodeStream(bufferedInputStream)
                bufferedInputStream.close()
                inputStream.close()
            } catch (e: Exception) {
                logger.e("Downloading $urlString causes exception: $e")
                throw e
            }

            return bitmap
        }

        fun getTimeElapsed(timeElapsed: Long): String {
            NHentaiApp.instance.applicationContext.let { context ->

                val yearsElapsed = timeElapsed / YEAR
                val monthsElapsed = timeElapsed / MONTH
                val weeksElapsed = timeElapsed / WEEK
                val daysElapsed = timeElapsed / DAY
                val hoursElapsed = timeElapsed / HOUR
                val minutesElapsed = timeElapsed / MINUTE
                if (yearsElapsed > 0) {
                    return if (yearsElapsed > 1) {
                        String.format(context.getString(R.string.years_elapsed), yearsElapsed)
                    } else {
                        context.getString(R.string.year_elapsed)
                    }
                }
                if (monthsElapsed > 0) {
                    return if (monthsElapsed > 1) {
                        String.format(context.getString(R.string.months_elapsed), monthsElapsed)
                    } else {
                        context.getString(R.string.month_elapsed)
                    }
                }
                if (weeksElapsed > 0) {
                    return if (weeksElapsed > 1) {
                        String.format(context.getString(R.string.weeks_elapsed), weeksElapsed)
                    } else {
                        context.getString(R.string.week_elapsed)
                    }
                }
                if (daysElapsed > 0) {
                    return if (daysElapsed > 1) {
                        String.format(context.getString(R.string.days_elapsed), daysElapsed)
                    } else {
                        context.getString(R.string.day_elapsed)
                    }
                }
                if (hoursElapsed > 0) {
                    return if (hoursElapsed > 1) {
                        String.format(context.getString(R.string.hours_elapsed), hoursElapsed)
                    } else {
                        context.getString(R.string.hour_elapsed)
                    }
                }
                if (minutesElapsed > 0) {
                    return if (minutesElapsed > 1) {
                        String.format(context.getString(R.string.minutes_elapsed), minutesElapsed)
                    } else {
                        context.getString(R.string.minute_elapsed)
                    }
                }
                return context.getString(R.string.just_now)
            }
        }

        @WorkerThread
        fun saveStringFile(data: String, fileName: String, filePath: String): Boolean {
            logger.d("File name: $fileName, path: $filePath")
            val directories = File(filePath)
            if (!directories.exists()) {
                directories.mkdirs()
            }

            val dataFile = File(directories, "$fileName.txt")
            try {
                dataFile.createNewFile()
                val fileOutputStream = FileOutputStream(dataFile)
                val outputStreamWriter = OutputStreamWriter(fileOutputStream)

                outputStreamWriter.append(data)
                outputStreamWriter.close()

                fileOutputStream.flush()
                fileOutputStream.close()
                return true
            } catch (exception: Exception) {
                logger.e("String isn't saved successfully, error=$exception")
            }
            return false
        }
    }

    override fun downloadAndSaveImage(
        fromUrl: String,
        directory: String,
        fileName: String,
        format: String
    ): String {
        val result = downloadImageBitmap(fromUrl)
        return saveImage(result, directory, fileName, format)
    }
}
