package com.remenod.oop2_coursework.domain.model

data class ProgressSnapshot(
    val percent: Double, // 0.0 to 1.0
    val explanation: String
) {
    val percentageString: String get() = "${(percent * 100).toInt()}%"
}
