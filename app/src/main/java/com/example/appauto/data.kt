package com.example.appauto


data class logindata(
    val code: Int,
    val `data`: Data,
    val message: String,
    val type: Int
)

data class Data(
    val access_token: String,
    val company_id: String,
    val expires_in: Int,
    val jti: String,
    val refresh_token: String,
    val scope: String,
    val token_type: String,
    val user_country_code: Any,
    val user_email: Any,
    val user_id: String,
    val user_name: String,
    val user_real_name: String,
    val user_tel: String
)

data class Currentuser(
    val code: Int,
    val `data`: Datacurrent,
    val message: String,
    val type: Int
)

data class Datacurrent(
    val companyId: Int,
    val countryCode: Int,
    val isCompanyUser: Boolean,
    val nickName: String,
    val token: String,
    val userCompanyName: String,
    val userId: Int,
    val userName: String,
    val userTel: String
)

data class receiver(
    val code: Int,
    val `data`: DataRE,
    val message: String,
    val type: Int
)

data class DataRE(
    val conditions: Any,
    val currPage: Int,
    val list: List<Item0>,
    val pageSize: Int,
    val totalCount: Int,
    val totalPage: Int
)

data class Item0(
    val createTime: Long,
    val deviceType: String,
    val duration: Int,
    val expireTime: Long,
    val id: Int,
    val isFarm: Boolean,
    val online: Boolean,
    val remainingTime: Int,
    val remark: String,
    val salesName: String,
    val sn: String,
    val status: Int,
    val users: Any
)

data class Receiverinfo(
    val name: String,
    val status: String
)

data class registerinfo(
    val code: Int,
    val `data`: Datar_register,
    val message: String
)

data class Datar_register(
    val pageModel: PageModel
)

data class PageModel(
    val countId: Any,
    val current: Int,
    val hitCount: Boolean,
    val maxLimit: Int,
    val optimizeCountSql: Boolean,
    val orders: List<Any>,
    val pages: Int,
    val records: List<Record>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
)

data class Record(
    val bindUsername: Any,
    val companyId: Int,
    val companyName: String,
    val createTime: String,
    val isHostNet: Boolean,
    val isNtrip: Boolean,
    val lastUpdateTime: String?,
    val lastUserName: String,
    val offHostNetTime: String?,
    val offNtripTime: String?,
    val permanentRegCode: Any,
    val permanentRegCodeHave: Boolean,
    val permanentRegCodeOn: Boolean,
    val productionType: String,
    val remark: Any,
    val sn: String,
    val tempRegCode: Any,
    val tempRegCodeExpireTime: Any,
    val tempRegCodeHave: Any,
    val tempRegDeadline: String,
    val tempRegMaxDuration: Any,
    val todayCanTrial: Boolean,
    val trialCode: Any,
    val trialCodeHave: Any,
    val trialDay: Int,
    val trialStatus: String
)

data class register_send(
    val keyword: Any,
    val orderItems: List<OrderItem>,
    val companyId: Any,
    val trialStatus: String?,
    val current: Any,
    val size: Any,
)

data class OrderItem(
    val asc: Any,
    val column: Any
)

data class register_time_extend(
    val isUpdateAll: Boolean,
    val snList: List<String>,
    val tempRegDeadline: String
)

data class extend_time_notice(
    val code: Int,
    val message: String
)

data class domestcode(
    val sn: String,
    val tempRegCodeExpireTime: String,
    val tempRegCodeType: String
)

data class domesticinfo(
    val code: Int,
    val message: String
)

data class finalinfo(
    val code: Int,
    val `data`: Data_finalinfo,
    val message: String
)

data class Data_finalinfo(
    val devRegInfo: DevRegInfo
)

data class DevRegInfo(
    val bindUsername: Any,
    val companyId: Int,
    val companyName: String,
    val createTime: String,
    val isHostNet: Boolean,
    val isNtrip: Boolean,
    val lastUpdateTime: String,
    val lastUserName: Any,
    val offHostNetTime: Any,
    val offNtripTime: Any,
    val permanentRegCode: Any,
    val permanentRegCodeHave: Boolean,
    val permanentRegCodeOn: Boolean,
    val productionType: String,
    val remark: String,
    val sn: String,
    val tempRegCode: Any,
    val tempRegCodeExpireTime: String,
    val tempRegCodeHave: Boolean,
    val tempRegDeadline: String,
    val tempRegMaxDuration: Any,
    val todayCanTrial: Any,
    val trialCode: Any,
    val trialCodeHave: Boolean,
    val trialDay: Int,
    val trialStatus: String
)

data class id_info(
    val code: Int,
    val `data`: Data_id,
    val message: String,
    val type: Int
)

data class Data_id(
    val accountType: String,
    val createTime: Long,
    val creatorName: String,
    val deviceType: String,
    val duration: Int,
    val expireTime: Long,
    val id: Int,
    val isFarm: Boolean,
    val isSpan: Boolean,
    val online: Boolean,
    val remainingTime: Int,
    val remark: String,
    val salesName: String,
    val sn: String,
    val status: Int,
    val users: Any
)

data class direct_more_message(
    val code: Int,
    val `data`: Any,
    val message: String,
    val type: Int
)

//软件注册码post请求json格式数据
data class software_post(
    val keyword: String,
    val page: Int,
    val query: Query,
    val size: Int,
    val sort: Sort
)

data class Query(
    val domestic: Boolean,
    val state: String
)

data class Sort(
    val creationTime: String
)

//软件注册码post请求，返回json格式数据
data class software_post_return(
    val code: Int,
    val `data`: Data_software,
    val message: String
)

data class Data_software(
    val content: List<Content>,
    val empty: Boolean,
    val first: Boolean,
    val last: Boolean,
    val number: Int,
    val numberOfElements: Int,
    val pageable: Pageable,
    val size: Int,
    val sort: SortX,
    val totalElements: Int,
    val totalPages: Int
)

data class Content(
    val batchNo: String,
    val creationTime: String,
    val expirationDate: String,
    val id: String,
    val operatorId: String,
    val passWord: String,
    val redeemableDays: Int,
    val remark: String,
    val salesMan: String,
    val state: String
)

data class Pageable(
    val offset: Int,
    val pageNumber: Int,
    val pageSize: Int,
    val paged: Boolean,
    val sort: SortX,
    val unpaged: Boolean
)

data class SortX(
    val empty: Boolean,
    val sorted: Boolean,
    val unsorted: Boolean
)

//生成注册码的post请求json格式数据
data class software_code(
    val cn: Boolean,
    val companyId: Any?,
    val expirationDate: String,
    val genNum: Int,
    val internationCode: Boolean,
    val isDomestic: Int,
    val isExport: Boolean,
    val redeemableDays: Int,
    val remark: String,
    val salesMan: String
)

//生成注册码后返回信息
data class code_back(
    val code: Int,
    val message: String
)

//设备录入，查询经销商的返回信息
data class equipment_input_search_back(
    val code: Int,
    val `data`: List<Data_inputback>,
    val message: String,
    val type: Int
)

data class Data_inputback(
    val companyId: Int,
    val companyName: String,
    val managerId: Int,
    val tel: String
)

//录入设备消息返回
data class equipment_input_callback(
    val code: Int,
    val `data`: Any,
    val message: String,
    val type: Int
)

//删除设备返回值
data class delete_equipment_callback(
    val code: Int,
    val `data`: Any,
    val message: String,
    val type: Int
)

//出库返回值
data class out_back(
    val code: Int,
    val `data`: Data_outback,
    val message: String
)

data class Data_outback(
    val tempRegDeadline: String
)

//永久码下发返回值
data class permanent_code_back(
    val code: Int,
    val `data`: Data_permanent,
    val message: String
)

data class Data_permanent(
    val tempRegDeadline: String
)

//功能授权发出信息
data class function_information(
    val HostNetDeadlineTimeEditable: Boolean,
    val NtripDeadlineTimeEditable: Boolean,
    val companyId: String,
    val initHostNetRegDeadline: String?,
    val initNtripRegDeadline: String?,
    val isHostNet: Boolean,
    val isNtrip: Boolean,
    val isSelectall: Boolean,
    val keyword: String?,
    val lastUpdateTime: String?,
    val offHostNetTime: String?,
    val offNtripTime: String?,
    val snList: List<String>,
    val trialStatus: Any?,
    val type: Int
)

//功能授权返回对象
data class function_given_back(
    val code: Int,
    val `data`: Data_functionback,
    val message: String
)

class Data_functionback




