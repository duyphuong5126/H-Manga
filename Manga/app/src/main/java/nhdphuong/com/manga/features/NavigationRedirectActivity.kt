package nhdphuong.com.manga.features

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import nhdphuong.com.manga.R
import nhdphuong.com.manga.features.home.HomeActivity

class NavigationRedirectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isTaskRoot) {
            HomeActivity.start(this)
        }
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
