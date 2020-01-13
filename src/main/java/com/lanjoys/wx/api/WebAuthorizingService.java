package com.lanjoys.wx.api;

import com.alibaba.fastjson.JSON;
import com.lanjoys.wx.WxProperty;
import com.lanjoys.wx.ex.AquireOpenIdException;
import com.lanjoys.wx.ex.AquireUserInfoException;
import com.lanjoys.wx.util.RestTemplateHelp;
import com.lanjoys.wx.vo.AccessToken;
import com.lanjoys.wx.vo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebAuthorizingService {
    @Autowired
    WxProperty wxProperty;

    public String getOpenId(String code) throws AquireOpenIdException {
        return getAccessToken(code).getOpenid();
    }

    private AccessToken getAccessToken(String code) throws AquireOpenIdException {
        String accessTokenRet = RestTemplateHelp.get().getForObject(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid={1}&secret={2}&code={3}&grant_type=authorization_code",
                String.class,
                wxProperty.getAppId(),
                wxProperty.getAppSecret(),
                code
        );

        AccessToken accessToken = JSON.parseObject(accessTokenRet, AccessToken.class);

        if (accessToken == null) {
            log.error("获取openId失败");
            throw new AquireOpenIdException("获取openId失败");
        }
        if (accessToken.hasError()) {
            log.error("获取openId失败,code:{},message:{}", accessToken.getErrcode(), accessToken.getErrmsg());
            throw new AquireOpenIdException("获取openId失败");
        }

        return accessToken;
    }

    public UserInfo getUserInfo(String code) throws AquireOpenIdException, AquireUserInfoException {
        AccessToken accessToken = getAccessToken(code);

        String userInfoRet = RestTemplateHelp.get().getForObject(
                "https://api.weixin.qq.com/sns/userinfo?access_token={1}&openid={2}&lang=zh_CN",
                String.class,
                accessToken.getAccess_token(),
                accessToken.getOpenid()
        );

        UserInfo userInfo = JSON.parseObject(userInfoRet, UserInfo.class);

        if (userInfo == null) {
            log.error("获取userInfo失败");
            throw new AquireUserInfoException("获取access_token失败");
        }
        if (userInfo.hasError()) {
            log.error("获取userInfo失败,code:{},message:{}", userInfo.getErrcode(), userInfo.getErrmsg());
            throw new AquireUserInfoException("获取access_token失败");
        }

        return userInfo;
    }

}
