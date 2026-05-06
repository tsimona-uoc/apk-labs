package com.project.luckysevens.data.online

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayerScoreDto(
    @Json(name = "displayName") val username: String = "",
    @Json(name = "score") val score: Int = 0,
    @Json(name = "lastLogin") val lastUpdate: Long = 0
)
