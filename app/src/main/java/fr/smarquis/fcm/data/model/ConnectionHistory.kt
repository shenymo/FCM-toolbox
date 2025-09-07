package fr.smarquis.fcm.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 连接历史记录实体
 */
@Entity(tableName = "connection_history")
data class ConnectionHistory(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "status")
    val status: ConnectionStatus,
    
    @ColumnInfo(name = "network_type")
    val networkType: NetworkType?,
    
    @ColumnInfo(name = "duration")
    val duration: Long? = null, // 连接持续时间（毫秒）
    
    @ColumnInfo(name = "device_info")
    val deviceInfo: String? = null // 设备信息
)

/**
 * 连接统计数据类
 */
data class ConnectionStats(
    val totalOnlineTime: Long, // 总在线时间（毫秒）
    val connectionCount: Int, // 连接次数
    val averageConnectionDuration: Long, // 平均连接持续时间（毫秒）
    val longestConnection: Long, // 最长连接时间（毫秒）
    val disconnectionCount: Int, // 断线次数
    val lastConnectionTime: Long?, // 最后连接时间
    val connectionsToday: Int, // 今日连接次数
    val onlineTimeToday: Long // 今日在线时间（毫秒）
)

/**
 * 连接时间段数据类
 */
data class ConnectionPeriod(
    val startTime: Long,
    val endTime: Long?,
    val duration: Long,
    val networkType: NetworkType?
)

/**
 * 日期统计数据类
 */
data class DailyStats(
    val date: String, // 格式: yyyy-MM-dd
    val onlineTime: Long, // 当日在线时间（毫秒）
    val connectionCount: Int, // 当日连接次数
    val averageDuration: Long // 当日平均连接时长（毫秒）
)
