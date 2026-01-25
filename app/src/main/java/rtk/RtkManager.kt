package rtk

import android.content.Context
import com.sinognss.sdk.core.PdaRtkClient
import com.sinognss.sdk.core.RtkClientListener
import com.sinognss.sdk.core.data.MessageResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

object RtkManager {

    private var client: PdaRtkClient? = null

    private val _state = MutableStateFlow<RtkState>(RtkState.Idle)
    val state: StateFlow<RtkState> = _state

    private val _nmea = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val nmea: SharedFlow<String> = _nmea

    fun init(context: Context) {
        if (client != null) return
    }

    suspend fun connect(
        context: Context,
        host: String,
        port: Int,
        user: String,
        pwd: String,
        mount: String,
        commIdType: Int = 0,
        btName: String? = null,
        btMac: String? = null
    ) {
        _state.value = RtkState.Connecting

        // If there's an existing client, stop it first
        client?.let { old ->
            try {
                old.stop()
            } catch (_: Throwable) {
                // ignore
            }
            client = null
        }

        val c = PdaRtkClient.Builder()
            .setHost(host)
            .setPort(port)
            .setUserName(user)
            .setPassword(pwd)
            .setMountPoint(mount)
            .setCommidType(commIdType)
            .apply {
                if (commIdType == 1) {
                    setBluetoothName(btName ?: "")
                    setBluetoothAddress(btMac ?: "")
                }
            }
            .build()

            ?: run {
                _state.value = RtkState.Error(-999, "PdaRtkClient build() returned null")
                return
            }

        c.init(context.applicationContext)

        client = c
        c.start(object : RtkClientListener {
            override fun onGnssMessage(message: MessageResult) {
                // 把报文/内容转成字符串给上层（按你实际 MessageResult 结构取）
                _nmea.tryEmit(message.toString())
            }

            override fun onStatus(status: Int) {
                _state.value = RtkState.fromCode(status)
            }
        })
    }

    suspend fun disconnect() {
        val c = client ?: run {
            _state.value = RtkState.Idle
            return
        }
        try {
            c.stop()
        } finally {
            client = null
            _state.value = RtkState.Idle
        }
    }
}

sealed class RtkState {
    data object Idle : RtkState()
    data object Connecting : RtkState()
    data class Connected(val code: Int) : RtkState()
    data class Disconnected(val code: Int) : RtkState()
    data class Error(val code: Int, val msg: String) : RtkState()

    companion object {
        fun fromCode(code: Int): RtkState {
            // 状态码
            return when (code) {
                200 -> Connected(code)
                201 -> Disconnected(code)
                else -> if (code < 0) Error(code, "RTK error: $code") else Connected(code)
            }
        }
    }
}