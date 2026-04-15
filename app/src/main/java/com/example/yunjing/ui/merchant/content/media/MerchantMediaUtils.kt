package com.example.yunjing.ui.merchant.content.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

/**
 * 本文件负责创建内容库拍照、录像时使用的媒体 Uri。
 */

fun createImageUri(context: Context): Uri {
    /**
     * 这个函数负责为拍照上传创建图片输出 Uri。
     */
    val contentValues = ContentValues().apply {
        put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "merchant_image_${System.currentTimeMillis()}.jpg"
        )
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: throw IllegalStateException("无法创建图片 Uri")
}

fun createVideoUri(context: Context): Uri {
    /**
     * 这个函数负责为录像上传创建视频输出 Uri。
     */
    val contentValues = ContentValues().apply {
        put(
            MediaStore.Video.Media.DISPLAY_NAME,
            "merchant_video_${System.currentTimeMillis()}.mp4"
        )
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
    }

    return context.contentResolver.insert(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: throw IllegalStateException("无法创建视频 Uri")
}
