package fr.smarquis.fcm.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import fr.smarquis.fcm.R
import fr.smarquis.fcm.data.model.ConnectionStats
import fr.smarquis.fcm.data.repository.ConnectionHistoryRepository
import fr.smarquis.fcm.databinding.FragmentConnectionOverviewBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ConnectionOverviewFragment : Fragment() {

    private var _binding: FragmentConnectionOverviewBinding? = null
    private val binding get() = _binding!!
    
    private val repository: ConnectionHistoryRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadConnectionStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadConnectionStats() {
        lifecycleScope.launch {
            try {
                val stats = repository.getConnectionStats()
                updateUI(stats)
            } catch (e: Exception) {
                // 处理错误
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(stats: ConnectionStats) {
        binding.apply {
            // 总在线时间
            textTotalOnlineTime.text = formatDuration(stats.totalOnlineTime)
            
            // 连接次数
            textConnectionCount.text = stats.connectionCount.toString()
            
            // 平均连接时长
            textAverageConnectionDuration.text = formatDuration(stats.averageConnectionDuration)
            
            // 最长连接时间
            textLongestConnection.text = formatDuration(stats.longestConnection)
            
            // 今日在线时间
            textTodayOnlineTime.text = formatDuration(stats.onlineTimeToday)
            
            // 今日连接次数
            textTodayConnections.text = stats.connectionsToday.toString()
            
            // 最后连接时间
            textLastConnectionTime.text = stats.lastConnectionTime?.let { timestamp ->
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
            } ?: getString(R.string.connection_history_empty)
        }
    }

    private fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0秒"
        
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        
        return when {
            hours > 0 -> {
                if (minutes > 0) {
                    getString(R.string.time_format_hours_minutes, hours, minutes)
                } else {
                    getString(R.string.time_format_hours, hours)
                }
            }
            minutes > 0 -> {
                if (seconds > 0) {
                    getString(R.string.time_format_minutes_seconds, minutes, seconds)
                } else {
                    getString(R.string.time_format_minutes, minutes)
                }
            }
            else -> getString(R.string.time_format_seconds, seconds)
        }
    }
}
