package fr.smarquis.fcm.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.smarquis.fcm.data.model.ConnectionHistory
import fr.smarquis.fcm.data.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionHistoryDao {

    @Query("SELECT * FROM connection_history ORDER BY timestamp DESC")
    fun getAllHistory(): LiveData<List<ConnectionHistory>>

    @Query("SELECT * FROM connection_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<ConnectionHistory>>

    @Query("SELECT * FROM connection_history WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getHistoryByDateRange(startTime: Long, endTime: Long): Flow<List<ConnectionHistory>>

    @Query("SELECT * FROM connection_history WHERE status = :status ORDER BY timestamp DESC")
    fun getHistoryByStatus(status: ConnectionStatus): Flow<List<ConnectionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ConnectionHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<ConnectionHistory>)

    @Query("DELETE FROM connection_history WHERE timestamp < :beforeTime")
    suspend fun deleteOldRecords(beforeTime: Long)

    @Query("DELETE FROM connection_history")
    suspend fun deleteAll()

    // 统计查询
    @Query("SELECT COUNT(*) FROM connection_history WHERE status = :status")
    suspend fun getCountByStatus(status: ConnectionStatus): Int

    @Query("SELECT SUM(duration) FROM connection_history WHERE status = :status AND duration IS NOT NULL")
    suspend fun getTotalDurationByStatus(status: ConnectionStatus): Long?

    @Query("SELECT AVG(duration) FROM connection_history WHERE status = :status AND duration IS NOT NULL")
    suspend fun getAverageDurationByStatus(status: ConnectionStatus): Long?

    @Query("SELECT MAX(duration) FROM connection_history WHERE status = :status AND duration IS NOT NULL")
    suspend fun getMaxDurationByStatus(status: ConnectionStatus): Long?

    // 今日统计
    @Query("""
        SELECT COUNT(*) FROM connection_history 
        WHERE status = :status 
        AND timestamp >= :startOfDay 
        AND timestamp <= :endOfDay
    """)
    suspend fun getTodayCountByStatus(status: ConnectionStatus, startOfDay: Long, endOfDay: Long): Int

    @Query("""
        SELECT SUM(duration) FROM connection_history 
        WHERE status = :status 
        AND duration IS NOT NULL 
        AND timestamp >= :startOfDay 
        AND timestamp <= :endOfDay
    """)
    suspend fun getTodayDurationByStatus(status: ConnectionStatus, startOfDay: Long, endOfDay: Long): Long?

    // 最后连接时间
    @Query("SELECT MAX(timestamp) FROM connection_history WHERE status = :status")
    suspend fun getLastConnectionTime(status: ConnectionStatus): Long?
}
