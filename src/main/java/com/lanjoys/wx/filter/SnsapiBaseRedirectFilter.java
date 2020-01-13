package com.lanjoys.wx.filter;

import com.lanjoys.wx.WxProperty;

public class SnsapiBaseRedirectFilter extends AbstractOauthRedirectFilter {
    public SnsapiBaseRedirectFilter(String filter, WxProperty wxProperty) {
        super(filter, wxProperty);
    }

    @Override
    protected OauthScopeEnum getScope() {
        return OauthScopeEnum.snsapi_base;
    }
}
