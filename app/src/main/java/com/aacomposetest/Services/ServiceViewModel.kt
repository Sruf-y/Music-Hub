package com.aacomposetest.Services

import com.aacomposetest.Classes.AutoStateFlow

class ServiceViewModel {
    var notificationState = AutoStateFlow<NotificationState>(NotificationState.Dead)

    var taskState = AutoStateFlow<Boolean>(false)

    fun startServiceFromActivity(){

    }


    companion object{
        enum class NotificationState {
            Foreground,
            Background,
            Dead
        }
    }
}