package fr.smarquis.fcm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.smarquis.fcm.data.model.ConnectionStats
import fr.smarquis.fcm.data.model.DailyStats
import fr.smarquis.fcm.data.repository.ConnectionHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionAnalysisViewModel(
    private val repository: ConnectionHistoryRepository
) : ViewModel() {

    private val _connectionStats = MutableStateFlow<ConnectionStats?>(null)
    val connectionStats: StateFlow<ConnectionStats?> = _connectionStats.asStateFlow()

    private val _dailyStats = MutableStateFlow<List<DailyStats>>(emptyList())
    val dailyStats: StateFlow<List<DailyStats>> = _dailyStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // 加载连接统计
                val stats = repository.getConnectionStats()
                _connectionStats.value = stats

                // 加载每日统计
                val daily = repository.getDailyStats(7)
                _dailyStats.value = daily

            } catch (e: Exception) {
                _error.value = e.message ?: "加载数据时发生错误"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadData()
    }

    fun clearError() {
        _error.value = null
    }

    fun cleanupOldRecords(daysToKeep: Int = 30) {
        viewModelScope.launch {
            try {
                repository.cleanupOldRecords(daysToKeep)
                loadData() // 重新加载数据
            } catch (e: Exception) {
                _error.value = "清理旧记录时发生错误: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                repository.deleteAll()
                loadData() // 重新加载数据
            } catch (e: Exception) {
                _error.value = "清除历史记录时发生错误: ${e.message}"
                e.printStackTrace()
            }
        }
    }
}
