package com.example.appauto

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import kotlin.concurrent.thread


public interface loginService{
    @GET("gateway/auth/oauth/token")
    fun login(@Query("username") username:String, @Query("password") password:String, @Query("grant_type") grant_type:String, @Query("client_id") client_id:String, @Query("client_secret") client_secret:String, @Query("scope") scope:String, @Query("isAgree") isAgree:String):Call<logindata>
}

public interface getCurrentUser{
    @GET("gateway/auth/user/getCurrentUser")
    fun getCurrentuser():Call<Currentuser>
}

public interface getReceiver{
    @FormUrlEncoded
    @POST("gateway/DiffPlat/device/list")
    fun getreceiver(@Field("currPage") currPage:Int, @Field("pageSize") pageSize:Int, @Field("conditions") conditions:String):Call<receiver>
}

public interface register_sendinto{
    @POST("gateway/dr/deviceRegInfo/list")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua-mobile: ?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Content-Type: application/json;charset=UTF-8",
        "Origin: https://cloud.sinognss.com",
        "Sec-Fetch-Site: same-origin",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
    )
    fun register_info(@Body registerSend: register_send, @Header("Authorization") auth : String):Call<registerinfo>
}

public interface register_time_extend_put{
    @PUT("gateway/dr/deviceRegInfo/updateRegInfoById")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Content-Type: application/json;charset=UTF-8",
        "Origin: https://cloud.sinognss.com",
        "Sec-Fetch-Site: same-origin",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
    )
    fun register_time(@Body registerSend: register_time_extend, @Header("Authorization") auth : String):Call<extend_time_notice>
}

public interface register_domestic{
    @POST("gateway/dr/deviceRegInfo/generateRegCodeAndPush")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Content-Type: application/json;charset=UTF-8",
        "Origin: https://cloud.sinognss.com",
        "Sec-Fetch-Site: same-origin",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
    )
    fun register_domestic_send(@Body domestcode: domestcode, @Header("Authorization") auth : String):Call<domesticinfo>
}

public interface finalreceiverinfo {
    @GET("gateway/dr/deviceRegInfo/getRegInfoBySn")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
    )
    fun get_final_receiverinfo(@Query("sn") sn:String, @Header("Authorization") auth : String):Call<finalinfo>
}

public interface getid {
    @FormUrlEncoded
    @POST("gateway/DiffPlat/device/list")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
        "Content-Type: application/x-www-form-urlencoded;charset=UTF-8",
    )
    fun get_luowang_id(@Field("currPage") currPage:Int, @Field("pageSize") pageSize:Int, @Field("conditions") conditions:String, @Header("Authorization") auth : String) : Call<receiver>
}

public interface idinfo{
    @FormUrlEncoded
    @POST("gateway/DiffPlat/device/detail")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
        "Content-Type: application/x-www-form-urlencoded;charset=UTF-8",
    )
    fun get_idinfo(@Field("id") id:Int, @Header("Authorization") auth : String) : Call<id_info>
}

public interface direct_more{
    @FormUrlEncoded
    @POST("gateway/DiffPlat/device/span/upadte")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
        "Content-Type: application/x-www-form-urlencoded;charset=UTF-8",
    )
    fun direct_more_change(@Field("id")id:Int,@Field("isSpan")isSpan:Boolean,@Header("Authorization") auth : String) : Call<direct_more_message>
}

public interface basestation_change{
    @FormUrlEncoded
    @POST("gateway/DiffPlat/device/accountType/update")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
        "Content-Type: application/x-www-form-urlencoded;charset=UTF-8",
    )
    fun basestation_change(@Field("sn")SN : String, @Field("accountType")accountType:String, @Header("Authorization") auth : String) : Call<direct_more_message>
}

public interface software_register{
    @POST("gateway/mr/redemption/paged")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
        "Content-Type: application/json;charset=UTF-8",
        )
    fun software_register(@Body software_post:software_post, @Header("Authorization") auth : String) : Call<software_post_return>
}

public interface software_code_interface{
    @POST("gateway/mr/redemption/batchGen")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Microsoft Edge\";v=\"132\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "userId: ",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Origin: https://cloud.sinognss.com",
        "Referer: https://cloud.sinognss.com/meaReg/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
        "Content-Type: application/json;charset=UTF-8",
    )
    fun software_code_fx(@Body software_code:software_code, @Header("Authorization") auth : String) : Call<code_back>
}

//设备录入接口---查询经销商
public interface equipment_input_search{
    @FormUrlEncoded
    @POST("gateway/DiffPlat/device/declarCompany")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Microsoft Edge\";v=\"132\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "userId: ",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Origin: https://cloud.sinognss.com",
        "Referer: https://cloud.sinognss.com/cm/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
        "Content-Type: application/x-www-form-urlencoded;charset=UTF-8",
    )
    fun equipment_input_search(@Field ("keyword")keyword:String, @Header("Authorization") auth : String) : Call<equipment_input_search_back>
}

//设备录入接口
public interface equipment_input_retrofit{
    @FormUrlEncoded
    @POST("gateway/DiffPlat/device/save")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Microsoft Edge\";v=\"132\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "userId: ",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Origin: https://cloud.sinognss.com",
        "Referer: https://cloud.sinognss.com/cm/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
        "Content-Type: application/x-www-form-urlencoded;charset=UTF-8",
    )
    fun equipment_input(@Field("sn")SN: String, @Field("declarCompanyId")CompanyId:String?, @Field("decalrId")ManagerId:String?, @Field("remark")remark:String, @Field("active")active:Boolean?, @Field("duration")duration:String, @Header("Authorization") auth : String) : Call<equipment_input_callback>
}

//删除设备接口
public interface equipment_delete{
    @GET("gateway/DiffPlat/device/delete")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Microsoft Edge\";v=\"132\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "userId: ",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Origin: https://cloud.sinognss.com",
        "Referer: https://cloud.sinognss.com/cm/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
    )
    fun delete_equipment(@Query("sn")SN: String, @Header("Authorization") auth : String) : Call<delete_equipment_callback>
}

//出库接口
public interface out{
    @PUT("gateway/dr/deviceRegInfo/updateTrialStatus")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Not(A:Brand\";v=\"99\", \"Microsoft Edge\";v=\"133\", \"Chromium\";v=\"133\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "userId: ",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Origin: https://cloud.sinognss.com",
        "Referer: https://cloud.sinognss.com/cm/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
    )
    fun out(@Query("sn")SN : String, @Header("Authorization") auth : String) : Call<out_back>
}

//永久码下发接口
public interface permanent_code{
    @GET("gateway/dr/deviceRegInfo/permanent/push")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Microsoft Edge\";v=\"132\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "userId: ",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Origin: https://cloud.sinognss.com",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br, zstd",
        "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
    )
    fun permanent_code(@Query("sn")SN : String, @Header("Authorization") auth : String) : Call<permanent_code_back>
}

public interface function_given{
    @PUT("gateway/dr/deviceRegSet/batchUpdate")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Microsoft Edge\";v=\"132\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "Content-Type: application/json;charset=UTF-8",
        "userId: ",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Origin: https://cloud.sinognss.com",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br",
        "Accept-Language: zh-CN,zh-Hans;q=0.9",
    )
    fun function_given(@Body function_information: function_information , @Header("Authorization") auth : String) : Call<function_given_back>
}

public interface trial{
    @PUT("/gateway/dr/deviceRegInfo/trialOne")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Microsoft Edge\";v=\"132\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "Content-Type: application/json;charset=UTF-8",
        "userId: ",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Origin: https://cloud.sinognss.com",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br",
        "Accept-Language: zh-CN,zh-Hans;q=0.9",
    )
    fun trial_given(@Query("sn")sn: String, @Query("day")day : Int, @Header("Authorization")auth : String) : Call<Trial_back>
}

public interface tiral_updata{
    @PUT("/gateway/dr/deviceRegInfo/updateTrialDay")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Microsoft Edge\";v=\"132\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "Content-Type: application/json;charset=UTF-8",
        "userId: ",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Origin: https://cloud.sinognss.com",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br",
        "Accept-Language: zh-CN,zh-Hans;q=0.9",
    )
    fun trial_updata(@Query("sn")sn: String, @Query("day")day : Int, @Header("Authorization")auth : String) : Call<Trial_back>
}

public interface tempRegDeadline_f{
    @PUT("/gateway/dr/deviceRegInfo/updateRegInfoById")
    @Headers(
        "Host: cloud.sinognss.com",
        "Connection: keep-alive",
        "sec-ch-ua-platform: \"Windows\"",
        "sec-ch-ua: \"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Microsoft Edge\";v=\"132\"",
        "sec-ch-ua-mobile:?0",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        "Accept: application/json, text/plain, */*",
        "Sec-Fetch-Site: same-origin",
        "Content-Type: application/json;charset=UTF-8",
        "userId: ",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Dest: empty",
        "Origin: https://cloud.sinognss.com",
        "Referer: https://cloud.sinognss.com/drFrontEnd/",
        "Accept-Encoding: gzip, deflate, br",
        "Accept-Language: zh-CN,zh-Hans;q=0.9",
    )
    fun tempRegDeadline_send(@Body tempRegDeadlined: tempRegDeadline_send, @Header("Authorization")auth : String) : Call<Trial_back>
}