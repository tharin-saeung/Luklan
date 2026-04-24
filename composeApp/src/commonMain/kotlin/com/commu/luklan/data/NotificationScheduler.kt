package com.commu.luklan.data

interface NotificationScheduler {
    fun schedule(medicine: Medicine)
    fun cancel(medicine: Medicine)
    fun cancelSlot(medicine: Medicine, index: Int)
    fun cancelAll()
    fun showImmediateNotification(title: String, body: String)
}

expect fun getNotificationScheduler(): NotificationScheduler
