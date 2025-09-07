package fr.smarquis.fcm.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import fr.smarquis.fcm.data.repository.ConnectionHistoryRepository
import fr.smarquis.fcm.databinding.FragmentConnectionChartsBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class ConnectionChartsFragment : Fragment() {

    private var _binding: FragmentConnectionChartsBinding? = null
    private val binding get() = _binding!!
    
    private val repository: ConnectionHistoryRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDailyStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDailyStats() {
        lifecycleScope.launch {
            try {
                val dailyStats = repository.getDailyStats(7) // 获取最近7天的数据
                updateChartsUI(dailyStats)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateChartsUI(dailyStats: List<fr.smarquis.fcm.data.model.DailyStats>) {
        // 这里暂时显示文本形式的统计数据
        // 在后续任务中会添加真正的图表库
        
        val statsText = StringBuilder()
        statsText.append("最近7天连接统计:\n\n")
        
        dailyStats.forEach { stats ->
            val onlineTimeFormatted = formatDuration(stats.onlineTime)
            val avgDurationFormatted = formatDuration(stats.averageDuration)
            
            statsText.append("${stats.date}:\n")
            statsText.append("  在线时间: $onlineTimeFormatted\n")
            statsText.append("  连接次数: ${stats.connectionCount}\n")
            statsText.append("  平均时长: $avgDurationFormatted\n\n")
        }
        
        binding.textChartPlaceholder.text = statsText.toString()
    }

    private fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0分钟"
        
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        
        return when {
            hours > 0 -> "${hours}小时${minutes}分钟"
            minutes > 0 -> "${minutes}分钟"
            else -> "不足1分钟"
        }
    }
}
