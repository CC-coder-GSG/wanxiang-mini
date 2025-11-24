package com.example.appauto

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import com.google.android.material.button.MaterialButton

class softwore_register : AppCompatActivity() {
    private lateinit var dbHelper: DBManager
    private var auth : String? = null
    private lateinit var recycleView: RecyclerView
    private lateinit var adapter: ContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_softwore_register)

        // 设置状态栏为深色，和 Toolbar 保持一致
        window.statusBarColor = Color.parseColor("#111827")
        // 使用浅色状态栏图标（白色），避免深色背景下看不清
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val button_softregister = findViewById<Button>(R.id.button_software_register)
        val button_software_new = findViewById<Button>(R.id.button_software_new)

        recycleView = findViewById(R.id.recycle_software_register)
        recycleView.layoutManager = LinearLayoutManager(this)
        //添加分割线
        val dividerItemDecoration = DividerItemDecoration(recycleView.context, LinearLayoutManager.VERTICAL)
        recycleView.addItemDecoration(dividerItemDecoration)

        adapter = ContentAdapter(emptyList())
        recycleView.adapter = adapter
        //连接数据库
        val dbHelper = DBManager(this)
        dbHelper.openDatabase()
        val db = dbHelper.database

        setSupportActionBar(toolbar)
        // 使用可在深色 Toolbar 上可见的返回箭头，并把图标/菜单染成白色
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)
        toolbar.setTitleTextColor(Color.WHITE)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        //软件注册码列表按钮点击事件
        button_softregister.setOnClickListener {
            //获取access_token
            val sql = "select * from luowang where user=\"345\""
            val dataresult = db.rawQuery(sql, null)
            dataresult.moveToFirst()
            val access_token =
                dataresult.getString(dataresult.getColumnIndexOrThrow("accesstoken"))
            auth = "bearer $access_token"
            dataresult.close()

            val gson = GsonBuilder().serializeNulls().create()
            val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
            val soft_retrofit = retrofit.create(software_register::class.java)

            val query = Query(true, "")
            val sort = Sort("")

            // ===== 打印请求体 =====
            val reqBody = software_post("", 1, query, 20, sort)

            soft_retrofit.software_register(reqBody, auth.toString()).enqueue(object :
                Callback<software_post_return>{
                override fun onResponse(p0: Call<software_post_return>, p1: Response<software_post_return>
                ) {
                    val result = p1.body()
                    if(result == null){
                        Toast.makeText(applicationContext,"请重新登录", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        adapter.updateData(result.data.content)
                    }
                }

                override fun onFailure(p0: Call<software_post_return>, p1: Throwable) {
                    p1.printStackTrace()
                    Toast.makeText(applicationContext,p1.message, Toast.LENGTH_SHORT).show()
                }

            })
        }

        button_software_new.setOnClickListener {
            val intent = Intent(this, newsoftware::class.java)
            startActivity(intent)
            adapter.updateData(emptyList())
        }
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
}

class ContentAdapter(private var contentList: List<Content>) :
    RecyclerView.Adapter<ContentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewBatchNo: TextView = itemView.findViewById(R.id.textViewBatchNo)
        val textViewCreationTime: TextView = itemView.findViewById(R.id.textViewCreationTime)
        val textViewExpirationDate: TextView = itemView.findViewById(R.id.textViewExpirationDate)
        val textViewId: TextView = itemView.findViewById(R.id.textViewId)
        val textViewOperatorId: TextView = itemView.findViewById(R.id.textViewOperatorId)
        val textViewPassWord: TextView = itemView.findViewById(R.id.textViewPassWord)
        val textViewRedeemableDays: TextView = itemView.findViewById(R.id.textViewRedeemableDays)
        val textViewRemark: TextView = itemView.findViewById(R.id.textViewRemark)
        val textViewSalesMan: TextView = itemView.findViewById(R.id.textViewSalesMan)
        val textViewState: TextView = itemView.findViewById(R.id.textViewState)
        val button_code: MaterialButton = itemView.findViewById(R.id.button_code)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = contentList[position]
        holder.textViewBatchNo.text = content.batchNo
        holder.textViewCreationTime.text = "创建时间：${content.creationTime}"
        holder.textViewExpirationDate.text = "截至时间：${content.expirationDate}"
        holder.textViewId.text = "id值：${content.id}"
        holder.textViewOperatorId.text = "operatorID：${content.operatorId}"
        holder.textViewPassWord.text = "卡密：${content.passWord}"

        // ✅ 这里是新增展示 4 个功能有效期的核心逻辑
        val funcList = content.functions ?: emptyList()
        // functions 的 id 可能是 Int / "2" / 其它字符串，做一次稳健匹配
        fun daysFor(fid: Int): Int? = funcList.firstOrNull { f ->
            val idStr = f.id.toString()
            idStr == fid.toString() || idStr.toIntOrNull() == fid
        }?.regDays

        val langDays = daysFor(2)?.takeIf { it > 0 }   // 多语言
        val railDays = daysFor(3)?.takeIf { it > 0 }   // 铁路测量
        val plotDays = daysFor(4)?.takeIf { it > 0 }   // 样方放样
        val geoDays  = daysFor(5)?.takeIf { it > 0 }   // 物探放样

        val redeemableText = buildString {
            append("软件注册时效：${content.redeemableDays}天")
            langDays?.let { append("\n多语言功能时效：${it}天") }
            railDays?.let { append("\n铁路测量功能时效：${it}天") }
            plotDays?.let { append("\n样方放样功能时效：${it}天") }
            geoDays?.let  { append("\n物探放样功能时效：${it}天") }
        }
        holder.textViewRedeemableDays.text = redeemableText

        holder.textViewRemark.text = "备注：${content.remark}"
        holder.textViewSalesMan.text = "归属人：${content.salesMan}"
        holder.textViewState.text = "state：${content.state}"

        //按钮点击事件
        holder.button_code.setOnClickListener {
            val code = content.passWord
            //二维码生成
            val bitmap = generateQRCodeBitmap(code)
            if (bitmap != null) {
                showQRCodeDialog(holder.itemView.context, bitmap)
            }
            else{
                Toast.makeText(holder.itemView.context, "二维码生成失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    fun updateData(newContentList: List<Content>) {
        contentList = newContentList
        notifyDataSetChanged()
    }

    private fun generateQRCodeBitmap(content: String): Bitmap?{
        val size = 512
        var bits = BitMatrix(size)
        val qrCodeWriter = QRCodeWriter()
        try {
            bits = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
        }catch (e: WriterException){
            e.printStackTrace()
        }

        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] = if (bits[x, y]) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        return bitmap
    }

    // 显示二维码弹出框
    private fun showQRCodeDialog(context: Context, bitmap: Bitmap) {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_qr_code, null)
        val imageViewQRCode = view.findViewById<ImageView>(R.id.imageViewQRCode)
        val buttonCopy = view.findViewById<MaterialButton>(R.id.buttonCopy)

        imageViewQRCode.setImageBitmap(bitmap)

        builder.setView(view)

        val dialog = builder.create()
        // 关键：去掉系统标题栏（顶部那条自带关闭）
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 让外层背景透明，避免还留一层系统框
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()

        val btnClose = view.findViewById<TextView>(R.id.btnClose)
        btnClose.setOnClickListener { dialog.dismiss() }

        // 设置复制按钮点击事件
        buttonCopy.setOnClickListener {
            copyQRCodeToClipboard(context, bitmap)
        }

        // 设置长按监听器
        imageViewQRCode.setOnLongClickListener {
            copyQRCodeToClipboard(context, bitmap)
            true // 返回 true 表示已处理长按事件
        }
    }

    // 复制二维码到剪贴板
    private fun copyQRCodeToClipboard(context: Context, bitmap: Bitmap) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val bitmapDrawable = BitmapDrawable(context.resources, bitmap)
        val bitmapUri = getImageUri(context, bitmap)
        val clipData = ClipData.newUri(context.contentResolver, "QR Code", bitmapUri)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "二维码已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    // 将 Bitmap 转换为 Uri
    private fun getImageUri(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "QR Code", null)
        return Uri.parse(path)
    }

}
