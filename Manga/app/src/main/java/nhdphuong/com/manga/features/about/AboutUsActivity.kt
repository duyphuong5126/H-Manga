package nhdphuong.com.manga.features.about

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_about_us.tvTwitterInfo
import kotlinx.android.synthetic.main.activity_about_us.tvEmailInfo
import kotlinx.android.synthetic.main.activity_about_us.tvRepositoryInfo
import kotlinx.android.synthetic.main.activity_about_us.tvVersionLabel
import kotlinx.android.synthetic.main.activity_about_us.tvTagVersionLabel
import kotlinx.android.synthetic.main.activity_about_us.ibBack
import nhdphuong.com.manga.BuildConfig
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.addClickAbleText
import nhdphuong.com.manga.supports.openEmailApp
import nhdphuong.com.manga.supports.openUrl
import nhdphuong.com.manga.views.customs.MyTextView
import javax.inject.Inject

class AboutUsActivity : AppCompatActivity(), AboutUsContract.View {
    @Inject
    lateinit var presenter: AboutUsContract.Presenter

    private lateinit var twitterInfo: MyTextView
    private lateinit var emailInfo: MyTextView
    private lateinit var repositoryInfo: MyTextView
    private lateinit var versionLabel: MyTextView
    private lateinit var tagVersionLabel: MyTextView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        NHentaiApp.instance.applicationComponent.plus(AboutUsModule(this)).inject(this)

        setUpView()
        twitterInfo.addClickAbleText(TWITTER_URL, TWITTER_LABEL) { twitterUrl ->
            this.openUrl(twitterUrl)
        }
        emailInfo.addClickAbleText(EMAIL, EMAIL) { emailAddress ->
            this.openEmailApp(emailAddress, BuildConfig.VERSION_NAME)
        }
        repositoryInfo.addClickAbleText(REPOSITORY_URL, REPOSITORY_LABEL) { repositoryUrl ->
            this.openUrl(repositoryUrl)
        }
        versionLabel.text = getString(R.string.app_version_template, BuildConfig.VERSION_NAME)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.setUp()
    }

    override fun onDestroy() {
        presenter.clear()
        super.onDestroy()
    }

    override fun showTagDataVersion(versionCode: String) {
        tagVersionLabel.text = getString(R.string.tag_data_version_template, versionCode)
    }

    private fun setUpView() {
        twitterInfo = tvTwitterInfo
        emailInfo = tvEmailInfo
        repositoryInfo = tvRepositoryInfo
        versionLabel = tvVersionLabel
        tagVersionLabel = tvTagVersionLabel
        backButton = ibBack
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
