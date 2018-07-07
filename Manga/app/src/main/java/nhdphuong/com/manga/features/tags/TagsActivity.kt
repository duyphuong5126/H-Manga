package nhdphuong.com.manga.features.tags

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.features.header.HeaderFragment
import nhdphuong.com.manga.features.header.HeaderModule
import nhdphuong.com.manga.features.header.HeaderPresenter
import javax.inject.Inject

class TagsActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context, @Tag tagType: String) {
            val intent = Intent(context, TagsActivity::class.java)
            intent.putExtra(Constants.TAG_TYPE, tagType)
            context.startActivity(intent)
        }
    }

    @Suppress("unused")
    @Inject
    lateinit var mTagsPresenter: TagsPresenter

    @Suppress("unused")
    @Inject
    lateinit var mHeaderPresenter: HeaderPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tags)

        var tagsFragment = supportFragmentManager.findFragmentById(R.id.clTagsFragment) as TagsFragment?
        if (tagsFragment == null) {
            tagsFragment = TagsFragment()
            supportFragmentManager.beginTransaction().replace(R.id.clTagsFragment, tagsFragment).commitAllowingStateLoss()
        }

        var headerFragment = supportFragmentManager.findFragmentById(R.id.clHeader) as HeaderFragment?
        if (headerFragment == null) {
            headerFragment = HeaderFragment()
            supportFragmentManager.beginTransaction().replace(R.id.clHeader, headerFragment).commitAllowingStateLoss()
        }
        intent.getStringExtra(Constants.TAG_TYPE)?.let { tag ->
            headerFragment.arguments = getTagBundle(tag)
            headerFragment.setTagChangeListener(tagsFragment)
            NHentaiApp.instance.applicationComponent.plus(TagsModule(tagsFragment, tag),
                    HeaderModule(headerFragment)).inject(this)
        }
    }

    private fun getTagBundle(@Tag tag: String): Bundle {
        val bundle = Bundle()
        bundle.putString(Constants.TAG_TYPE, tag)
        return bundle
    }
}
