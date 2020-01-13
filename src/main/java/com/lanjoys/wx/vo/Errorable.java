package com.lanjoys.wx.vo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Errorable {
    private String errcode;
    private String errmsg;

    public boolean hasError() {
        log.info("errcode:{},errmsg:{}", errcode, errmsg);
        return false;
    }
}
