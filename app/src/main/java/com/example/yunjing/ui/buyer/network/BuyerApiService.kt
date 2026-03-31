package com.example.yunjing.ui.buyer.network

import com.example.yunjing.ui.buyer.model.BuyerTutorialDto
import com.example.yunjing.ui.merchant.model.ApiResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BuyerApiService {
    @POST("api/buyer/tutorials/import/{publishCode}")
    suspend fun importTutorial(
        @Path("publishCode") publishCode: String,
        @Query("buyerUserId") buyerUserId: Long
    ): ApiResponse<BuyerTutorialDto>

    @GET("api/buyer/tutorials")
    suspend fun listTutorials(
        @Query("buyerUserId") buyerUserId: Long
    ): ApiResponse<List<BuyerTutorialDto>>

    @GET("api/buyer/tutorials/{tutorialId}")
    suspend fun getTutorialDetail(
        @Path("tutorialId") tutorialId: Long,
        @Query("buyerUserId") buyerUserId: Long
    ): ApiResponse<BuyerTutorialDto>

    @DELETE("api/buyer/tutorials/{tutorialId}")
    suspend fun deleteTutorial(
        @Path("tutorialId") tutorialId: Long,
        @Query("buyerUserId") buyerUserId: Long
    ): ApiResponse<Unit>
}