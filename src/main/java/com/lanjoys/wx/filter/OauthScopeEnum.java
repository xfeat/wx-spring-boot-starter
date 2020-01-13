package com.lanjoys.wx.filter;

public enum OauthScopeEnum {
    snsapi_base, snsapi_userinfo;

    public boolean equals(String name) {
        return this.name().equals(name);
    }
}
