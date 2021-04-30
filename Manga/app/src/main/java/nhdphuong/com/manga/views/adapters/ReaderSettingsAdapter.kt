package nhdphuong.com.manga.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R
import nhdphuong.com.manga.databinding.ItemReadingModeRadioBinding
import nhdphuong.com.manga.databinding.ItemTapNavigationSwitchBinding
import nhdphuong.com.manga.views.uimodel.ReaderType
import nhdphuong.com.manga.views.uimodel.ReaderType.HorizontalPage
import nhdphuong.com.manga.views.uimodel.ReaderType.VerticalScroll
import nhdphuong.com.manga.views.uimodel.ReaderType.ReversedHorizontalPage

class ReaderSettingsAdapter(
    private var readerType: ReaderType,
    private val settingsChangeListener: SettingsChangeListener,
    private var tapNavigationEnabled: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val logger: Logger by lazy {
        Logger("ReaderSettingsAdapter")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            READING_DIRECTION_TYPE -> ReadingModeViewHolder(parent, this::onTypeChanged)
            TAP_NAVIGATION_TYPE ->
                TapNavigationSwitchViewHolder(parent, this::onTapNavigationSettingChanged)
            else -> throw RuntimeException("Invalid view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            READING_DIRECTION_TYPE -> (holder as ReadingModeViewHolder).bindTo(readerType)
            TAP_NAVIGATION_TYPE -> (holder as TapNavigationSwitchViewHolder)
                .bindTo(tapNavigationEnabled, readerType != VerticalScroll)
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun onTypeChanged(newType: ReaderType) {
        logger.d("New reader type: $newType")
        readerType = newType
        settingsChangeListener.onTypeChanged(newType)
        onTapNavigationSettingChanged(tapNavigationEnabled && newType != VerticalScroll)
        notifyItemChanged(TAP_NAVIGATION_TYPE)
    }

    private fun onTapNavigationSettingChanged(isEnabled: Boolean) {
        logger.d("Is tap navigation enabled: $isEnabled")
        tapNavigationEnabled = isEnabled
        settingsChangeListener.onTapNavigationSettingChanged(isEnabled, readerType)
    }

    private class ReadingModeViewHolder(
        parent: ViewGroup,
        private val onTypeChanged: (newType: ReaderType) -> Unit
    ) : RecyclerView.ViewHolder(
        ItemReadingModeRadioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    ) {
        private val viewBinding: ItemReadingModeRadioBinding =
            ItemReadingModeRadioBinding.bind(itemView)

        private val readerTypeGroup: RadioGroup get() = viewBinding.readerTypeGroup
        private val modeReversedHorizontal: RadioButton get() = viewBinding.modeReversedHorizontal
        private val modeVertical: RadioButton get() = viewBinding.modeVertical
        private val modeHorizontal: RadioButton get() = viewBinding.modeHorizontal

        fun bindTo(readerType: ReaderType) {
            when (readerType) {
                HorizontalPage -> modeHorizontal.isChecked = true
                VerticalScroll -> modeVertical.isChecked = true
                ReversedHorizontalPage -> modeReversedHorizontal.isChecked = true
            }
            readerTypeGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.modeHorizontal -> onTypeChanged(HorizontalPage)
                    R.id.modeVertical -> onTypeChanged(VerticalScroll)
                    R.id.modeReversedHorizontal -> onTypeChanged(ReversedHorizontalPage)
                }
            }
        }
    }

    private class TapNavigationSwitchViewHolder(
        parent: ViewGroup,
        private val onTapNavigationSettingChanged: (isEnabled: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(
        ItemTapNavigationSwitchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    ) {
        private val viewBinding = ItemTapNavigationSwitchBinding.bind(itemView)
        private val scEnabled: SwitchCompat = viewBinding.scEnabled

        fun bindTo(tapNavigationEnabled: Boolean, isTapNavigationChangeable: Boolean) {
            scEnabled.isChecked = tapNavigationEnabled && isTapNavigationChangeable
            scEnabled.isEnabled = isTapNavigationChangeable
            scEnabled.setOnCheckedChangeListener { _, isChecked ->
                onTapNavigationSettingChanged(isChecked)
            }
        }
    }

    interface SettingsChangeListener {
        fun onTypeChanged(newType: ReaderType)
        fun onTapNavigationSettingChanged(isEnabled: Boolean, currentReaderType: ReaderType)
    }

    companion object {
        private const val READING_DIRECTION_TYPE = 0
        private const val TAP_NAVIGATION_TYPE = 1
    }
}