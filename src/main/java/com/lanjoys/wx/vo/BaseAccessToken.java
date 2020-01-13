package com.lanjoys.wx.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BaseAccessToken extends Errorable {
    private String access_token;
    private long expires_in;
}
