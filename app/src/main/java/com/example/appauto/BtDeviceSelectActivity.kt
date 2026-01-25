package com.example.appauto

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class BtDeviceSelectActivity : AppCompatActivity() {

    private fun hasPermission(p: String): Boolean {
        return ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasScanAndConnectPerms(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // On API <= 30, discovery typically requires location; we request both fine+coarse.
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private fun safeIsBluetoothEnabled(): Boolean {
        return try {
            btAdapter?.isEnabled == true
        } catch (_: SecurityException) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun safeName(device: BluetoothDevice): String {
        return try {
            device.name ?: "(未命名设备)"
        } catch (_: SecurityException) {
            "(无权限读取名称)"
        }
    }

    @SuppressLint("MissingPermission")
    private fun safeMac(device: BluetoothDevice): String {
        return try {
            device.address ?: ""
        } catch (_: SecurityException) {
            ""
        }
    }

    private val btAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val devices = LinkedHashMap<String, BtUiDevice>() // mac -> device
    private lateinit var rv: RecyclerView
    private lateinit var tvTip: TextView

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val ok = granted.values.all { it }
        if (ok) startDiscovery()
        else tvTip.text = "提示：缺少蓝牙权限，无法扫描。请在系统设置中授权后重试。"
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    if (!hasScanAndConnectPerms()) return
                    val d: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (d == null) return

                    val mac = safeMac(d)
                    if (mac.isBlank()) return
                    val name = safeName(d)

                    devices[mac] = BtUiDevice(name = name, mac = mac)
                    (rv.adapter as? BtListAdapter)?.submit(devices.values.toList())
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    tvTip.text = "正在扫描…"
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    tvTip.text = if (devices.isEmpty()) {
                        "未搜索到设备。请确认：1）已开启蓝牙 2）RTK设备已上电 3）已在系统中配对。"
                    } else {
                        "点击设备即可返回并填充参数。"
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bt_select)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        tvTip = findViewById(R.id.tv_tip)
        rv = findViewById(R.id.rv_devices)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = BtListAdapter { item ->
            val data = Intent()
                .putExtra(EXTRA_BT_NAME, item.name)
                .putExtra(EXTRA_BT_MAC, item.mac)
            setResult(RESULT_OK, data)
            finish()
        }

        findViewById<MaterialButton>(R.id.btn_scan).setOnClickListener {
            ensurePermAndScan()
        }

        registerReceiver(
            receiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
        )

        ensurePermAndScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        try {
            btAdapter?.cancelDiscovery()
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    private fun ensurePermAndScan() {
        if (btAdapter == null) {
            tvTip.text = "此设备不支持蓝牙。"
            return
        }
        if (!safeIsBluetoothEnabled()) {
            tvTip.text = "请先打开系统蓝牙后再扫描（或授予蓝牙权限后重试）。"
            return
        }

        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 31) {
            perms += Manifest.permission.BLUETOOTH_SCAN
            perms += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            perms += Manifest.permission.ACCESS_FINE_LOCATION
            perms += Manifest.permission.ACCESS_COARSE_LOCATION
        }

        val need = perms.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (need) permLauncher.launch(perms.toTypedArray())
        else startDiscovery()
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        if (!hasScanAndConnectPerms()) {
            tvTip.text = "提示：缺少权限，无法扫描。请授权后重试。"
            return
        }
        try { btAdapter?.cancelDiscovery() } catch (_: Exception) {}

        devices.clear()
        (rv.adapter as? BtListAdapter)?.submit(emptyList())

        // 先展示“已配对”的设备（RTK通常是已配对设备，体验更好）
        try {
            btAdapter?.bondedDevices.orEmpty().forEach { d ->
                val mac = safeMac(d)
                if (mac.isBlank()) return@forEach
                val name = safeName(d)
                devices[mac] = BtUiDevice(name = name, mac = mac)
            }
            (rv.adapter as? BtListAdapter)?.submit(devices.values.toList())
        } catch (se: SecurityException) {
            tvTip.text = "提示：无权限读取已配对设备，请授权后重试。"
        } catch (_: Exception) {
        }

        try {
            btAdapter?.startDiscovery()
        } catch (se: SecurityException) {
            tvTip.text = "提示：无权限开始扫描，请授权后重试。"
        }
    }

    data class BtUiDevice(val name: String, val mac: String)

    companion object {
        const val EXTRA_BT_NAME = "extra_bt_name"
        const val EXTRA_BT_MAC = "extra_bt_mac"
    }
}

private class BtListAdapter(
    private val onClick: (BtDeviceSelectActivity.BtUiDevice) -> Unit
) : RecyclerView.Adapter<BtListVH>() {

    private val items = ArrayList<BtDeviceSelectActivity.BtUiDevice>()

    fun submit(list: List<BtDeviceSelectActivity.BtUiDevice>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): BtListVH {
        val v = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bt_device, parent, false)
        return BtListVH(v)
    }

    override fun onBindViewHolder(holder: BtListVH, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}

private class BtListVH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    private val tvName: TextView = itemView.findViewById(R.id.tv_name)
    private val tvMac: TextView = itemView.findViewById(R.id.tv_mac)

    fun bind(item: BtDeviceSelectActivity.BtUiDevice) {
        tvName.text = item.name
        tvMac.text = item.mac
    }
}