package com.ramprasad.countries.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Ramprasad on 7/6/25.
 */
data class Currency(
    @SerializedName("code") var code: String = "",
    @SerializedName("name") var name: String = "",
    @SerializedName("symbol") var symbol: String = "",
)
