package com.aacomposetest.Classes

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object MyPermissionLauncher {

    @Composable
    fun rememberLauncher(onGrant:(() -> Unit)? = null,onDeny:(() -> Unit)? = null,onFinish:((Boolean) -> Unit)? = null): ManagedActivityResultLauncher<String, Boolean> {
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                onGrant?.invoke()
            } else {
                onDeny?.invoke()
            }
            onFinish?.invoke(granted)
        }
    }
}
