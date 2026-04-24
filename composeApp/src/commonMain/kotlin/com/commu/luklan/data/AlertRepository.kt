package com.commu.luklan.data

interface AlertRepository {
    suspend fun sendAlert(alert: Alert): Result<Unit>
    suspend fun getAlertsForUser(userId: String): Result<List<Alert>>
    suspend fun deleteAlert(alertId: String): Result<Unit>
    suspend fun deleteAllAlerts(userId: String, groupIds: List<String>): Result<Unit>
}

expect fun getAlertRepository(): AlertRepository
