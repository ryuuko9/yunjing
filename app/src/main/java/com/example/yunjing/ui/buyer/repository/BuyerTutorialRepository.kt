package com.example.yunjing.ui.buyer.repository

import com.example.yunjing.ui.buyer.model.BuyerTutorialDto
import com.example.yunjing.ui.buyer.network.BuyerApiService
import com.example.yunjing.ui.merchant.model.ApiResponse
import com.example.yunjing.ui.merchant.model.MerchantProjectDetailDto

class BuyerTutorialRepository(
    private val api: BuyerApiService
) {
    suspend fun getPublishedProject(publishCode: String): ApiResponse<MerchantProjectDetailDto> =
        api.getPublishedProject(publishCode)

    suspend fun listTutorials(buyerUserId: Long): ApiResponse<List<BuyerTutorialDto>> =
        api.listTutorials(buyerUserId)

    suspend fun importTutorial(
        publishCode: String,
        buyerUserId: Long
    ): ApiResponse<BuyerTutorialDto> = api.importTutorial(publishCode, buyerUserId)

    suspend fun deleteTutorial(
        tutorialId: Long,
        buyerUserId: Long
    ): ApiResponse<Unit> = api.deleteTutorial(tutorialId, buyerUserId)
}
