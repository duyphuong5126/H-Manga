package nhdphuong.com.manga.features.admin

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.databinding.FragmentAdminBinding
import nhdphuong.com.manga.views.DialogHelper

class AdminFragment : Fragment(), AdminContract.View {
    companion object {
        const val TAG = "AdminFragment"
        private const val REQUEST_STORAGE_PERMISSION = 3143
    }

    private lateinit var mPresenter: AdminContract.Presenter
    private lateinit var mBinding: FragmentAdminBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentAdminBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.run {
            mtvPagesCount.text = ""
            mbtStartDownloading.setOnClickListener {
                mPresenter.startDownloading()
            }
            mtvPagesDownloaded.text = ""
            mtvArtistsCount.text = ""
            mtvCharactersCount.text = ""
            mtvCategoriesCount.text = ""
            mtvLanguagesCount.text = ""
            mtvParodiesCount.text = ""
            mtvGroupsCount.text = ""
            mtvTagsCount.text = ""
            mtvUnknownTagsCount.text = ""

            spCensored.isChecked = NHentaiApp.instance.isCensored
            spCensored.setOnCheckedChangeListener { _, isChecked ->
                mPresenter.toggleCensored(isChecked)
            }
        }
        mPresenter.start()
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
        mPresenter = presenter
    }

    override fun showNumberOfPages(numOfPages: Long) {
        mBinding.mtvPagesCount.text = getString(R.string.number_of_pages, numOfPages)
    }

    override fun updateDownloadingStatistics(
            downloadedPages: Int,
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
        mBinding.run {
            mtvPagesDownloaded.text = resources.getString(R.string.downloaded_pages, downloadedPages)
            mtvArtistsCount.text = resources.getString(R.string.number_of_artists, artists)
            mtvCharactersCount.text = resources.getString(R.string.number_of_characters, characters)
            mtvCategoriesCount.text = resources.getString(R.string.number_of_categories, categories)
            mtvLanguagesCount.text = resources.getString(R.string.number_of_languages, languages)
            mtvParodiesCount.text = resources.getString(R.string.number_of_parodies, parodies)
            mtvGroupsCount.text = resources.getString(R.string.number_of_groups, groups)
            mtvTagsCount.text = resources.getString(R.string.number_of_tags, tags)
            mtvUnknownTagsCount.text = resources.getString(R.string.number_of_unknown, unknownsTypes)
        }
    }

    override fun updateProgress() {
    }

    override fun showRequestStoragePermission() {
        DialogHelper.showStoragePermissionDialog(activity!!, onOk = {
            requestStoragePermission()
        }, onDismiss = {
            Toast.makeText(context, getString(R.string.toast_storage_permission_require), Toast.LENGTH_SHORT).show()
        })
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
}
