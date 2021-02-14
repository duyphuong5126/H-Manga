package nhdphuong.com.manga.features.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import javax.inject.Inject

class AdminActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AdminActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Suppress("unused")
    @Inject
    lateinit var mAdminPresenter: AdminPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        supportFragmentManager.run {
            var adminFragment = findFragmentById(R.id.layout_root) as AdminFragment?
            if (adminFragment == null) {
                adminFragment = AdminFragment()

                val tag = "AdminFragment"
                beginTransaction().replace(R.id.layout_root, adminFragment, tag)
                    .addToBackStack(tag)
                    .commitAllowingStateLoss()
            }

            NHentaiApp.instance.applicationComponent.plus(
                AdminModule(adminFragment)
            ).inject(
                this@AdminActivity
            )
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
