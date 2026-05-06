package com.project.luckysevens.data.online

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VictoryDto(
    val username: String = "",
    val winnings: Int = 0,
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
