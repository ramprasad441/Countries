package com.ramprasad.countries.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Ramprasad on 7/6/25.
 */
data class Countries(
    @SerializedName("capital") var capital: String = "",
    @SerializedName("code") var code: String = "",
    @SerializedName("currency") var currency: Currency = Currency(),
    @SerializedName("flag") var flag: String = "",
    @SerializedName("language") var language: Language = Language(),
    @SerializedName("name") var name: String = "",
    @SerializedName("region") var region: String = "",
    var header: String? = null
)