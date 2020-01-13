package com.lanjoys.wx.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsSDKConfig {
    private String appId;
    private long timestamp;
    private String nonceStr;
    private String signature;

}
