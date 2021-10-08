package nhdphuong.com.manga.features.deeplink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nhdphuong.com.manga.Constants.Companion.ACTION_OPEN_BOOK_EXTERNALLY
import nhdphuong.com.manga.Constants.Companion.ARTIST
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.CATEGORY
import nhdphuong.com.manga.Constants.Companion.CHARACTER
import nhdphuong.com.manga.Constants.Companion.DEEP_LINK_BOOK_PATH
import nhdphuong.com.manga.Constants.Companion.GROUP
import nhdphuong.com.manga.Constants.Companion.LANGUAGE
import nhdphuong.com.manga.Constants.Companion.PARODY
import nhdphuong.com.manga.Constants.Companion.TAG
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.features.home.HomeActivity
import nhdphuong.com.manga.features.preview.BookPreviewActivity

class DeepLinkActivity : AppCompatActivity() {
    private val logger = Logger("DeepLinkActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.run {
            data?.path?.split('/')?.filter(String::isNotBlank)?.let {
                logger.d("data=$it")
                if (it.size >= 2) {
                    val path = it[0].trim().lowercase()
                    val value = it[1].trim().lowercase()
                    when (path) {
                        DEEP_LINK_BOOK_PATH -> {
                            if (BookPreviewActivity.isStarted) {
                                val openBookData = Bundle().apply {
                                    putString(BOOK_ID, value)
                                }
                                BroadCastReceiverHelper.sendBroadCast(
                                    this@DeepLinkActivity,
                                    ACTION_OPEN_BOOK_EXTERNALLY,
                                    openBookData
                                )
                            } else {
                                HomeActivity.start(this@DeepLinkActivity, bookId = value)
                            }
                        }
                        ARTIST, CHARACTER, CATEGORY, LANGUAGE, PARODY, GROUP, TAG -> {
                            if (!BookPreviewActivity.isStarted) {
                                HomeActivity.startSearching(
                                    this@DeepLinkActivity,
                                    searchTerm = value
                                )
                            }
                        }
                        else -> HomeActivity.start(this@DeepLinkActivity)
                    }
                }
            }
        }
        finish()
    }
}