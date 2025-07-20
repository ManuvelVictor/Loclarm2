package com.victor.loclarm2.data.model

data class Alarm(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Float = 100f,
    val userId: String = "",
    val isActive: Boolean = false
)