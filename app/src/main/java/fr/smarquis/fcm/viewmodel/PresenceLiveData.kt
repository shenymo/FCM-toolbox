package fr.smarquis.fcm.viewmodel

import android.app.Application
import android.os.Build.MANUFACTURER
import android.os.Build.MODEL
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import fr.smarquis.fcm.BuildConfig
import fr.smarquis.fcm.data.model.ConnectionHistory
import fr.smarquis.fcm.data.model.ConnectionStatus
import fr.smarquis.fcm.data.model.Presence
import fr.smarquis.fcm.data.model.Presence.Error
import fr.smarquis.fcm.data.model.Presence.Offline
import fr.smarquis.fcm.data.model.Presence.Online
import fr.smarquis.fcm.data.model.Token
import fr.smarquis.fcm.data.repository.ConnectionHistoryRepository
import fr.smarquis.fcm.usecase.GetTokenUseCase
import fr.smarquis.fcm.utils.NetworkUtils
import fr.smarquis.fcm.utils.uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PresenceLiveData(
    private val application: Application,
    database: FirebaseDatabase,
    getTokenUseCase: GetTokenUseCase,
    private val connectionHistoryRepository: ConnectionHistoryRepository,
    scope: CoroutineScope,
) : LiveData<Presence>(Offline), ValueEventListener, CoroutineScope by scope {

    private var token: Token? = null
    private var lastConnectionTime: Long? = null
    private var currentPresence: Presence = Offline
    private val presenceRef: DatabaseReference = database.getReference(".info/connected")
    private val connectionRef: DatabaseReference = database.getReference("devices/${uuid(application)}").apply {
        onDisconnect().removeValue()
    }

    init {
        launch {
            getTokenUseCase().collectLatest {
                token = it
                updateMetadata()
            }
        }
    }

    override fun onActive() {
        presenceRef.addValueEventListener(this)
        DatabaseReference.goOnline()
        updateMetadata()
        Log.d("PresenceLiveData", "onActive called")
    }

    override fun onInactive() {
        clearMetadata()
        DatabaseReference.goOffline()
        presenceRef.removeEventListener(this)
        value = Offline
    }

    override fun onCancelled(error: DatabaseError) {
        value = Error(error)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        val newPresence = when (snapshot.getValue(Boolean::class.java)) {
            true -> Online
            false, null -> Offline
        }

        // 记录连接状态变化
        if (newPresence != currentPresence) {
            recordConnectionChange(newPresence)
            currentPresence = newPresence
        }

        value = newPresence
        updateMetadata()
    }

    private fun updateMetadata() = connectionRef.setValue(metadata(token))

    private fun clearMetadata() = connectionRef.removeValue()

    private fun metadata(token: Token?) = mapOf(
        "name" to if (MODEL.lowercase().startsWith(MANUFACTURER.lowercase())) MODEL else MANUFACTURER.lowercase() + " " + MODEL,
        "token" to when (token) {
            is Token.Success -> token.value
            Token.Loading, is Token.Failure, null -> null
        },
        "version" to BuildConfig.VERSION_CODE,
        "timestamp" to ServerValue.TIMESTAMP,
    )

    private fun recordConnectionChange(newPresence: Presence) {
        launch {
            val currentTime = System.currentTimeMillis()
            val networkType = NetworkUtils.getCurrentNetworkType(application)
            val deviceInfo = if (MODEL.lowercase().startsWith(MANUFACTURER.lowercase())) MODEL else "$MANUFACTURER $MODEL"

            Log.d("PresenceLiveData", "Recording connection change: $newPresence, networkType: $networkType")

            when (newPresence) {
                is Online -> {
                    // 记录连接事件
                    val connectionHistory = ConnectionHistory(
                        timestamp = currentTime,
                        status = ConnectionStatus.CONNECTED,
                        networkType = networkType,
                        deviceInfo = deviceInfo
                    )
                    connectionHistoryRepository.insert(connectionHistory)
                    lastConnectionTime = currentTime
                    Log.d("PresenceLiveData", "Inserted connection record: $connectionHistory")
                }
                is Offline -> {
                    // 记录断开事件，并计算连接持续时间
                    val duration = lastConnectionTime?.let { currentTime - it }
                    val disconnectionHistory = ConnectionHistory(
                        timestamp = currentTime,
                        status = ConnectionStatus.DISCONNECTED,
                        networkType = networkType,
                        duration = duration,
                        deviceInfo = deviceInfo
                    )
                    connectionHistoryRepository.insert(disconnectionHistory)
                    Log.d("PresenceLiveData", "Inserted disconnection record: $disconnectionHistory, duration: $duration")

                    // 如果有连接时间，更新最后一次连接记录的持续时间
                    if (duration != null && lastConnectionTime != null) {
                        // 这里可以考虑更新最后一次连接记录的持续时间
                        // 但为了简化，我们在断开时记录持续时间
                    }
                    lastConnectionTime = null
                }
                is Error -> {
                    // 记录错误事件
                    val errorHistory = ConnectionHistory(
                        timestamp = currentTime,
                        status = ConnectionStatus.ERROR,
                        networkType = networkType,
                        deviceInfo = deviceInfo
                    )
                    connectionHistoryRepository.insert(errorHistory)
                }
            }
        }
    }

}
