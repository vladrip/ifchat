package com.vladrip.ifchat.ui.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

val Int.sdp: Dp
    @Composable get() {
        val id = when (this) {
            in 1..600 -> "_${this}sdp"
            in (-60..-1) -> "_minus${this}sdp"
            else -> return this.dp
        }

        val context = LocalContext.current

        @SuppressLint("DiscouragedApi")
        val resourceField = context.resources.getIdentifier(id, "dimen", context.packageName)
        return if (resourceField != 0) dimensionResource(resourceField) else this.dp
    }

val Int.ssp: TextUnit
    @Composable get() = with(LocalDensity.current) { sdp.toSp() }

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}