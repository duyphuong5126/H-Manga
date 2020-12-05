package nhdphuong.com.manga.data.entity.alternativedomain

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants.Companion.ALTERNATIVE_DOMAINS
import nhdphuong.com.manga.Constants.Companion.ALTERNATIVE_DOMAINS_DATA_VERSION

data class AlternativeDomainGroup(
    @field:SerializedName(ALTERNATIVE_DOMAINS_DATA_VERSION) val groupVersion: Int,
    @field:SerializedName(ALTERNATIVE_DOMAINS) val groups: List<AlternativeDomain>
)