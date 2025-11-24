package com.example.appauto

import android.widget.TextView

import android.content.Intent
import android.graphics.Color
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.content.DialogInterface
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.appcompat.widget.Toolbar
import android.content.Context
import android.view.inputmethod.InputMethodManager

class account_of_cors : AppCompatActivity() {

    private lateinit var etKeyword: AppCompatEditText
    private lateinit var btnQuery: MaterialButton
    private lateinit var rvList: RecyclerView
    private lateinit var tvPageInfo: TextView
    private lateinit var etPageInput: AppCompatEditText
    private lateinit var btnPageGo: MaterialButton
    private lateinit var btnPrevPage: MaterialButton
    private lateinit var btnNextPage: MaterialButton
    private var auth : String? = null
    private lateinit var dbHelper: DBManager

    // Shared Gson / Retrofit
    private val gson by lazy { GsonBuilder().serializeNulls().create() }
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://cloud.sinognss.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // 查询差分账号密码接口
    private val passwordApi by lazy {
        retrofit.create(cors_account_password_check::class.java)
    }

    // 重置差分账号密码接口
    private val resetApi by lazy {
        retrofit.create(cors_password_reset::class.java)
    }

    // 自定义差分账号密码接口
    private val customApi by lazy {
        retrofit.create(cors_password_custom::class.java)
    }

    /** 确保 auth 可用（从本地数据库取 accesstoken） */
    private fun ensureAuth(): String? {
        if (!auth.isNullOrBlank()) return auth
        return try {
            dbHelper = DBManager(this)
            dbHelper.openDatabase()
            val db = dbHelper.database
            val sql = "select * from luowang where user=\"345\""
            val c = db.rawQuery(sql, null)
            var token: String? = null
            if (c.moveToFirst()) {
                token = c.getString(c.getColumnIndexOrThrow("accesstoken"))
            }
            c.close()
            db.close()
            dbHelper.closeDatabase()
            auth = if (token.isNullOrBlank()) null else "bearer $token"
            auth
        } catch (e: Exception) {
            Log.e("cors_account_pwd", "load auth failed", e)
            null
        }
    }

    private val pageSize = 10
    private var currentPage = 1
    private var totalPages = 1
    private var totalCount = 0

    private var loading = false
    private var lastKeyword: String = ""

    private lateinit var adapter: CorsAccountAdapter

    /** 强制查询密码并显示（不做查看/隐藏切换） */
    private fun fetchAndShowPassword(id: String) {
        val a = ensureAuth()
        if (a.isNullOrBlank()) {
            toast("查询失败，请尝试重新登陆。")
            return
        }

        if (loading) return
        loading = true

        val req = PasswordCheck(id = id)
        val reqJson = gson.toJson(req)
        Log.d("cors_account_pwd", "Request url=https://cloud.sinognss.com/gateway/BaseUser/userInfo/checkPass")
        Log.d("cors_account_pwd", "Request body=$reqJson")

        passwordApi.password_check(req, a).enqueue(object : Callback<Password> {
            override fun onResponse(call: Call<Password>, response: Response<Password>) {
                loading = false
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    toast("查询密码失败。" + body.toString())
                    return
                }
                if (body.code != 0) {
                    toast(body.message)
                    return
                }
                adapter.showPassword(id, body.data)
            }

            override fun onFailure(call: Call<Password>, t: Throwable) {
                loading = false
                Log.e("cors_account_pwd", "Request failed", t)
                toast(t.message ?: "网络错误")
            }
        })
    }

    private fun handleViewPwd(item: RecordCors) {
        val id = item.id.toString()

        // 如果当前正在显示明文，则点击后隐藏
        if (adapter.isPasswordVisible(id)) {
            adapter.hidePassword(id)
            return
        }

        fetchAndShowPassword(id)
    }

    private fun handleResetPwd(item: RecordCors) {
        val id = item.id.toString()
        val wasVisible = adapter.isPasswordVisible(id)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("重置密码")
            .setMessage("确认要重置该账号的差分密码吗？")
            .setNegativeButton("取消") { d, _ -> d.dismiss() }
            .setPositiveButton("确认") { d, _ ->
                d.dismiss()

                val a = ensureAuth()
                if (a.isNullOrBlank()) {
                    toast("重置失败，请重新登陆")
                    return@setPositiveButton
                }

                if (loading) return@setPositiveButton
                loading = true

                val req = PasswordCheck(id = id)
                val reqJson = gson.toJson(req)
                Log.d("cors_account_reset", "Request url=https://cloud.sinognss.com/gateway/BaseUser/userInfo/resetPass")
                Log.d("cors_account_reset", "Request body=$reqJson")

                // 接口返回是 Call<PasswordReset>
                resetApi.password_correct(req, a).enqueue(object : Callback<PasswordReset> {
                    override fun onResponse(call: Call<PasswordReset>, response: Response<PasswordReset>) {
                        loading = false
                        val body = response.body()
                        if (!response.isSuccessful || body == null) {
                            toast("重置密码失败")
                            return
                        }
                        toast(body.message ?: "重置成功")

                        if (wasVisible) {
                            fetchAndShowPassword(id)
                        }
                    }

                    override fun onFailure(call: Call<PasswordReset>, t: Throwable) {
                        loading = false
                        Log.e("cors_account_reset", "Request failed", t)
                        toast(t.message ?: "网络错误")
                    }
                })
            }
            .create()

        dialog.setOnShowListener {
            val dp = resources.displayMetrics.density
            val radiusPx = (8f * dp)
            val strokePx = (1f * dp).toInt()
            val black = Color.parseColor("#111827")
            val line = Color.parseColor("#E5E7EB")

            // 确认按钮：黑底白字
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.let { btn ->
                btn.isAllCaps = false
                btn.textSize = 14f
                btn.setPadding((16 * dp).toInt(), (7 * dp).toInt(), (16 * dp).toInt(), (7 * dp).toInt())

                if (btn is MaterialButton) {
                    btn.cornerRadius = radiusPx.toInt()
                    btn.strokeWidth = 0
                    btn.backgroundTintList = ColorStateList.valueOf(black)
                    btn.setTextColor(Color.WHITE)
                } else {
                    btn.setTextColor(Color.WHITE)
                    val gd = GradientDrawable().apply {
                        cornerRadius = radiusPx
                        setColor(black)
                    }
                    btn.background = gd
                }
            }

            // 取消按钮：白底黑字（浅灰描边）
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.let { btn ->
                btn.isAllCaps = false
                btn.textSize = 14f
                btn.setPadding((16 * dp).toInt(), (7 * dp).toInt(), (16 * dp).toInt(), (7 * dp).toInt())

                if (btn is MaterialButton) {
                    btn.cornerRadius = radiusPx.toInt()
                    btn.strokeWidth = strokePx
                    btn.strokeColor = ColorStateList.valueOf(line)
                    btn.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                    btn.setTextColor(black)
                } else {
                    btn.setTextColor(black)
                    val gd = GradientDrawable().apply {
                        cornerRadius = radiusPx
                        setColor(Color.WHITE)
                        setStroke(strokePx, line)
                    }
                    btn.background = gd
                }
            }
        }

        dialog.show()
    }


    private fun handleCustomPwd(item: RecordCors) {
        val idInt = item.id
        val idStr = idInt.toString()
        val wasVisible = adapter.isPasswordVisible(idStr)

        val view = layoutInflater.inflate(R.layout.dialog_custom_password, null)
        val etNewPwd = view.findViewById<AppCompatEditText>(R.id.et_new_password)

        // dialog_custom_password.xml 已包含按钮，所以这里不要再 setPositive/NegativeButton
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(view)
            .create()

        val btnConfirm = view.findViewById<MaterialButton>(R.id.btn_confirm)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btn_cancel)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            val newPwd = etNewPwd.text?.toString()?.trim().orEmpty()
            if (newPwd.isEmpty()) {
                toast("请输入新密码")
                return@setOnClickListener
            }

            val a = ensureAuth()
            if (a.isNullOrBlank()) {
                toast("修改失败，请重新登陆")
                return@setOnClickListener
            }

            if (loading) return@setOnClickListener
            loading = true

            val req = PasswordCustom(id = idInt, password = newPwd)
            val reqJson = gson.toJson(req)
            Log.d("cors_account_custom", "Request url=https://cloud.sinognss.com/gateway/BaseUser/userInfo/customPass")
            Log.d("cors_account_custom", "Request body=$reqJson")

            customApi.password_custom(req, a).enqueue(object : Callback<PasswordReset> {
                override fun onResponse(call: Call<PasswordReset>, response: Response<PasswordReset>) {
                    loading = false
                    val body = response.body()
                    if (!response.isSuccessful || body == null) {
                        toast("自定义密码失败")
                        return
                    }
                    toast(body.message ?: "修改成功")
                    dialog.dismiss()

                    if (wasVisible) {
                        fetchAndShowPassword(idStr)
                    }
                }

                override fun onFailure(call: Call<PasswordReset>, t: Throwable) {
                    loading = false
                    Log.e("cors_account_custom", "Request failed", t)
                    toast(t.message ?: "网络错误")
                }
            })
        }

        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_of_cors)

        etKeyword = findViewById(R.id.et_account_query)
        btnQuery = findViewById(R.id.btn_query_account)
        rvList = findViewById(R.id.rv_account_list)

        tvPageInfo = findViewById(R.id.tv_page_info)
        etPageInput = findViewById(R.id.et_page_input)
        btnPageGo = findViewById(R.id.btn_page_go)
        btnPrevPage = findViewById(R.id.btn_page_prev)
        btnNextPage = findViewById(R.id.btn_page_next)

        updatePageInfo()

        adapter = CorsAccountAdapter(
            onViewPwd = { item -> handleViewPwd(item) },
            onResetPwd = { item -> handleResetPwd(item) },
            onCustomPwd = { item -> handleCustomPwd(item) }
        )

        // 三个点图标白色（防止黑底看不见）
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // 使用可在深色 Toolbar 上可见的返回箭头，并把图标/菜单染成白色
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)
        toolbar.setTitleTextColor(Color.WHITE)

        toolbar.setNavigationOnClickListener {
            finish()
        }
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.setNavigationOnClickListener{
            finish()
        }
        setupRecycler()
        setupEvents()
    }


    private fun setupRecycler() {
        rvList.layoutManager = LinearLayoutManager(this)
        rvList.adapter = adapter

        // 下拉/滑动到底部自动加载下一页
        rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                if (loading) return
                if (currentPage >= totalPages) return

                val lm = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = lm.findLastVisibleItemPosition()
                if (lastVisible >= adapter.itemCount - 2) {
                    currentPage += 1
                    requestPage(reset = false)
                }
            }
        })
    }

    private fun setupEvents() {
        btnQuery.setOnClickListener {
            hideKeyboard()
            startNewSearch()
        }

        etKeyword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_GO) {
                hideKeyboard()
                startNewSearch()
                true
            } else false
        }

        btnPrevPage.setOnClickListener {
            if (currentPage > 1 && !loading) {
                currentPage -= 1
                adapter.setItems(emptyList())
                requestPage(reset = true)
            }
        }

        btnNextPage.setOnClickListener {
            if (currentPage < totalPages && !loading) {
                currentPage += 1
                adapter.setItems(emptyList())
                requestPage(reset = true)
            }
        }

        btnPageGo.setOnClickListener {
            val input = etPageInput.text?.toString()?.trim()
            val target = input?.toIntOrNull()
            if (target == null) {
                toast("请输入有效页码")
                return@setOnClickListener
            }
            val clamped = target.coerceIn(1, totalPages.coerceAtLeast(1))
            if (clamped != currentPage && !loading) {
                currentPage = clamped
                adapter.setItems(emptyList())
                requestPage(reset = true)
            }
        }
    }

    private fun startNewSearch() {
        lastKeyword = etKeyword.text?.toString()?.trim() ?: ""
        currentPage = 1
        totalPages = 1
        totalCount = 0

        adapter.setItems(emptyList())
        updatePageInfo()
        requestPage(reset = true)
    }

    private fun requestPage(reset: Boolean) {
        if (loading) return
        loading = true

        val req = AccountOfCorsGet(
            keyword = lastKeyword, // 空就 ""
            size = pageSize,
            current = currentPage,
            conditions = Conditions(
                accountStatus = "",
                activeStatus = "",
                accountType = "",
                instanceId = ""
            )
        )

        val a = ensureAuth()
        if (a.isNullOrBlank()) {
            loading = false
            toast("查询失败，请重新登陆")
            return
        }

        val reqJson = gson.toJson(req)
        Log.d("cors_account_get", "Request url=https://cloud.sinognss.com/gateway/BaseUser/userInfo/list")
        Log.d("cors_account_get", "Request body=$reqJson")
        Log.d("cors_account_get", "Authorization=$a")
        val cors_account_get = retrofit.create(cors_account_get::class.java)
        cors_account_get.cors_account_get(req, a).enqueue(object : Callback<CorsInformation> {
            override fun onResponse(call: Call<CorsInformation>, response: Response<CorsInformation>) {
                loading = false
                Log.d("cors_account_get", "HTTP code=${response.code()}")
                if (!response.isSuccessful) {
                    Log.d("cors_account_get", "Raw response=${response.errorBody()?.string()}")
                }

                val body = response.body()

                if (!response.isSuccessful || body == null) {
                    toast("查询失败，请重新登陆")
                    return
                }

                if (body.code != 0) {
                    toast(body.message)
                    return
                }

                val pageData = body.data
                val records = pageData.records

                totalPages = pageData.pages
                totalCount = pageData.total
                currentPage = pageData.current
                updatePageInfo()

                if (reset) {
                    adapter.setItems(records)
                } else {
                    adapter.addItems(records)
                }

                if (records.isEmpty() && reset) toast("暂无数据")
            }

            override fun onFailure(call: Call<CorsInformation>, t: Throwable) {
                Log.e("cors_account_get", "Request failed", t)
                loading = false
                toast(t.message ?: "网络错误")
            }
        })
    }

    private fun updatePageInfo() {
        tvPageInfo.text = "第 ${currentPage} / ${totalPages} 页  ·  共 ${totalCount} 条"
        btnPrevPage.isEnabled = currentPage > 1
        btnNextPage.isEnabled = currentPage < totalPages
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val button_luowang = item.itemId
        if (button_luowang==R.id.luowang){
            val intent = Intent(this, lwaccount::class.java)
            startActivity(intent)
        }
        return true
    }

    /** 收起软键盘 */
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val v = currentFocus ?: etKeyword
        imm?.hideSoftInputFromWindow(v.windowToken, 0)
        v.clearFocus()
    }

}