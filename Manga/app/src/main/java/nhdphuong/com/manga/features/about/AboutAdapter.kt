package nhdphuong.com.manga.features.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.R
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.CurrentAppVersion
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.TagDataVersion
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.AppVersionsCenter
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.SupportEmail
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.SupportTwitter
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.AvailableVersion
import nhdphuong.com.manga.features.about.uimodel.AboutUiModel.NewVersionAvailable
import nhdphuong.com.manga.supports.addClickAbleText
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.customs.MyTextView

class AboutAdapter(
    private val aboutList: List<AboutUiModel>,
    private val aboutCallback: AboutCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val beingInstalledVersion = BeingInstalledVersion()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CURRENT_APP_VERSION,
            TAG_DATA_VERSION,
            NEW_VERSION_AVAILABLE -> {
                LabelViewHolder(inflater.inflate(R.layout.item_about_label, parent, false))
            }
            APP_VERSION_CENTER,
            SUPPORT_EMAIL,
            SUPPORT_TWITTER -> {
                UrlViewHolder(inflater.inflate(R.layout.item_about_url, parent, false))
            }

            else -> {
                AppVersionViewHolder(
                    inflater.inflate(
                        R.layout.item_about_app_version,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        when (val aboutItem = aboutList[position]) {
            is CurrentAppVersion -> {
                (holder as LabelViewHolder).setText(
                    context.getString(
                        R.string.app_version_template,
                        aboutItem.versionCode
                    )
                )
            }
            is TagDataVersion -> {
                (holder as LabelViewHolder).setText(
                    context.getString(
                        R.string.tag_data_version_template,
                        aboutItem.versionCode
                    )
                )
            }
            is AppVersionsCenter -> {
                val itemLabel = context.getString(R.string.view_other_app_versions)
                val urlLabel = context.getString(R.string.my_github)
                (holder as UrlViewHolder).setUrl(
                    itemLabel,
                    urlLabel,
                    aboutItem.url,
                    aboutCallback::openLink
                )
            }
            is SupportEmail -> {
                val itemLabel = context.getString(R.string.email)
                (holder as UrlViewHolder).setUrl(
                    itemLabel,
                    aboutItem.address,
                    aboutItem.address,
                    aboutCallback::openEmailAddress
                )
            }
            is SupportTwitter -> {
                val itemLabel = context.getString(R.string.twitter)
                (holder as UrlViewHolder).setUrl(
                    itemLabel,
                    aboutItem.name,
                    aboutItem.url,
                    aboutCallback::openLink
                )
            }
            is NewVersionAvailable -> {
                (holder as LabelViewHolder).setText(context.getString(R.string.newer_versions))
            }
            is AvailableVersion -> {
                (holder as AppVersionViewHolder)
                    .setVersionData(
                        aboutItem,
                        aboutItem.versionNumber == beingInstalledVersion.versionNumber
                    )
            }
        }
    }

    override fun getItemCount(): Int = aboutList.size

    override fun getItemViewType(position: Int): Int {
        return when (aboutList[position]) {
            is CurrentAppVersion -> CURRENT_APP_VERSION
            is TagDataVersion -> TAG_DATA_VERSION
            is AppVersionsCenter -> APP_VERSION_CENTER
            is SupportEmail -> SUPPORT_EMAIL
            is SupportTwitter -> SUPPORT_TWITTER
            is NewVersionAvailable -> NEW_VERSION_AVAILABLE
            is AvailableVersion -> AVAILABLE_VERSION
        }
    }


    interface AboutCallback {
        fun openLink(url: String)
        fun openEmailAddress(email: String)
        fun installVersion(versionCode: String, versionNumber: Int)
        fun showAppBeingUpgraded()
    }

    private class LabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mtvLabel: MyTextView = itemView.findViewById(R.id.mtvLabel)

        fun setText(text: String) {
            mtvLabel.text = text
        }
    }

    private class UrlViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mtvLabel: MyTextView = itemView.findViewById(R.id.mtvLabel)
        private val mtvUrl: MyTextView = itemView.findViewById(R.id.mtvUrl)

        fun setUrl(
            itemLabel: String,
            urlLabel: String,
            url: String,
            onUrlOpened: (url: String) -> Unit
        ) {
            mtvLabel.text = itemLabel
            mtvUrl.addClickAbleText(url, urlLabel, onUrlOpened)
        }
    }

    private inner class AppVersionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mtvVersionName: MyTextView = itemView.findViewById(R.id.mtvVersionName)
        private val buttonInstall: MyTextView = itemView.findViewById(R.id.buttonInstall)
        private val pbInstallationProgress: ProgressBar =
            itemView.findViewById(R.id.pbInstallationProgress)
        private val mtvWhatsNewContent: MyTextView = itemView.findViewById(R.id.mtvWhatsNewContent)

        fun setVersionData(
            version: AvailableVersion,
            isBeingInstalled: Boolean
        ) {
            val context = itemView.context
            mtvVersionName.text =
                context.getString(R.string.app_version_template, version.versionCode)
            mtvWhatsNewContent.text = version.whatsNew
            buttonInstall.becomeVisibleIf(!isBeingInstalled)
            pbInstallationProgress.becomeVisibleIf(isBeingInstalled)

            buttonInstall.setOnClickListener {
                when (beingInstalledVersion.versionNumber) {
                    -1 -> {
                        aboutCallback.installVersion(version.versionCode, version.versionNumber)
                    }
                    else -> {
                        aboutCallback.showAppBeingUpgraded()
                    }
                }
            }
        }
    }

    fun showInstallationProgress(versionNumber: Int) {
        beingInstalledVersion.versionNumber = versionNumber
        aboutList.indexOfFirst {
            it is AvailableVersion && it.versionNumber == versionNumber
        }.takeIf { it >= 0 }?.let(this::notifyItemChanged)
    }

    fun hideInstallationProgress(versionNumber: Int) {
        beingInstalledVersion.versionNumber = -1
        aboutList.indexOfFirst {
            it is AvailableVersion && it.versionNumber == versionNumber
        }.takeIf { it >= 0 }?.let(this::notifyItemChanged)
    }

    companion object {
        private const val CURRENT_APP_VERSION = 1
        private const val TAG_DATA_VERSION = 2
        private const val APP_VERSION_CENTER = 3
        private const val SUPPORT_EMAIL = 4
        private const val SUPPORT_TWITTER = 5
        private const val NEW_VERSION_AVAILABLE = 6
        private const val AVAILABLE_VERSION = 7

        private data class BeingInstalledVersion(var versionNumber: Int = -1)
    }
}