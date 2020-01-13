package com.lanjoys.wx.api;

import com.google.common.hash.Hashing;
import com.lanjoys.wx.WxAutoConfiguration;
import com.lanjoys.wx.WxProperty;

import java.util.Arrays;

import static com.google.common.base.Charsets.UTF_8;

public class WxSignatureService {
    public static boolean isSignatureRight(String signature, String timestamp, String nonce) {
        String[] arr = new String[]{WxAutoConfiguration.getBean(WxProperty.class).getToken(), timestamp, nonce};
        Arrays.sort(arr);
        return Hashing.sha1().hashString(String.join("", arr), UTF_8).toString().equals(signature);
    }
}
