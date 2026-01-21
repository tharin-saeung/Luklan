package com.commu.luklan.data

actual fun getNotificationScheduler(): NotificationScheduler {
    return IosNotificationScheduler()
}
