package fr.smarquis.fcm.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.smarquis.fcm.data.model.ConnectionHistory
import fr.smarquis.fcm.data.model.ConnectionStatus
import fr.smarquis.fcm.databinding.ItemConnectionHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ConnectionHistoryAdapter : ListAdapter<ConnectionHistory, ConnectionHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConnectionHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemConnectionHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())

        fun bind(history: ConnectionHistory) {
            binding.apply {
                // 设置状态图标和文本
                imageStatus.setImageResource(history.status.icon)
                textStatus.text = itemView.context.getString(history.status.label)
                
                // 设置时间
                textTimestamp.text = dateFormat.format(Date(history.timestamp))
                
                // 设置网络类型
                textNetworkType.text = history.networkType?.displayName ?: "未知"
                
                // 设置持续时间（仅对断开连接显示）
                if (history.status == ConnectionStatus.DISCONNECTED && history.duration != null) {
                    textDuration.text = formatDuration(history.duration)
                    textDuration.visibility = android.view.View.VISIBLE
                } else {
                    textDuration.visibility = android.view.View.GONE
                }
                
                // 设置设备信息
                textDeviceInfo.text = history.deviceInfo ?: ""
            }
        }

        private fun formatDuration(durationMs: Long): String {
            if (durationMs <= 0) return "0秒"
            
            val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
            
            return when {
                hours > 0 -> "${hours}小时${minutes}分钟"
                minutes > 0 -> "${minutes}分钟${seconds}秒"
                else -> "${seconds}秒"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ConnectionHistory>() {
        override fun areItemsTheSame(oldItem: ConnectionHistory, newItem: ConnectionHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ConnectionHistory, newItem: ConnectionHistory): Boolean {
            return oldItem == newItem
        }
    }
}
