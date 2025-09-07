package fr.smarquis.fcm.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import fr.smarquis.fcm.R

/**
 * 连接状态枚举
 */
enum class ConnectionStatus(@DrawableRes val icon: Int, @StringRes val label: Int) {
    CONNECTED(android.R.drawable.presence_online, R.string.connection_status_connected),
    DISCONNECTED(android.R.drawable.presence_invisible, R.string.connection_status_disconnected),
    ERROR(android.R.drawable.presence_busy, R.string.connection_status_error)
}

/**
 * 网络类型枚举
 */
enum class NetworkType(val displayName: String) {
    WIFI("WiFi"),
    MOBILE("移动网络"),
    ETHERNET("以太网"),
    UNKNOWN("未知")
}
