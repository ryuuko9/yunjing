package com.example.yunjing.ui.merchant.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.yunjing.ui.merchant.model.*
import com.example.yunjing.ui.merchant.network.MerchantContentApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class MerchantContentRepository(
    private val api: MerchantContentApi
) {

    suspend fun createProject(
        userId: Long,
        projectName: String,
        projectDesc: String?
    ): Result<MerchantProjectDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.createProject(
                CreateProjectRequest(
                    userId = userId,
                    projectName = projectName,
                    projectDesc = projectDesc
                )
            )
            if (!response.success || response.data == null) {
                throw IllegalStateException(response.message.ifBlank { "创建项目失败" })
            }
            response.data
        }
    }

    suspend fun listProjects(userId: Long): Result<List<MerchantProjectDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.listProjects(userId)
            if (!response.success || response.data == null) {
                throw IllegalStateException(response.message.ifBlank { "获取项目列表失败" })
            }
            response.data
        }
    }

    suspend fun getProjectDetail(projectId: Long): Result<MerchantProjectDetailDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getProjectDetail(projectId)
            if (!response.success || response.data == null) {
                throw IllegalStateException(response.message.ifBlank { "获取项目详情失败" })
            }
            response.data
        }
    }

    suspend fun uploadMedia(
        context: Context,
        projectId: Long,
        assetType: String,
        uri: Uri
    ): Result<ProjectMediaAssetDto> = withContext(Dispatchers.IO) {
        runCatching {
            val file = uriToTempFile(context, uri)
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val assetTypeBody = assetType.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadMedia(
                projectId = projectId,
                assetType = assetTypeBody,
                file = part
            )

            if (!response.success || response.data?.asset == null) {
                throw IllegalStateException(response.message.ifBlank { "上传素材失败" })
            }

            response.data.asset
        }
    }

    suspend fun rebuildProject(
        projectId: Long,
        sourceAssetIds: List<Long>? = null
    ): Result<List<ProjectModelAssetDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.rebuildProject(
                projectId = projectId,
                request = RebuildRequest(sourceAssetIds)
            )
            if (!response.success || response.data == null) {
                throw IllegalStateException(response.message.ifBlank { "发起重建失败" })
            }
            response.data
        }
    }

    suspend fun listModels(projectId: Long): Result<List<ProjectModelAssetDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.listModels(projectId)
            if (!response.success || response.data == null) {
                throw IllegalStateException(response.message.ifBlank { "获取模型列表失败" })
            }
            response.data
        }
    }

    private fun uriToTempFile(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        val fileName = queryFileName(contentResolver, uri) ?: "upload_file"
        val suffix = fileName.substringAfterLast('.', "")
            .takeIf { it.isNotBlank() }
            ?.let { ".$it" }
            ?: ""

        val tempFile = File.createTempFile("merchant_upload_", suffix, context.cacheDir)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("无法读取所选文件")
        return tempFile
    }

    private fun queryFileName(contentResolver: ContentResolver, uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
    }
}