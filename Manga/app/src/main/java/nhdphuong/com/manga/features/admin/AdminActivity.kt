package nhdphuong.com.manga.features.admin

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
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

                beginTransaction().replace(R.id.layout_root, adminFragment, AdminFragment.TAG)
                        .addToBackStack(AdminFragment.TAG)
                        .commitAllowingStateLoss()
            }

            NHentaiApp.instance.applicationComponent.plus(AdminModule(adminFragment)).inject(this@AdminActivity)
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
