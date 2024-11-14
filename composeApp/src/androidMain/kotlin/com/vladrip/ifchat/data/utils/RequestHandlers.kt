package com.vladrip.ifchat.data.utils

import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.executeWithRetry
import com.vladrip.ifchat.data.Data
import com.vladrip.ifchat.data.network.model.ErrorResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

fun <S : Any> handleNetworkRequest(
    fireRequest: suspend () -> NetworkResponse<S, ErrorResponse>,
) = flow {
    val response = fireRequest().let {
        if (it is NetworkResponse.NetworkError) {
            emit(Data.NetworkError)
            executeWithRetry(times = Int.MAX_VALUE, initialDelay = 500) {
                fireRequest()
            }
        } else it
    }

    if (response is NetworkResponse.Success) emit(Data.Success(response.body))
    else if (response is NetworkResponse.Error) emit(Data.Error(response.body))
}

fun <T> handleLocalRequest(fireRequest: () -> Flow<T>): Flow<Data<T>> =
    fireRequest().map {
        if (it != null) Data.Success(it) else Data.Loading
    }