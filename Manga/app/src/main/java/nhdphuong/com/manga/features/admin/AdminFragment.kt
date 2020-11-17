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
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
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
import nhdphuong.com.manga.views.customs.MyButton
import nhdphuong.com.manga.views.customs.MyTextView
import nhdphuong.com.manga.views.showStoragePermissionDialog
import nhdphuong.com.manga.views.showTagDataBeingDownloadedDialog

class AdminFragment : Fragment(), AdminContract.View {
    companion object {
        const val TAG = "AdminFragment"
        private const val REQUEST_STORAGE_PERMISSION = 3143

        private enum class DownloadingSwitch {
            Start, Stop
        }
    }

    private lateinit var mbtStartDownloading: MyButton
    private lateinit var mtvArtistsCount: MyTextView
    private lateinit var mtvCategoriesCount: MyTextView
    private lateinit var mtvCharactersCount: MyTextView
    private lateinit var mtvGroupsCount: MyTextView
    private lateinit var mtvLanguagesCount: MyTextView
    private lateinit var mtvPagesCount: MyTextView
    private lateinit var mtvPagesDownloaded: MyTextView
    private lateinit var mtvParodiesCount: MyTextView
    private lateinit var mtvTagsCount: MyTextView
    private lateinit var mtvUnknownTagsCount: MyTextView
    private lateinit var spCensored: SwitchCompat

    private lateinit var presenter: AdminContract.Presenter

    private var currentDownloadingSwitch: DownloadingSwitch = DownloadingSwitch.Start
        set(value) {
            if (value != field) {
                changeDownloadButtonStatus(value)
            }
            field = value
        }

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
        setUpUI(view)
        mtvPagesCount.text = ""
        mbtStartDownloading.setOnClickListener {
            if (!TagsDownloadingService.isTagBeingDownloaded) {
                presenter.startDownloading()
            } else {
                activity?.showTagDataBeingDownloadedDialog {
                    TagsDownloadingService.stopCurrentTask()
                    currentDownloadingSwitch = DownloadingSwitch.Start
                }
            }
        }

        clearAllData()

        spCensored.isChecked = NHentaiApp.instance.isCensored
        spCensored.setOnCheckedChangeListener { _, isChecked ->
            presenter.toggleCensored(isChecked)
        }
        presenter.start()
    }

    override fun onStart() {
        super.onStart()
        currentDownloadingSwitch = if (TagsDownloadingService.isTagBeingDownloaded) {
            DownloadingSwitch.Stop
        } else {
            DownloadingSwitch.Start
        }
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
        mtvPagesCount.text = getString(R.string.number_of_pages, numOfPages)
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
        mtvPagesDownloaded.text = resources.getString(R.string.downloaded_pages, downloadedPages)
        mtvArtistsCount.text = resources.getString(R.string.number_of_artists, artists)
        mtvCharactersCount.text = resources.getString(R.string.number_of_characters, characters)
        mtvCategoriesCount.text = resources.getString(R.string.number_of_categories, categories)
        mtvLanguagesCount.text = resources.getString(R.string.number_of_languages, languages)
        mtvParodiesCount.text = resources.getString(R.string.number_of_parodies, parodies)
        mtvGroupsCount.text = resources.getString(R.string.number_of_groups, groups)
        mtvTagsCount.text = resources.getString(R.string.number_of_tags, tags)
        mtvUnknownTagsCount.text =
            resources.getString(R.string.number_of_unknown, unknownsTypes)
    }

    override fun startDownloadingTagData(numberOfPage: Long) {
        currentDownloadingSwitch = DownloadingSwitch.Stop
        context?.let {
            Logger.d(TAG, "isTagBeingDownloaded=${TagsDownloadingService.isTagBeingDownloaded}")
            TagsDownloadingService.start(it, numberOfPage)
        }
    }

    override fun showRequestStoragePermission() {
        activity?.showStoragePermissionDialog(onOk = {
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

    private fun setUpUI(rootView: View) {
        mbtStartDownloading = rootView.findViewById(R.id.mbt_start_downloading)
        mtvArtistsCount = rootView.findViewById(R.id.mtv_artists_count)
        mtvCategoriesCount = rootView.findViewById(R.id.mtv_categories_count)
        mtvCharactersCount = rootView.findViewById(R.id.mtv_characters_count)
        mtvGroupsCount = rootView.findViewById(R.id.mtv_groups_count)
        mtvLanguagesCount = rootView.findViewById(R.id.mtv_languages_count)
        mtvPagesCount = rootView.findViewById(R.id.mtv_pages_count)
        mtvPagesDownloaded = rootView.findViewById(R.id.mtv_pages_downloaded)
        mtvParodiesCount = rootView.findViewById(R.id.mtv_parodies_count)
        mtvTagsCount = rootView.findViewById(R.id.mtv_tags_count)
        mtvUnknownTagsCount = rootView.findViewById(R.id.mtv_unknown_tags_count)
        spCensored = rootView.findViewById(R.id.sp_censored)
    }

    private fun requestStoragePermission() {
        val storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(storagePermission, REQUEST_STORAGE_PERMISSION)
    }

    private fun clearAllData() {
        mtvPagesDownloaded.text = ""
        mtvArtistsCount.text = ""
        mtvCharactersCount.text = ""
        mtvCategoriesCount.text = ""
        mtvLanguagesCount.text = ""
        mtvParodiesCount.text = ""
        mtvGroupsCount.text = ""
        mtvTagsCount.text = ""
        mtvUnknownTagsCount.text = ""
    }

    private fun changeDownloadButtonStatus(newStatus: DownloadingSwitch) {
        Logger.d(TAG, "currentDownloadingSwitch=$newStatus")
        mbtStartDownloading.text = getString(
            if (newStatus == DownloadingSwitch.Start) {
                R.string.start_downloading
            } else {
                R.string.stop_downloading
            }
        )
    }
}
