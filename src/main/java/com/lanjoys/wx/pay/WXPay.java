package com.lanjoys.wx.pay;

import com.google.common.collect.Maps;
import com.lanjoys.wx.WxProperty;
import com.lanjoys.wx.pay.WXPayConstants.SignType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Data
public class WXPay {

    @Autowired
    private WxProperty config;
    @Autowired
    private WXPayRequest wxPayRequest;

    public static void main(String[] args) throws Exception {
        WxProperty wxProperty = new WxProperty();
        wxProperty.setAppId("wxf7f3b84937b92a1f");
        wxProperty.setAppSecret("94b021b3b867d84d47ffe24094358bde");
        WxProperty.PayProperty payProperty = new WxProperty.PayProperty();
        payProperty.setMchId("1515266711");
        payProperty.setKey("dfglk13hftfT78YghTYU6399KJVFcghr");
        payProperty.setNotifyUrl("http://baidu.com");
        wxProperty.setPay(payProperty);

        WXPayRequest wxPayRequest = new WXPayRequest();
        wxPayRequest.setConfig(wxProperty);
        WXPay wxPay = new WXPay();
        wxPay.setConfig(wxProperty);
        wxPay.setWxPayRequest(wxPayRequest);


        Map<String, String> data = new HashMap<>();
        data.put("body", "诚信金缴纳");
        data.put("out_trade_no", "123456789");
        data.put("total_fee", "11");
        data.put("spbill_create_ip", "101.231.118.210");
        data.put("trade_type", "NATIVE");
        data.put("product_id", "1");
        data.put("attach", "EARNEST_MONEY|");

        for (int i = 0; i < 100; i++) {
            Map<String, String> resp = wxPay.unifiedOrder(data);
            System.out.println(resp);
        }


    }

    /**
     * 向 Map 中添加 appid、mch_id、nonce_str、sign_type、sign <br>
     * 该函数适用于商户适用于统一下单等接口，不适用于红包、代金券接口
     *
     * @param reqData
     * @return
     * @throws Exception
     */
    public Map<String, String> fillRequestData(Map<String, String> reqData) throws Exception {
        reqData.put("appid", config.getAppId());
        reqData.put("mch_id", config.getPay().getMchId());
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("sign_type", WXPayConstants.HMACSHA256);
        reqData.put("sign", WXPayUtil.generateSignature(reqData, config.getPay().getKey(), SignType.HMACSHA256));
        return reqData;
    }

    public Map<String, String> createWxJSAPIPayString(String prepayId) throws Exception {
        Map<String, String> sp = Maps.newHashMap();
        sp.put("appId", config.getAppId());
        sp.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        sp.put("nonceStr", WXPayUtil.generateNonceStr());
        sp.put("package", "prepay_id=" + prepayId);
        sp.put("signType", WXPayConstants.HMACSHA256);
        sp.put("paySign", WXPayUtil.generateSignature(sp, config.getPay().getKey(), WXPayConstants.SignType.HMACSHA256));
        return sp;
    }

    /**
     * 判断xml数据的sign是否有效，必须包含sign字段，否则返回false。
     *
     * @param reqData 向wxpay post的请求数据
     * @return 签名是否有效
     * @throws Exception
     */
    public boolean isResponseSignatureValid(Map<String, String> reqData) throws Exception {
        // 返回数据的签名方式和请求中给定的签名方式是一致的
        return WXPayUtil.isSignatureValid(reqData, this.config.getPay().getKey(), SignType.HMACSHA256);
    }

    /**
     * 判断支付结果通知中的sign是否有效
     *
     * @param reqData 向wxpay post的请求数据
     * @return 签名是否有效
     * @throws Exception
     */
    public boolean isPayResultNotifySignatureValid(Map<String, String> reqData) throws Exception {
        return WXPayUtil.isSignatureValid(reqData, this.config.getPay().getKey(), SignType.HMACSHA256);
    }

    /**
     * 不需要证书的请求
     *
     * @param urlSuffix        String
     * @param reqData          向wxpay post的请求数据
     * @param connectTimeoutMs 超时时间，单位是毫秒
     * @param readTimeoutMs    超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public String requestWithoutCert(String urlSuffix, Map<String, String> reqData,
                                     int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String reqBody = WXPayUtil.mapToXml(reqData);
        log.info("请求参数:{}", reqBody);
        return this.wxPayRequest.requestWithoutCert(urlSuffix, reqBody, connectTimeoutMs, readTimeoutMs);
    }

    /**
     * 需要证书的请求
     *
     * @param urlSuffix        String
     * @param reqData          向wxpay post的请求数据  Map
     * @param connectTimeoutMs 超时时间，单位是毫秒
     * @param readTimeoutMs    超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public String requestWithCert(String urlSuffix, Map<String, String> reqData,
                                  int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String reqBody = WXPayUtil.mapToXml(reqData);
        log.info("请求参数:{}", reqBody);
        return this.wxPayRequest.requestWithCert(urlSuffix, reqBody, connectTimeoutMs, readTimeoutMs);
    }

    /**
     * 处理 HTTPS API返回数据，转换成Map对象。return_code为SUCCESS时，验证签名。
     *
     * @param xmlStr API返回的XML格式数据
     * @return Map类型数据
     * @throws Exception
     */
    public Map<String, String> processResponseXml(String xmlStr) throws Exception {
        String RETURN_CODE = "return_code";
        String return_code;
        Map<String, String> respData = WXPayUtil.xmlToMap(xmlStr);
        if (!respData.containsKey(RETURN_CODE))
            throw new Exception(String.format("No `return_code` in XML: %s", xmlStr));

        return_code = respData.get(RETURN_CODE);

        if (return_code.equals(WXPayConstants.FAIL)) return respData;

        if (return_code.equals(WXPayConstants.SUCCESS)) {
            if (this.isResponseSignatureValid(respData)) return respData;

            throw new Exception(String.format("Invalid sign value in XML: %s", xmlStr));
        }

        throw new Exception(String.format("return_code value %s is invalid in XML: %s", return_code, xmlStr));
    }

    /**
     * 作用：统一下单<br>
     * 场景：公共号支付、扫码支付、APP支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> unifiedOrder(Map<String, String> reqData) throws Exception {
        return this.unifiedOrder(reqData, config.getPay().getConnectTimeoutMs(), this.config.getPay().getReadTimeoutMs());
    }

    /**
     * 作用：统一下单<br>
     * 场景：公共号支付、扫码支付、APP支付
     *
     * @param reqData          向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs    读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> unifiedOrder(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {

        if (this.config.getPay().getNotifyUrl() != null) {
            reqData.put("notify_url", this.config.getPay().getNotifyUrl());
        }
        String respXml = this.requestWithoutCert(WXPayConstants.UNIFIEDORDER_URL_SUFFIX, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }

    /**
     * 作用：查询订单<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> orderQuery(Map<String, String> reqData) throws Exception {
        return this.orderQuery(reqData, config.getPay().getConnectTimeoutMs(), this.config.getPay().getReadTimeoutMs());
    }

    /**
     * 作用：查询订单<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     *
     * @param reqData          向wxpay post的请求数据 int
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs    读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> orderQuery(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.ORDERQUERY_URL_SUFFIX, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }

    /**
     * 作用：撤销订单<br>
     * 场景：刷卡支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> reverse(Map<String, String> reqData) throws Exception {
        return this.reverse(reqData, config.getPay().getConnectTimeoutMs(), this.config.getPay().getReadTimeoutMs());
    }

    /**
     * 作用：撤销订单<br>
     * 场景：刷卡支付<br>
     * 其他：需要证书
     *
     * @param reqData          向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs    读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> reverse(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String respXml = this.requestWithCert(WXPayConstants.REVERSE_URL_SUFFIX, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }

    /**
     * 作用：关闭订单<br>
     * 场景：公共号支付、扫码支付、APP支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> closeOrder(Map<String, String> reqData) throws Exception {
        return this.closeOrder(reqData, config.getPay().getConnectTimeoutMs(), this.config.getPay().getReadTimeoutMs());
    }

    /**
     * 作用：关闭订单<br>
     * 场景：公共号支付、扫码支付、APP支付
     *
     * @param reqData          向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs    读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> closeOrder(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.CLOSEORDER_URL_SUFFIX, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }

    /**
     * 作用：申请退款<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refund(Map<String, String> reqData) throws Exception {
        return this.refund(reqData, this.config.getPay().getConnectTimeoutMs(), this.config.getPay().getReadTimeoutMs());
    }

    /**
     * 作用：申请退款<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付<br>
     * 其他：需要证书
     *
     * @param reqData          向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs    读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refund(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String respXml = this.requestWithCert(WXPayConstants.REFUND_URL_SUFFIX, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }

    /**
     * 作用：退款查询<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refundQuery(Map<String, String> reqData) throws Exception {
        return this.refundQuery(reqData, this.config.getPay().getConnectTimeoutMs(), this.config.getPay().getReadTimeoutMs());
    }

    /**
     * 作用：退款查询<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     *
     * @param reqData          向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs    读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refundQuery(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.REFUNDQUERY_URL_SUFFIX, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }

    /**
     * 作用：对账单下载（成功时返回对账单数据，失败时返回XML格式数据）<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> downloadBill(Map<String, String> reqData) throws Exception {
        return this.downloadBill(reqData, this.config.getPay().getConnectTimeoutMs(), this.config.getPay().getReadTimeoutMs());
    }

    /**
     * 作用：对账单下载<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付<br>
     * 其他：无论是否成功都返回Map。若成功，返回的Map中含有return_code、return_msg、data，
     * 其中return_code为`SUCCESS`，data为对账单数据。
     *
     * @param reqData          向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs    读超时时间，单位是毫秒
     * @return 经过封装的API返回数据
     * @throws Exception
     */
    public Map<String, String> downloadBill(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String respStr = this.requestWithoutCert(WXPayConstants.DOWNLOADBILL_URL_SUFFIX, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs).trim();
        Map<String, String> ret;
        // 出现错误，返回XML数据
        if (respStr.indexOf("<") == 0) {
            ret = WXPayUtil.xmlToMap(respStr);
        } else {
            // 正常返回csv数据
            ret = new HashMap<>();
            ret.put("return_code", WXPayConstants.SUCCESS);
            ret.put("return_msg", "ok");
            ret.put("data", respStr);
        }
        return ret;
    }

    /**
     * 作用：交易保障<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> report(Map<String, String> reqData) throws Exception {
        return this.report(reqData, this.config.getPay().getConnectTimeoutMs(), this.config.getPay().getReadTimeoutMs());
    }

    /**
     * 作用：交易保障<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     *
     * @param reqData          向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs    读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> report(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.REPORT_URL_SUFFIX, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return WXPayUtil.xmlToMap(respXml);
    }

    /**
     * 作用：转换短链接<br>
     * 场景：刷卡支付、扫码支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> shortUrl(Map<String, String> reqData) throws Exception {
        return this.shortUrl(reqData, this.config.getPay().getConnectTimeoutMs(), this.config.getPay().getReadTimeoutMs());
    }

    /**
     * 作用：转换短链接<br>
     * 场景：刷卡支付、扫码支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> shortUrl(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.SHORTURL_URL_SUFFIX, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }

    /**
     * 作用：授权码查询OPENID接口<br>
     * 场景：刷卡支付
     *
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> authCodeToOpenid(Map<String, String> reqData) throws Exception {
        return this.authCodeToOpenid(reqData, this.config.getPay().getConnectTimeoutMs(), this.config.getPay().getReadTimeoutMs());
    }

    /**
     * 作用：授权码查询OPENID接口<br>
     * 场景：刷卡支付
     *
     * @param reqData          向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs    读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> authCodeToOpenid(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.AUTHCODETOOPENID_URL_SUFFIX, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }
}
