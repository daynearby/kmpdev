package com.example.kmpdev.core.network

actual fun isNetworkIOException(e: Exception): Boolean =
    e is java.net.UnknownHostException ||
            e is java.net.ConnectException ||
            e is java.net.SocketTimeoutException