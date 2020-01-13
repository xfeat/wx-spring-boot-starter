package com.lanjoys.wx;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.InputStream;

@Data
@ConfigurationProperties(prefix = WxProperty.PREFIX)
public class WxProperty {
    public static final String PREFIX = "wx";
    private String appId;
    private String appSecret;
    private String token;
    private PayProperty pay;

    @Data
    public static class PayProperty {
        private String domain = "api.mch.weixin.qq.com";
        private String mchId;
        private String key;
        private String notifyUrl;
        private int connectTimeoutMs = 6 * 1000;
        private int readTimeoutMs = 8 * 1000;

        public InputStream getCertStream() {
            return WxProperty.class.getResourceAsStream("/apiclient_cert.p12");
        }
    }
}
