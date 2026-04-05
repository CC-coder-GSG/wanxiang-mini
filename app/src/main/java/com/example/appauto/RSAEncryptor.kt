package com.example.appauto

import android.util.Base64
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object RSAEncryptor {

    // sinognss.cn 前端 JS 硬编码的 RSA-2048 公钥（SubjectPublicKeyInfo / PKCS#8 格式）
    private const val PUBLIC_KEY =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjtu1w4s79b6tN8NmYxHh" +
        "0oNmW+CmAh1MpSJaZbkI2Un/3TMqQx3m2fvHnqYPRCNAILn4oueWfYNJjPtnKMH" +
        "2XXbUWq/li1/uqwr2zUkey1pWf7UlDKKP5gUMdCmxOFGHcG98BZIF46AsLlEvOr" +
        "FDTy3pnDWv6h7thsLi7CmqlFTye0XvesTsxwELvmV8BShEo3PXNPpFbnoT9R7FT" +
        "si7cGRE8uISyavCzImRBa4lVqdK3Z33V4/CRjbtDL2W1hxAJm25coODUbI1AOA2" +
        "LMjs8EN7zKUfRNA+mCAtSIlmG5KvIrlAYcBEBlFcdbZmsFVpvCCZoeXrETaQ9ZEW" +
        "fQbMMQIDAQAB"

    fun encrypt(plainText: String): String {
        val keyBytes = Base64.decode(PUBLIC_KEY, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }
}
