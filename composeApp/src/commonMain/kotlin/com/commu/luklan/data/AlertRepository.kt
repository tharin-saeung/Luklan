package com.commu.luklan.data

interface AlertRepository {
    suspend fun sendAlert(alert: Alert): Result<Unit>
    suspend fun getAlertsForUser(userId: String): Result<List<Alert>>
}

expect fun getAlertRepository(): AlertRepository
