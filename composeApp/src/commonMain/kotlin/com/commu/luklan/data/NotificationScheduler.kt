package com.commu.luklan.data

interface NotificationScheduler {
    fun schedule(medicine: Medicine)
    fun cancel(medicine: Medicine)
    fun cancelSlot(medicine: Medicine, index: Int)
}

expect fun getNotificationScheduler(): NotificationScheduler
