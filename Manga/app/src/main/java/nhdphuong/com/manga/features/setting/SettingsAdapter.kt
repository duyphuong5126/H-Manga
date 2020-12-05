package nhdphuong.com.manga.features.setting

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.alternativedomain.AlternativeDomain
import nhdphuong.com.manga.features.setting.uimodel.SettingUiModel
import nhdphuong.com.manga.features.setting.uimodel.SettingUiModel.AlternativeDomainsUiModel

class SettingsAdapter(
    private val settingList: List<SettingUiModel>,
    private val settingCallback: SettingCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return AlternativeDomainViewHolder(
            layoutInflater.inflate(
                R.layout.item_alternative_domains,
                parent,
                false
            ),
            settingCallback
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val settingItem = settingList[position]) {
            is AlternativeDomainsUiModel -> {
                (holder as AlternativeDomainViewHolder).bindTo(settingItem)
            }
        }
    }

    override fun getItemCount(): Int = settingList.size

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

    interface SettingCallback {
        fun onDomainSelected(alternativeDomain: AlternativeDomain)
        fun onClearAlternativeDomain()
    }
}