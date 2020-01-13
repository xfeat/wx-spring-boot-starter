package com.lanjoys.wx.api;

import com.alibaba.fastjson.JSON;
import com.lanjoys.wx.ex.AquireBaseAccessTokenException;
import com.lanjoys.wx.ex.AquireUserInfoException;
import com.lanjoys.wx.util.RestTemplateHelp;
import com.lanjoys.wx.vo.WxUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserManagerService {

    @Autowired
    AccessTokenService accessTokenService;

    public WxUser getUser(String openId) throws AquireBaseAccessTokenException, AquireUserInfoException {
        String userInfoRet;
        try {
            userInfoRet = RestTemplateHelp.get().getForObject(
                    "https://api.weixin.qq.com/cgi-bin/user/info?access_token={1}&openid={2}&lang=zh_CN",
                    String.class,
                    accessTokenService.getAccessToken(),
                    openId
            );
        } catch (AquireBaseAccessTokenException e) {
            log.error("获取accessToken失败", e);
            throw e;
        }


        WxUser userInfo = JSON.parseObject(userInfoRet, WxUser.class);

        if (userInfo == null) {
            log.error("获取userInfo失败");
            throw new AquireUserInfoException("获取WxUser失败");
        }
        if (userInfo.hasError()) {
            log.error("获取userInfo失败,code:{},message:{}", userInfo.getErrcode(), userInfo.getErrmsg());
            throw new AquireUserInfoException("获取WxUser失败");
        }

        log.info("微信用户信息:{}", userInfo);
        return userInfo;
    }
}
