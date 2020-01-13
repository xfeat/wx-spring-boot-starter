package com.lanjoys.wx.filter;

import com.lanjoys.wx.WxProperty;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@Slf4j
@AllArgsConstructor
public abstract class AbstractOauthRedirectFilter extends OncePerRequestFilter {
    protected String filter;
    protected WxProperty wxProperty;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("根据需要重定向到oauth连接");

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String prefix = request.getRequestURL().toString().replace(request.getRequestURI(), "");
        String queryString = StringUtils.isNotBlank(request.getQueryString()) ? "?" + request.getQueryString() : "";
        String nextUrl = prefix + request.getRequestURI().replaceFirst(filter + "/", "/") + queryString;

        String userAgent = request.getHeader("user-agent");
        if (userAgent != null && userAgent.contains("MicroMessenger")) {//微信浏览器
            nextUrl = buildWxOauthURL(prefix + request.getRequestURI().replaceFirst(filter + "/", "/auto-login/") + queryString);
        }

        response.sendRedirect(
                nextUrl
        );

    }

    @SneakyThrows
    private String buildWxOauthURL(String redirectURI) {
        return "https://open.weixin.qq.com/connect/oauth2/authorize" +
                "?appid=" + wxProperty.getAppId() +
                "&redirect_uri=" + URLEncoder.encode(redirectURI, "utf-8") +
                "&response_type=code&scope=" + getScope().name() +
                "&state=" + getScope().name() +
                "#wechat_redirect";
    }

    protected abstract OauthScopeEnum getScope();
}
