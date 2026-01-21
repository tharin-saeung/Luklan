package com.commu.luklan.data

import com.commu.luklan.LuklanApplication

actual fun getNotificationScheduler(): NotificationScheduler {
    return AndroidNotificationScheduler(LuklanApplication.getAppContext())
}
