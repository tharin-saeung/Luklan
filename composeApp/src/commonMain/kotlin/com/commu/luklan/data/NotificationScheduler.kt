package com.commu.luklan.data

interface NotificationScheduler {
    fun schedule(medicine: Medicine)
    fun cancel(medicine: Medicine)
}

expect fun getNotificationScheduler(): NotificationScheduler
