package com.lanjoys.wx.api;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.lanjoys.wx.WxProperty;
import com.lanjoys.wx.ex.AquireBaseAccessTokenException;
import com.lanjoys.wx.ex.AquireJSApiTickeException;
import com.lanjoys.wx.util.RestTemplateHelp;
import com.lanjoys.wx.vo.JsApiTicket;
import com.lanjoys.wx.vo.JsSDKConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.google.common.base.Charsets.UTF_8;

@Slf4j
@Service
public class JsSDKService {
    @Autowired
    WxProperty wxProperty;

    @Autowired
    AccessTokenService accessTokenService;

    public JsSDKConfig getConfig(String url) throws AquireBaseAccessTokenException, AquireJSApiTickeException {
        String accessTokenRet = RestTemplateHelp.get().getForObject(
                "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={1}&type=jsapi",
                String.class,
                accessTokenService.getAccessToken()
        );

        JsApiTicket apiTicket = JSON.parseObject(accessTokenRet, JsApiTicket.class);
        if (apiTicket == null) {
            log.error("获取apiTicket失败");
            throw new AquireJSApiTickeException("获取apiTicket失败");
        }
        if (apiTicket.hasError()) {
            log.error("获取apiTicket失败,code:{},message:{}", apiTicket.getErrcode(), apiTicket.getErrmsg());
            throw new AquireJSApiTickeException("获取apiTicket失败");
        }

        Map<String, Object> params = Maps.newTreeMap();
        params.put("noncestr", RandomStringUtils.random(10, true, true));
        params.put("jsapi_ticket", apiTicket.getTicket());
        params.put("timestamp", System.currentTimeMillis() / 1000);
        params.put("url", url);

        String paramStr = "";
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!"".equals(paramStr)) paramStr += "&";
            paramStr += (entry.getKey() + "=" + entry.getValue());
        }

        log.info("param:{}", paramStr);


        return new JsSDKConfig(
                wxProperty.getAppId(),
                (long) params.get("timestamp"),
                (String) params.get("noncestr"),
                Hashing.sha1().hashString(paramStr, UTF_8).toString()
        );
    }
}
