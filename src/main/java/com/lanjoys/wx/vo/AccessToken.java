package com.lanjoys.wx.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccessToken extends Errorable {
    private String access_token;
    private long expires_in;
    private String refresh_token;
    private String openid;
    private String scope;
}
