package me.link.bootstrap.core.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

/**
 * 幂等 Token 签名工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdempotentSignUtils {

    /**
     * 生成签名
     *
     * @param data 待签名数据 (uuid + tenantId)
     * @param salt 密钥
     * @return 16进制签名字符串
     */
    public static String sign(String data, String salt) {
        HMac hMac = SecureUtil.hmacSha256(salt.getBytes(StandardCharsets.UTF_8));
        return hMac.digestHex(data);
    }

    /**
     * 验证签名
     *
     * @param data      原始数据
     * @param salt      密钥
     * @param signature 待验证的签名
     * @return 是否通过
     */
    public static boolean verify(String data, String salt, String signature) {
        String expect = sign(data, salt);
        return expect.equalsIgnoreCase(signature);
    }
}