package com.vladrip.ifchat.data

import com.vladrip.ifchat.data.network.model.ErrorResponse
import com.vladrip.ifchat.ui.shared.Status

sealed class Data<out T> {
    data class Success<T>(val payload: T) : com.vladrip.ifchat.data.Data<T>()

    open class NotSuccess : com.vladrip.ifchat.data.Data<Nothing>()
    data object Loading : com.vladrip.ifchat.data.Data.NotSuccess()
    data object NetworkError : com.vladrip.ifchat.data.Data.NotSuccess()
    data class Error(val body: ErrorResponse?) : com.vladrip.ifchat.data.Data.NotSuccess()

    fun uiStatus(): Status {
        return when (this) {
            is com.vladrip.ifchat.data.Data.Success -> Status.SUCCESS
            is com.vladrip.ifchat.data.Data.Loading -> Status.LOADING
            is com.vladrip.ifchat.data.Data.NetworkError -> Status.NETWORK_ERROR
            else -> Status.ERROR
        }
    }
}