package com.lanjoys.wx.api;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.lanjoys.wx.ex.AquireBaseAccessTokenException;
import com.lanjoys.wx.ex.AquireShortUrlException;
import com.lanjoys.wx.util.RestTemplateHelp;
import com.lanjoys.wx.vo.ShortUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ShortUrlService {

    @Autowired
    AccessTokenService accessTokenService;

    public String s(String url) throws AquireBaseAccessTokenException, AquireShortUrlException {
        Map<String,String> params = Maps.newHashMap();
        params.put("action", "long2short");
        params.put("long_url", url);
        String ret;
        try {
            ret = RestTemplateHelp.get().postForObject(
                    "https://api.weixin.qq.com/cgi-bin/shorturl?access_token={1}",
                    JSON.toJSONString(params),
                    String.class,
                    accessTokenService.getAccessToken()
            );
        } catch (AquireBaseAccessTokenException e) {
            log.error("获取accessToken失败", e);
            throw e;
        }

        ShortUrl shortUrl = JSON.parseObject(ret, ShortUrl.class);

        if (shortUrl == null) {
            log.error("获取短链失败");
            throw new AquireShortUrlException("获取短链失败");
        }

        if (shortUrl.hasError()) {
            log.error("获取短链失败,code:{},message:{}", shortUrl.getErrcode(), shortUrl.getErrmsg());
            throw new AquireShortUrlException("获取短链失败");
        }

        log.info("短链信息:{}", shortUrl);
        return shortUrl.getShort_url();


    }

}
