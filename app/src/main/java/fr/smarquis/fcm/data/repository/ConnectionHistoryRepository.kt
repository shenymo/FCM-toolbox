package fr.smarquis.fcm.data.repository

import fr.smarquis.fcm.data.db.ConnectionHistoryDao
import fr.smarquis.fcm.data.model.ConnectionHistory
import fr.smarquis.fcm.data.model.ConnectionStats
import fr.smarquis.fcm.data.model.ConnectionStatus
import fr.smarquis.fcm.data.model.DailyStats
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ConnectionHistoryRepository(private val dao: ConnectionHistoryDao) {

    fun getAllHistory() = dao.getAllHistory()

    fun getRecentHistory(limit: Int = 50) = dao.getRecentHistory(limit)

    fun getHistoryByDateRange(startTime: Long, endTime: Long) = dao.getHistoryByDateRange(startTime, endTime)

    fun getHistoryByStatus(status: ConnectionStatus) = dao.getHistoryByStatus(status)

    suspend fun insert(history: ConnectionHistory) = dao.insert(history)

    suspend fun insertAll(histories: List<ConnectionHistory>) = dao.insertAll(histories)

    suspend fun deleteOldRecords(beforeTime: Long) = dao.deleteOldRecords(beforeTime)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun getConnectionStats(): ConnectionStats {
        // 持续时间信息存储在DISCONNECTED记录中
        val totalOnlineTime = dao.getTotalDurationByStatus(ConnectionStatus.DISCONNECTED) ?: 0L
        val connectionCount = dao.getCountByStatus(ConnectionStatus.CONNECTED)
        val averageConnectionDuration = dao.getAverageDurationByStatus(ConnectionStatus.DISCONNECTED) ?: 0L
        val longestConnection = dao.getMaxDurationByStatus(ConnectionStatus.DISCONNECTED) ?: 0L
        val disconnectionCount = dao.getCountByStatus(ConnectionStatus.DISCONNECTED)
        val lastConnectionTime = dao.getLastConnectionTime(ConnectionStatus.CONNECTED)

        // 今日统计
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis - 1

        val connectionsToday = dao.getTodayCountByStatus(ConnectionStatus.CONNECTED, startOfDay, endOfDay)
        val onlineTimeToday = dao.getTodayDurationByStatus(ConnectionStatus.DISCONNECTED, startOfDay, endOfDay) ?: 0L

        return ConnectionStats(
            totalOnlineTime = totalOnlineTime,
            connectionCount = connectionCount,
            averageConnectionDuration = averageConnectionDuration,
            longestConnection = longestConnection,
            disconnectionCount = disconnectionCount,
            lastConnectionTime = lastConnectionTime,
            connectionsToday = connectionsToday,
            onlineTimeToday = onlineTimeToday
        )
    }

    suspend fun getDailyStats(days: Int = 7): List<DailyStats> {
        val result = mutableListOf<DailyStats>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        repeat(days) { dayOffset ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -dayOffset)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis - 1

            val dateString = dateFormat.format(Date(startOfDay))
            val connectionCount = dao.getTodayCountByStatus(ConnectionStatus.CONNECTED, startOfDay, endOfDay)
            val onlineTime = dao.getTodayDurationByStatus(ConnectionStatus.DISCONNECTED, startOfDay, endOfDay) ?: 0L
            val averageDuration = if (connectionCount > 0) onlineTime / connectionCount else 0L

            result.add(
                DailyStats(
                    date = dateString,
                    onlineTime = onlineTime,
                    connectionCount = connectionCount,
                    averageDuration = averageDuration
                )
            )
        }

        return result.reversed() // 返回按日期升序排列的结果
    }

    suspend fun cleanupOldRecords(daysToKeep: Int = 30) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -daysToKeep)
        val cutoffTime = calendar.timeInMillis
        dao.deleteOldRecords(cutoffTime)
    }
}
