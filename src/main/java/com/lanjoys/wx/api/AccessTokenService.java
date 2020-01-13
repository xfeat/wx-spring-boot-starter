package com.lanjoys.wx.api;


import com.lanjoys.wx.WxProperty;
import com.lanjoys.wx.ex.AquireBaseAccessTokenException;
import com.lanjoys.wx.vo.BaseAccessToken;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AccessTokenService {
    private static final long REFRESH_FLOOR_SECOND = 300;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    WxProperty wxProperty;
    @Autowired
    StringRedisTemplate redisTemplate;

    public String getAccessToken() throws AquireBaseAccessTokenException {
        String accessTokenKey = WxProperty.PREFIX + ":access_token";
        String existAccessToken = redisTemplate.opsForValue().get(accessTokenKey);
        if (existAccessToken != null && redisTemplate.getExpire(accessTokenKey) > REFRESH_FLOOR_SECOND) {
            return existAccessToken;
        }

        RLock lock = redissonClient.getLock("_lock:" + accessTokenKey);
        lock.lock();
        try {
            String accessTokenAgain = redisTemplate.opsForValue().get(accessTokenKey);
            if (accessTokenAgain != null && redisTemplate.getExpire(accessTokenKey) > REFRESH_FLOOR_SECOND)
                return accessTokenAgain;

            RestTemplate restTemplate = new RestTemplate();
            BaseAccessToken baseAccessToken = restTemplate.getForObject(
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={1}&secret={2}",
                    BaseAccessToken.class,
                    wxProperty.getAppId(),
                    wxProperty.getAppSecret()
            );

            if (baseAccessToken == null) {
                log.error("获取access_token失败");
                throw new AquireBaseAccessTokenException("获取access_token失败");
            }
            if (baseAccessToken.hasError()) {
                log.error("获取access_token失败,code:{},message:{}", baseAccessToken.getErrcode(), baseAccessToken.getErrmsg());
                throw new AquireBaseAccessTokenException("获取access_token失败");
            }

            redisTemplate.opsForValue().set(accessTokenKey, baseAccessToken.getAccess_token(), baseAccessToken.getExpires_in(), TimeUnit.SECONDS);
            return baseAccessToken.getAccess_token();
        } finally {
            lock.unlock();
        }
    }
}
