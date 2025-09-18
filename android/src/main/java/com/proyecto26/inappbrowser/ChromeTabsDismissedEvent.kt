package com.proyecto26.inappbrowser

data class ChromeTabsDismissedEvent(
    val message: String,
    val resultType: String,
    val isError: Boolean
) 