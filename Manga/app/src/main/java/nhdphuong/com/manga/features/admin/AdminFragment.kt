package nhdphuong.com.manga.features.admin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_admin.mbt_start_downloading
import kotlinx.android.synthetic.main.fragment_admin.mtv_artists_count
import kotlinx.android.synthetic.main.fragment_admin.mtv_categories_count
import kotlinx.android.synthetic.main.fragment_admin.mtv_characters_count
import kotlinx.android.synthetic.main.fragment_admin.mtv_groups_count
import kotlinx.android.synthetic.main.fragment_admin.mtv_languages_count
import kotlinx.android.synthetic.main.fragment_admin.mtv_pages_count
import kotlinx.android.synthetic.main.fragment_admin.mtv_pages_downloaded
import kotlinx.android.synthetic.main.fragment_admin.mtv_parodies_count
import kotlinx.android.synthetic.main.fragment_admin.mtv_tags_count
import kotlinx.android.synthetic.main.fragment_admin.mtv_unknown_tags_count
import kotlinx.android.synthetic.main.fragment_admin.sp_censored
import nhdphuong.com.manga.Constants.Companion.ACTION_TAGS_DOWNLOADING_COMPLETED
import nhdphuong.com.manga.Constants.Companion.ACTION_TAGS_DOWNLOADING_FAILED
import nhdphuong.com.manga.Constants.Companion.ACTION_TAGS_DOWNLOADING_PROGRESS
import nhdphuong.com.manga.Constants.Companion.DOWNLOADED_PAGES
import nhdphuong.com.manga.Constants.Companion.TAGS_DOWNLOADING_RESULT
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.service.TagsDownloadingService
import nhdphuong.com.manga.service.TagsDownloadingService.Companion.TagDownloadingResult
import nhdphuong.com.manga.views.DialogHelper

class AdminFragment : Fragment(), AdminContract.View {
    companion object {
        const val TAG = "AdminFragment"
        private const val REQUEST_STORAGE_PERMISSION = 3143
    }

    private lateinit var presenter: AdminContract.Presenter

    private val tagDownloadingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_TAGS_DOWNLOADING_PROGRESS -> {
                    val downloadedPages = intent.getIntExtra(DOWNLOADED_PAGES, 0).toLong()
                    intent.getParcelableExtra<TagDownloadingResult>(TAGS_DOWNLOADING_RESULT)?.let {
                        updateDownloadingStatistics(
                            downloadedPages,
                            it.artists,
                            it.characters,
                            it.categories,
                            it.languages,
                            it.parodies,
                            it.groups,
                            it.tags,
                            it.unknownTypes
                        )
                    }
                }

                ACTION_TAGS_DOWNLOADING_FAILED -> {
                    clearAllData()
                }

                ACTION_TAGS_DOWNLOADING_COMPLETED -> {
                    clearAllData()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mbt_start_downloading?.setOnClickListener {
            presenter.startDownloading()
        }

        clearAllData()

        sp_censored?.isChecked = NHentaiApp.instance.isCensored
        sp_censored?.setOnCheckedChangeListener { _, isChecked ->
            presenter.toggleCensored(isChecked)
        }
        presenter.start()
    }

    override fun onStart() {
        super.onStart()
        BroadCastReceiverHelper.registerBroadcastReceiver(
            context,
            tagDownloadingReceiver,
            ACTION_TAGS_DOWNLOADING_PROGRESS,
            ACTION_TAGS_DOWNLOADING_FAILED,
            ACTION_TAGS_DOWNLOADING_COMPLETED
        )
    }

    override fun onStop() {
        super.onStop()
        BroadCastReceiverHelper.unRegisterBroadcastReceiver(context, tagDownloadingReceiver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            val permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                showRequestStoragePermission()
            }
            val result = if (permissionGranted) "granted" else "denied"
            Logger.d(TAG, "Storage permission is $result")
        }
    }

    override fun setPresenter(presenter: AdminContract.Presenter) {
        this.presenter = presenter
    }

    override fun showNumberOfPages(numOfPages: Long) {
        mtv_pages_count?.text = getString(R.string.number_of_pages, numOfPages)
    }

    private fun updateDownloadingStatistics(
        downloadedPages: Long,
        artists: Int,
        characters: Int,
        categories: Int,
        languages: Int,
        parodies: Int,
        groups: Int,
        tags: Int,
        unknownsTypes: Int
    ) {
        val resources = resources
        mtv_pages_downloaded?.text = resources.getString(R.string.downloaded_pages, downloadedPages)
        mtv_artists_count?.text = resources.getString(R.string.number_of_artists, artists)
        mtv_characters_count?.text = resources.getString(R.string.number_of_characters, characters)
        mtv_categories_count?.text = resources.getString(R.string.number_of_categories, categories)
        mtv_languages_count?.text = resources.getString(R.string.number_of_languages, languages)
        mtv_parodies_count?.text = resources.getString(R.string.number_of_parodies, parodies)
        mtv_groups_count?.text = resources.getString(R.string.number_of_groups, groups)
        mtv_tags_count?.text = resources.getString(R.string.number_of_tags, tags)
        mtv_unknown_tags_count?.text =
            resources.getString(R.string.number_of_unknown, unknownsTypes)
    }

    override fun startDownloadingTagData(numberOfPage: Long) {
        activity?.let {
            Logger.d(TAG, "isTagBeingDownloaded=${TagsDownloadingService.isTagBeingDownloaded}")
            if (!TagsDownloadingService.isTagBeingDownloaded) {
                TagsDownloadingService.start(it, numberOfPage)
            } else {
                DialogHelper.showTagDataBeingDownloadedDialog(it)
            }
        }
    }

    override fun showRequestStoragePermission() {
        DialogHelper.showStoragePermissionDialog(activity!!, onOk = {
            requestStoragePermission()
        }, onDismiss = {
            Toast.makeText(
                context,
                getString(R.string.toast_storage_permission_require),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    override fun restartApp() {
        NHentaiApp.instance.restartApp()
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun isActive(): Boolean = isAdded


    private fun requestStoragePermission() {
        val storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(storagePermission, REQUEST_STORAGE_PERMISSION)
    }

    private fun clearAllData() {
        mtv_pages_count?.text = ""
        mtv_pages_downloaded?.text = ""
        mtv_artists_count?.text = ""
        mtv_characters_count?.text = ""
        mtv_categories_count?.text = ""
        mtv_languages_count?.text = ""
        mtv_parodies_count?.text = ""
        mtv_groups_count?.text = ""
        mtv_tags_count?.text = ""
        mtv_unknown_tags_count?.text = ""
    }
}
