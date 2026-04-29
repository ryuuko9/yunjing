package com.example.yunjing.ui.unity

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.yunjing.network.AppServerConfig
import com.demo.picker.PickerUnityActivity

const val EXTRA_PUBLISH_CODE = "publishCode"
const val EXTRA_TUTORIAL_VIDEO_URL = "tutorialVideoUrl"
const val EXTRA_TUTORIAL_TITLE = "tutorialTitle"

fun normalizeBackendMediaUrl(rawUrl: String?): String? {
    return AppServerConfig.normalizeBackendUrl(rawUrl)
}

fun Context.createUnityPlayerIntent(
    publishCode: String? = null,
    tutorialVideoUrl: String? = null,
    tutorialTitle: String? = null
): Intent {
    return Intent(this, PickerUnityActivity::class.java).apply {
        publishCode?.trim()?.takeIf { it.isNotEmpty() }?.let {
            putExtra(EXTRA_PUBLISH_CODE, it)
        }
        normalizeBackendMediaUrl(tutorialVideoUrl)?.let {
            putExtra(EXTRA_TUTORIAL_VIDEO_URL, it)
        }
        tutorialTitle?.trim()?.takeIf { it.isNotEmpty() }?.let {
            putExtra(EXTRA_TUTORIAL_TITLE, it)
        }
        if (this@createUnityPlayerIntent !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}

fun Context.canOpenUnityPlayer(intent: Intent): Boolean {
    return intent.resolveActivity(packageManager) != null
}
