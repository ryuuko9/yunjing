package com.example.yunjing.ui.merchant.content.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.yunjing.ui.merchant.content.model.ParseProjectRequest
import com.example.yunjing.ui.merchant.content.model.RenameProjectRequest
import com.example.yunjing.ui.merchant.model.ApiResponse
import com.example.yunjing.ui.merchant.model.CreateProjectRequest
import com.example.yunjing.ui.merchant.model.MerchantProjectDetailDto
import com.example.yunjing.ui.merchant.model.MerchantProjectDto
import com.example.yunjing.ui.merchant.model.ProjectMediaAssetDto
import com.example.yunjing.ui.merchant.model.ProjectModelAssetDto
import com.example.yunjing.ui.merchant.model.ProjectResponseDto
import com.example.yunjing.ui.merchant.model.RebuildRequest
import com.example.yunjing.ui.merchant.network.MerchantContentApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

/**
 * 本文件负责封装 merchant 内容库相关的数据访问与错误收口逻辑。
 */
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
            unwrapBody(response, "创建项目失败")
        }
    }

    suspend fun listProjects(userId: Long): Result<List<MerchantProjectDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.listProjects(userId)
            unwrapBody(response, "获取项目列表失败")
        }
    }

    suspend fun getProjectDetail(userId: Long, projectId: Long): Result<MerchantProjectDetailDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getProjectDetail(projectId, userId)
            unwrapBody(response, "获取项目详情失败")
        }
    }
    suspend fun renameProject(userId: Long, projectId: Long, newName: String): Result<Unit> {
        return try {
            val response = api.renameProject(
                projectId,
                userId,
                RenameProjectRequest(projectName = newName)
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && !body.success) {
                    Result.failure(RuntimeException(body.message.ifBlank { "重命名失败，请稍后再试" }))
                } else {
                    Result.success(Unit)
                }
            } else {
                val message = parseErrorMessage(
                    errorBody = response.errorBody()?.string(),
                    defaultMessage = "重命名失败，请稍后再试"
                )
                Result.failure(RuntimeException(message))
            }
        } catch (e: HttpException) {
            val message = parseErrorMessage(
                errorBody = e.response()?.errorBody()?.string(),
                defaultMessage = "重命名失败，请稍后再试"
            )
            Result.failure(RuntimeException(message))
        } catch (e: Exception) {
            Result.failure(RuntimeException(e.message ?: "重命名失败，请稍后再试"))
        }
    }

    suspend fun deleteProject(userId: Long, projectId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.deleteProject(projectId, userId)
            unwrapUnit(response, "删除项目失败")
        }
    }

    suspend fun uploadMedia(
        context: Context,
        userId: Long,
        projectId: Long,
        assetType: String,
        uri: Uri
    ): Result<ProjectMediaAssetDto> = withContext(Dispatchers.IO) {
        runCatching {
            val file = uriToTempFile(context, uri)
            try {
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val assetTypeBody = assetType.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = api.uploadMedia(
                    projectId = projectId,
                    userId = userId,
                    assetType = assetTypeBody,
                    file = part
                )

                val body = unwrapBody(response, "上传素材失败")
                body.asset
            } finally {
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }

    suspend fun rebuildProject(
        userId: Long,
        projectId: Long,
        sourceAssetIds: List<Long>? = null
    ): Result<List<ProjectModelAssetDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.rebuildProject(
                projectId = projectId,
                userId = userId,
                request = RebuildRequest(sourceAssetIds)
            )
            unwrapBody(response, "模型重建失败")
        }
    }

    suspend fun rebuild(userId: Long, projectId: Long): Result<Unit> {
        return runCatching {
            val resp = api.rebuild(projectId, userId)
            if (!resp.success) {
                throw IllegalStateException(resp.message.ifBlank { "模型重建失败" })
            }
        }
    }

    suspend fun listModels(userId: Long, projectId: Long): Result<List<ProjectModelAssetDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.listModels(projectId, userId)
            unwrapBody(response, "获取模型列表失败")
        }
    }

    private fun <T> unwrapBody(
        response: Response<ApiResponse<T>>,
        defaultMessage: String
    ): T {
        if (response.isSuccessful) {
            val body = response.body() ?: throw IllegalStateException(defaultMessage)
            if (!body.success) {
                throw IllegalStateException(body.message.ifBlank { defaultMessage })
            }
            return body.data ?: throw IllegalStateException(body.message.ifBlank { defaultMessage })
        } else {
            throw IllegalStateException(parseErrorMessage(response.errorBody()?.string(), defaultMessage))
        }
    }

    private fun unwrapUnit(
        response: Response<ApiResponse<Unit>>,
        defaultMessage: String
    ) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && !body.success) {
                throw IllegalStateException(body.message.ifBlank { defaultMessage })
            }
            return
        } else {
            throw IllegalStateException(parseErrorMessage(response.errorBody()?.string(), defaultMessage))
        }
    }

    suspend fun deleteMedia(
        userId: Long,
        projectId: Long,
        mediaId: Long
    ): Result<Unit> {
        return try {
            val response = api.deleteMedia(projectId, mediaId, userId)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    IllegalStateException(
                        response.message.ifBlank { "删除素材失败" }
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private fun parseErrorMessage(
        errorBody: String?,
        defaultMessage: String
    ): String {
        if (errorBody.isNullOrBlank()) return defaultMessage

        return try {
            val json = JSONObject(errorBody)

            val message = json.optString("message")
            val reason = json.optString("reason")
            val error = json.optString("error")

            when {
                message.isNotBlank() && message.lowercase() != "conflict" -> message
                reason.isNotBlank() && reason.lowercase() != "conflict" -> reason
                error.isNotBlank() && error.lowercase() != "conflict" -> error
                else -> defaultMessage
            }
        } catch (_: Exception) {
            defaultMessage
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

    suspend fun parseProject(
        userId: Long,
        projectId: Long,
        parseMode: String,
        sourceAssetIds: List<Long>,
        modelIds: List<Long>
    ): Result<MerchantProjectDetailDto> {
        return try {
            val response = api.parseProject(
                projectId = projectId,
                userId = userId,
                request = ParseProjectRequest(
                    parseMode = parseMode,
                    sourceAssetIds = sourceAssetIds,
                    modelIds = modelIds
                )
            )

            if (!response.success) {
                Result.failure(
                    IllegalStateException(response.message.ifBlank { "解析失败" })
                )
            } else {
                val detail = response.data
                if (detail == null) {
                    Result.failure(IllegalStateException("解析成功但未返回项目详情"))
                } else {
                    Result.success(detail)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishProject(userId: Long, projectId: Long): ApiResponse<ProjectResponseDto> {
        return api.publishProject(projectId, userId)
    }
}
