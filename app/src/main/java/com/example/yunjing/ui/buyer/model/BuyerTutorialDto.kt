package com.example.yunjing.ui.buyer.model

data class BuyerTutorialDto(
    val id: Long,
    val buyerUserId: Long,
    val publishCode: String,
    val projectId: Long,
    val tutorialName: String,
    val projectDesc: String?,
    val coverUrl: String?,
    val explodedImageUrl: String?,
    val tutorialVideoUrl: String?,
    val tutorialTitle: String?,
    val publishUrl: String?,
    val addedAt: String?,
    val updatedAt: String?
)