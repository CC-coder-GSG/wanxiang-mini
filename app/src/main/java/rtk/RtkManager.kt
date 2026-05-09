package rtk

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.sinognss.gnss.core.RtkClientListener
import com.sinognss.gnss.core.NtripClientListener
import com.sinognss.gnss.core.NtripConnectClient
import com.sinognss.gnss.core.RtkConnectClient
import com.sinognss.gnss.core.data.*
import com.sinognss.gnss.connect.ConnectStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.Dispatcher

object RtkManager {
    private const val TAG = "RtkManager"

    private var rtkClient: RtkConnectClient? = null
    private var ntripClient: NtripConnectClient? = null

    private val _state = MutableStateFlow<RtkState>(RtkState.Idle)
    val state: StateFlow<RtkState> = _state

    private val _nmea = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val nmea: SharedFlow<String> = _nmea

    // 流量监测流
    private val _serverTraffic = MutableSharedFlow<Int>(extraBufferCapacity = 64)
    val serverTraffic: SharedFlow<Int> = _serverTraffic

    suspend fun connect(
        scope: LifecycleCoroutineScope,
        context: Context,
        host: String,
        port: Int,
        user: String,
        pwd: String,
        mount: String,
        btName: String,
        btMac: String
    ) {
        _state.value = RtkState.Connecting
        Log.i(TAG, ">>> [调试] 开始连接: $host:$port/$mount")

        try {
            if (host.isBlank()) throw IllegalArgumentException("IP不能为空")
            if (mount.isBlank()) throw IllegalArgumentException("挂载点不能为空")

            rtkClient = RtkConnectClient.Builder().build()
            ntripClient = NtripConnectClient.Builder()
                .setHost(host).setPort(port).setUserName(user).setPassword(pwd).setMountPoint(mount)
                .build()

            rtkClient?.connect(btName, btMac, object : RtkClientListener {
                override fun onGnssMessage(message: MessageResult) {
                    val rawMsg = message.toString()
                    _nmea.tryEmit(rawMsg) // UI显示

                    // 兼容各类 GGA 上传
                    if (rawMsg.contains("GGA")) {
                        if (message is Gpgga) {
                            try {
                                // 限制日志频率，别刷屏
                                if (System.currentTimeMillis() % 3000 < 50) {
                                    Log.i(TAG, ">>> [调试] ↑ 正在上传 GGA 位置...")
                                }
                                // 始终保持上传，不受下行数据影响
                                scope.launch (Dispatchers.IO){
                                    ntripClient?.sendGgaData(message)
                                }
                            } catch (t: Throwable) {
                                Log.e(TAG, "GGA上传失败", t)
                            }
                        }
                    }
                }

                override fun onStatus(status: Int) {
                    _state.value = RtkState.fromRtkCode(status)
                    if (status == ConnectStatus.STATE_CONNECTED) {
                        startNtrip(context)
                    }
                }
            })
        } catch (t: Throwable) {
            _state.value = RtkState.Error(-1, "连接失败: ${t.message}")
        }
    }

    private fun startNtrip(context: Context) {
        try {
            ntripClient?.connect(context, object : NtripClientListener {
                override fun onStatus(status: Int) {
                    if (status < 0) _state.value = RtkState.Error(status, "Ntrip异常($status)")
                }
            }) { diffData ->
                // 🟢【核心修正】智能拦截模式
                if (diffData != null && diffData.isNotEmpty()) {
                    val firstByte = diffData[0].toInt() and 0xFF
                    // ✅ 情况1: 收到标准 RTCM 数据
                    // 动作: 转发给RTK + 更新UI + 打印日志
                    if (System.currentTimeMillis() % 3000 < 50) {
                        Log.i(TAG, ">>> [调试] ↓ 收到有效差分数据 (${diffData.size}B)")
                    }

                    _serverTraffic.tryEmit(diffData.size) // 变绿

                    try {
                        rtkClient?.sendCommand(diffData) // 写入蓝牙
                    } catch (t: Throwable) {
                        Log.e(TAG, "写入蓝牙失败", t)
                    }
                }
            }
        } catch (t: Throwable) {
            _state.value = RtkState.Error(-102, "启动失败: ${t.message}")
        }
    }

    suspend fun disconnect() {
        try { ntripClient?.disConnect(); rtkClient?.disconnect() } catch (_: Exception) {}
        finally { _state.value = RtkState.Idle }
    }
}

// 状态类保持不变
sealed class RtkState {
    data object Idle : RtkState()
    data object Connecting : RtkState()
    data class Connected(val code: Int) : RtkState()
    data class Error(val code: Int, val msg: String) : RtkState()

    companion object {
        fun fromRtkCode(code: Int): RtkState {
            return when (code) {
                ConnectStatus.STATE_CONNECTED -> Connected(code)
                ConnectStatus.STATE_ERROR -> Error(code, "设备连接错误")
                else -> Connecting
            }
        }
    }
}