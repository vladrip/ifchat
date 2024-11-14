package com.vladrip.ifchat.data.network.model

data class ErrorResponse(
    val status: Int,
    val error: String,
)

data class PagedResponse<E>(
    val content: List<E>,
    val last: Boolean,
)