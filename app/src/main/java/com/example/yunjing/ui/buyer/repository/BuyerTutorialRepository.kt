package com.example.yunjing.ui.buyer.repository

import com.example.yunjing.ui.buyer.model.BuyerTutorialDto
import com.example.yunjing.ui.buyer.network.BuyerApiService
import com.example.yunjing.ui.merchant.model.ApiResponse

class BuyerTutorialRepository(
    private val api: BuyerApiService
) {

    suspend fun listTutorials(buyerUserId: Long): ApiResponse<List<BuyerTutorialDto>> {
        return api.listTutorials(buyerUserId)
    }

    suspend fun importTutorial(
        publishCode: String,
        buyerUserId: Long
    ): ApiResponse<BuyerTutorialDto> {
        return api.importTutorial(publishCode, buyerUserId)
    }

    suspend fun deleteTutorial(
        tutorialId: Long,
        buyerUserId: Long
    ): ApiResponse<Unit> {
        return api.deleteTutorial(tutorialId, buyerUserId)
    }
}