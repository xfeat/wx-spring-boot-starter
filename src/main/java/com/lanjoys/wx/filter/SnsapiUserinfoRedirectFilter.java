package com.lanjoys.wx.filter;

import com.lanjoys.wx.WxProperty;

public class SnsapiUserinfoRedirectFilter extends AbstractOauthRedirectFilter {
    public SnsapiUserinfoRedirectFilter(String filter, WxProperty wxProperty) {
        super(filter, wxProperty);
    }

    @Override
    protected OauthScopeEnum getScope() {
        return OauthScopeEnum.snsapi_userinfo;
    }
}
