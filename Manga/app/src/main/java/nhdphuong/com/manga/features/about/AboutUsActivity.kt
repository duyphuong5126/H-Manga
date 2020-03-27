package nhdphuong.com.manga.features.about

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_about_us.tvTwitterInfo
import kotlinx.android.synthetic.main.activity_about_us.tvEmailInfo
import kotlinx.android.synthetic.main.activity_about_us.tvRepositoryInfo
import kotlinx.android.synthetic.main.activity_about_us.tvVersionLabel
import kotlinx.android.synthetic.main.activity_about_us.ibBack
import nhdphuong.com.manga.BuildConfig
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.addClickAbleText
import nhdphuong.com.manga.supports.openEmailApp
import nhdphuong.com.manga.supports.openUrl

class AboutUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        tvTwitterInfo.addClickAbleText(TWITTER_URL, TWITTER_LABEL) { twitterUrl ->
            this.openUrl(twitterUrl)
        }
        tvEmailInfo.addClickAbleText(EMAIL, EMAIL) { emailAddress ->
            this.openEmailApp(emailAddress, BuildConfig.VERSION_NAME)
        }
        tvRepositoryInfo.addClickAbleText(REPOSITORY_URL, REPOSITORY_LABEL) { repositoryUrl ->
            this.openUrl(repositoryUrl)
        }
        tvVersionLabel.text = getString(R.string.version_template, BuildConfig.VERSION_NAME)
        ibBack.setOnClickListener {
            onBackPressed()
        }
    }

    companion object {
        private const val TWITTER_LABEL = "Nonoka (@Nonoka5126)"
        private const val TWITTER_URL = "https://twitter.com/Nonoka5126"
        private const val REPOSITORY_LABEL = "duyphuong5126"
        private const val REPOSITORY_URL = "https://github.com/duyphuong5126/H-Manga/releases"
        private const val EMAIL = "kanade5126@gmail.com"

        @JvmStatic
        fun start(fromContext: Context) {
            val intent = Intent(fromContext, AboutUsActivity::class.java)
            fromContext.startActivity(intent)
        }
    }
}
