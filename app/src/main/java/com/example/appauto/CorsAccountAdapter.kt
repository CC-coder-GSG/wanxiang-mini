package com.example.appauto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CorsAccountAdapter(
    private val onViewPwd: (RecordCors) -> Unit,
    private val onResetPwd: (RecordCors) -> Unit,
    private val onCustomPwd: (RecordCors) -> Unit
) : RecyclerView.Adapter<CorsAccountAdapter.VH>() {

    private val items = mutableListOf<RecordCors>()
    private val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    // 密码显示状态（按 id 记录，避免修改 RecordCors 数据类）
    private val passwordMap = mutableMapOf<String, String>()
    private val visibleSet = mutableSetOf<String>()

    fun isPasswordVisible(id: String): Boolean = visibleSet.contains(id)

    fun showPassword(id: String, pwd: String) {
        passwordMap[id] = pwd
        visibleSet.add(id)
        notifyItemChangedById(id)
    }

    fun hidePassword(id: String) {
        visibleSet.remove(id)
        notifyItemChangedById(id)
    }

    private fun notifyItemChangedById(id: String) {
        val idx = items.indexOfFirst { it.id.toString() == id }
        if (idx >= 0) notifyItemChanged(idx)
    }

    fun setItems(list: List<RecordCors>) {
        // 刷新列表时清理密码显示状态
        passwordMap.clear()
        visibleSet.clear()

        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun addItems(list: List<RecordCors>) {
        val start = items.size
        items.addAll(list)
        notifyItemRangeInserted(start, list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account_of_cors, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val record = items[position]

        // 必显字段
        h.tvAccount.text = record.name
        val id = record.id.toString()
        val visible = visibleSet.contains(id)
        h.tvPassword.text = if (visible) (passwordMap[id] ?: "********") else "********"
        h.btnViewPwd.text = if (visible) "隐藏" else "查看"

        // 状态映射
        h.tvAccountStatus.text = if (record.accountStatus == 0) "启用" else "其他"
        h.tvActiveStatus.text = if (record.activeStatus == 0) "服务中" else "其他"
        h.tvAccountType.text = if (record.accountType == 0) "付费" else "其他"

        // 时间字段（ms 时间戳；Any? 安全解析）
        bindTime(h.tvLastActiveTime, record.lastActiveTime)
        bindTime(h.tvActiveTime, record.activeTime)
        bindTime(h.tvRegisterDate, record.registerdate)
        bindTime(h.tvExpireDate, record.expiredate)

        // 备注 / 实例ID（Any? -> String?）
        bindTextOrGone(h.tvRemark, anyToStr(record.remark))
        bindTextOrGone(h.tvInstanceId, anyToStr(record.instanceId))

        // 三个按钮先留空回调
        h.btnViewPwd.setOnClickListener { onViewPwd(record) }
        h.btnResetPwd.setOnClickListener { onResetPwd(record) }
        h.btnCustomPwd.setOnClickListener { onCustomPwd(record) }
    }

    override fun getItemCount(): Int = items.size

    /** Any? -> 毫秒 Long?（兼容 null / Long / Double / String） */
    private fun anyToLongMs(v: Any?): Long? {
        return when (v) {
            null -> null
            is Long -> v
            is Int -> v.toLong()
            is Double -> v.toLong()
            is Float -> v.toLong()
            is String -> v.toLongOrNull()
            else -> null
        }
    }

    private fun bindTime(tv: TextView, v: Any?) {
        val ms = anyToLongMs(v)
        if (ms == null || ms <= 0) {
            tv.visibility = View.GONE
        } else {
            tv.visibility = View.VISIBLE
            tv.text = sdf.format(Date(ms))
        }
    }

    private fun bindTextOrGone(tv: TextView, text: String?) {
        if (text.isNullOrBlank()) {
            tv.visibility = View.GONE
        } else {
            tv.visibility = View.VISIBLE
            tv.text = text
        }
    }

    private fun anyToStr(v: Any?): String? {
        if (v == null) return null
        val s = v.toString()
        return if (s.equals("null", true) || s.isBlank()) null else s
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvAccount: TextView = v.findViewById(R.id.tv_name_value)
        val tvPassword: TextView = v.findViewById(R.id.tv_password_value)

        val tvAccountStatus: TextView = v.findViewById(R.id.tv_account_status_value)
        val tvLastActiveTime: TextView = v.findViewById(R.id.tv_last_active_time_value)
        val tvActiveStatus: TextView = v.findViewById(R.id.tv_active_status_value)
        val tvActiveTime: TextView = v.findViewById(R.id.tv_active_time_value)

        val tvRegisterDate: TextView = v.findViewById(R.id.tv_register_date_value)
        val tvExpireDate: TextView = v.findViewById(R.id.tv_expire_date_value)

        val tvAccountType: TextView = v.findViewById(R.id.tv_account_type_value)
        val tvRemark: TextView = v.findViewById(R.id.tv_remark_value)
        val tvInstanceId: TextView = v.findViewById(R.id.tv_instance_id_value)

        val btnViewPwd: MaterialButton = v.findViewById(R.id.btn_pwd_view)
        val btnResetPwd: MaterialButton = v.findViewById(R.id.btn_pwd_reset)
        val btnCustomPwd: MaterialButton = v.findViewById(R.id.btn_pwd_custom)
    }
}