package com.lanjoys.wx.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JsApiTicket extends Errorable {
    private String ticket;
    private int expires_in;
}
