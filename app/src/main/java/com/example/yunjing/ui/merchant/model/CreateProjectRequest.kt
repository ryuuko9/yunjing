package com.example.yunjing.ui.merchant.model

data class CreateProjectRequest(
    val userId: Long,
    val projectName: String,
    val projectDesc: String? = null
)