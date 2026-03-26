package com.example.yunjing.ui.merchant.network

import com.example.yunjing.ui.merchant.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface MerchantContentApi {

    @POST("api/merchant/projects")
    suspend fun createProject(
        @Body request: CreateProjectRequest
    ): Response<ApiResponse<MerchantProjectDto>>

    @GET("api/merchant/projects")
    suspend fun listProjects(
        @Query("userId") userId: Long
    ): Response<ApiResponse<List<MerchantProjectDto>>>

    @GET("api/merchant/projects/{projectId}")
    suspend fun getProjectDetail(
        @Path("projectId") projectId: Long
    ): Response<ApiResponse<MerchantProjectDetailDto>>

    @PUT("api/merchant/projects/{projectId}/rename")
    suspend fun renameProject(
        @Path("projectId") projectId: Long,
        @Body request: RenameProjectRequest
    ): retrofit2.Response<ApiResponse<Unit>>

    @DELETE("api/merchant/projects/{projectId}")
    suspend fun deleteProject(
        @Path("projectId") projectId: Long
    ): Response<ApiResponse<Unit>>

    @DELETE("api/merchant/projects/{projectId}/media/{mediaId}")
    suspend fun deleteMedia(
        @Path("projectId") projectId: Long,
        @Path("mediaId") mediaId: Long
    ): ApiResponse<Unit>

    @Multipart
    @POST("api/merchant/projects/{projectId}/media/upload")
    suspend fun uploadMedia(
        @Path("projectId") projectId: Long,
        @Part("assetType") assetType: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<UploadMediaResponse>>

    @POST("api/merchant/projects/{projectId}/rebuild")
    suspend fun rebuildProject(
        @Path("projectId") projectId: Long,
        @Body request: RebuildRequest
    ): Response<ApiResponse<List<ProjectModelAssetDto>>>

    @GET("api/merchant/projects/{projectId}/models")
    suspend fun listModels(
        @Path("projectId") projectId: Long
    ): Response<ApiResponse<List<ProjectModelAssetDto>>>
}