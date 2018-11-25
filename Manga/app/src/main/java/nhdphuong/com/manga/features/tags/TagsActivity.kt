package nhdphuong.com.manga.features.tags

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.features.SearchContract
import nhdphuong.com.manga.features.header.HeaderFragment
import nhdphuong.com.manga.features.header.HeaderModule
import nhdphuong.com.manga.features.header.HeaderPresenter
import javax.inject.Inject

class TagsActivity : AppCompatActivity(), SearchContract {
    companion object {
        fun start(activity: Activity, @Tag tagType: String, requestCode: Int) {
            val intent = Intent(activity, TagsActivity::class.java)
            intent.putExtra(Constants.TAG_TYPE, tagType)
            activity.startActivityForResult(intent, requestCode)
        }

        fun start(fragment: Fragment, @Tag tagType: String, requestCode: Int) {
            val intent = Intent(fragment.context, TagsActivity::class.java)
            intent.putExtra(Constants.TAG_TYPE, tagType)
            fragment.startActivityForResult(intent, requestCode)
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

        NHentaiApp.instance.suspendUpdateTagsService()
        var tagsFragment = supportFragmentManager.findFragmentById(R.id.clTagsFragment) as TagsFragment?
        if (tagsFragment == null) {
            tagsFragment = TagsFragment()
            supportFragmentManager.beginTransaction().replace(R.id.clTagsFragment, tagsFragment).commitAllowingStateLoss()
        }
        tagsFragment.setSearchInputListener(this)

        var headerFragment = supportFragmentManager.findFragmentById(R.id.clHeader) as HeaderFragment?
        if (headerFragment == null) {
            headerFragment = HeaderFragment()
            supportFragmentManager.beginTransaction().replace(R.id.clHeader, headerFragment).commitAllowingStateLoss()
        }
        headerFragment.setSearchInputListener(this)
        intent.getStringExtra(Constants.TAG_TYPE)?.let { tag ->
            headerFragment.arguments = getTagBundle(tag)
            headerFragment.setTagChangeListener(tagsFragment)
            NHentaiApp.instance.applicationComponent.plus(TagsModule(tagsFragment, tag),
                    HeaderModule(headerFragment)).inject(this)
        }
    }

    override fun finish() {
        super.finish()
        NHentaiApp.instance.resumeUpdateTagsService()
    }

    override fun onSearchInputted(data: String) {
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.TAG_RESULT, data)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun getTagBundle(@Tag tag: String): Bundle {
        val bundle = Bundle()
        bundle.putString(Constants.TAG_TYPE, tag)
        return bundle
    }
}
