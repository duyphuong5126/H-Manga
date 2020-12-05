package nhdphuong.com.manga.features.setting

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.alternativedomain.AlternativeDomain
import nhdphuong.com.manga.features.setting.uimodel.SettingUiModel
import nhdphuong.com.manga.features.setting.uimodel.SettingUiModel.AlternativeDomainsUiModel
import nhdphuong.com.manga.features.setting.uimodel.SettingUiModel.AllowAppUpgradeStatus
import nhdphuong.com.manga.views.customs.MyTextView

class SettingsAdapter(
    private val settingList: List<SettingUiModel>,
    private val settingCallback: SettingCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ALLOW_UPGRADE_STATUS -> {
                SwitchViewHolder(layoutInflater.inflate(R.layout.item_about_switch, parent, false))
            }

            else -> {
                AlternativeDomainViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_alternative_domains,
                        parent,
                        false
                    ),
                    settingCallback
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val settingItem = settingList[position]) {
            is AlternativeDomainsUiModel -> {
                (holder as AlternativeDomainViewHolder).bindTo(settingItem)
            }

            is AllowAppUpgradeStatus -> {
                val itemLabel =
                    holder.itemView.context.getString(R.string.app_upgrade_notification_receive)
                (holder as SwitchViewHolder).setUrl(
                    itemLabel,
                    settingItem.isEnabled,
                    settingCallback::changeAppUpgradeNotificationStatus
                )
            }
        }
    }

    override fun getItemCount(): Int = settingList.size

    override fun getItemViewType(position: Int): Int {
        return when (settingList[position]) {
            is AllowAppUpgradeStatus -> ALLOW_UPGRADE_STATUS
            is AlternativeDomainsUiModel -> ALTERNATIVE_DOMAINS
        }
    }

    private class AlternativeDomainViewHolder(
        view: View,
        private val settingCallback: SettingCallback
    ) : RecyclerView.ViewHolder(view) {
        private val rgAvailableDomains: RadioGroup = view.findViewById(R.id.rgAvailableDomains)

        @SuppressLint("InflateParams")
        fun bindTo(alternativeDomains: AlternativeDomainsUiModel) {
            val layoutInflater = LayoutInflater.from(itemView.context)
            alternativeDomains.alternativeDomainGroup.groups.forEach { domain ->
                val domainItem: RadioButton = layoutInflater.inflate(
                    R.layout.item_alternative_domain_option,
                    null,
                    false
                ) as RadioButton
                domainItem.text = domain.domainId
                domainItem.setOnClickListener {
                    if (domainItem.isChecked) {
                        settingCallback.onDomainSelected(domain)
                    }
                }
                rgAvailableDomains.addView(domainItem)
                if (alternativeDomains.activeDomainId == domain.domainId) {
                    rgAvailableDomains.check(domainItem.id)
                }
            }
            val resetItem: RadioButton = layoutInflater.inflate(
                R.layout.item_alternative_domain_option,
                null,
                false
            ) as RadioButton
            resetItem.setOnClickListener {
                if (resetItem.isChecked) {
                    settingCallback.onClearAlternativeDomain()
                }
            }
            resetItem.setText(R.string.dont_use)
            rgAvailableDomains.addView(resetItem)
            if (alternativeDomains.activeDomainId.isNullOrBlank()) {
                rgAvailableDomains.check(resetItem.id)
            }
        }
    }

    private class SwitchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mtvLabel: MyTextView = itemView.findViewById(R.id.mtvLabel)
        private val scEnabled: SwitchCompat = itemView.findViewById(R.id.scEnabled)

        fun setUrl(label: String, isEnabled: Boolean, onStatusChanged: (enabled: Boolean) -> Unit) {
            mtvLabel.text = label
            scEnabled.isChecked = isEnabled
            scEnabled.setOnCheckedChangeListener { _, isChecked ->
                onStatusChanged(isChecked)
            }
        }
    }

    interface SettingCallback {
        fun onDomainSelected(alternativeDomain: AlternativeDomain)
        fun onClearAlternativeDomain()
        fun changeAppUpgradeNotificationStatus(enabled: Boolean)
    }

    companion object {
        private const val ALLOW_UPGRADE_STATUS = 1
        private const val ALTERNATIVE_DOMAINS = 2
    }
}