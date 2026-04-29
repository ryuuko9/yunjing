package com.example.yunjing.network

import android.net.Uri

object AppServerConfig {

    private const val BACKEND_SCHEME = "http"
    private const val BACKEND_HOST = "yunjingzhilian.asia"
    private const val BACKEND_PORT = 80

    private const val ASSEMBLE_SCHEME = "http"
    private const val ASSEMBLE_HOST = "assemble.yunjingzhilian.asia"
    private const val ASSEMBLE_PORT = 80

    private val localBackendHosts = setOf(
        "localhost",
        "127.0.0.1",
        "10.0.2.2",
        "172.20.10.3",
        "192.168.1.50",
        "172.24.116.14",
        "47.237.211.12"
    )

    val backendBaseUrl: String = buildBaseUrl(
        scheme = BACKEND_SCHEME,
        host = BACKEND_HOST,
        port = BACKEND_PORT
    )

    val assembleBaseUrl: String = buildBaseUrl(
        scheme = ASSEMBLE_SCHEME,
        host = ASSEMBLE_HOST,
        port = ASSEMBLE_PORT
    )

    fun normalizeBackendUrl(rawUrl: String?): String? {
        val value = rawUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null

        if (value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true)) {
            val uri = Uri.parse(value)
            val host = uri.host?.trim().orEmpty()
            if (host in localBackendHosts) {
                return uri.buildUpon()
                    .scheme(BACKEND_SCHEME)
                    .encodedAuthority(buildAuthority(BACKEND_HOST, BACKEND_PORT))
                    .build()
                    .toString()
            }
            return value
        }

        val normalizedPath = if (value.startsWith("/")) value else "/$value"
        return backendBaseUrl.removeSuffix("/") + normalizedPath
    }

    private fun buildBaseUrl(scheme: String, host: String, port: Int): String {
        val authority = buildAuthority(host, port)
        return "$scheme://$authority/"
    }

    private fun buildAuthority(host: String, port: Int): String {
        return if (isDefaultPort(port)) host else "$host:$port"
    }

    private fun isDefaultPort(port: Int): Boolean {
        return port == 80 || port == 443
    }
}
