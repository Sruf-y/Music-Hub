package com.aacomposetest.Classes

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class CodeClock() {

    private lateinit var time0: LocalDateTime

    fun startMonitoring(){
        time0 = LocalDateTime.now()

    }

    fun getMillisSinceStart(): Long{

        return Duration.between(time0, LocalDateTime.now()).toMillis()
    }

}