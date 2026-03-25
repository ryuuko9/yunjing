package com.example.yunjing.ui.merchant.network

import com.example.yunjing.ui.merchant.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface MerchantContentApi {

    @POST("api/merchant/projects")
    suspend fun createProject(
        @Body request: CreateProjectRequest
    ): ApiResponse<MerchantProjectDto>

    @GET("api/merchant/projects")
    suspend fun listProjects(
        @Query("userId") userId: Long
    ): ApiResponse<List<MerchantProjectDto>>

    @GET("api/merchant/projects/{projectId}")
    suspend fun getProjectDetail(
        @Path("projectId") projectId: Long
    ): ApiResponse<MerchantProjectDetailDto>

    @Multipart
    @POST("api/merchant/projects/{projectId}/media/upload")
    suspend fun uploadMedia(
        @Path("projectId") projectId: Long,
        @Part("assetType") assetType: RequestBody,
        @Part file: MultipartBody.Part
    ): ApiResponse<UploadMediaResponse>

    @POST("api/merchant/projects/{projectId}/rebuild")
    suspend fun rebuildProject(
        @Path("projectId") projectId: Long,
        @Body request: RebuildRequest
    ): ApiResponse<List<ProjectModelAssetDto>>

    @GET("api/merchant/projects/{projectId}/models")
    suspend fun listModels(
        @Path("projectId") projectId: Long
    ): ApiResponse<List<ProjectModelAssetDto>>
}