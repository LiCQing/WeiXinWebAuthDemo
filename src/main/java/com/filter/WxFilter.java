package com.filter;

import com.controller.WxController;
import com.model.User;
import com.service.UserService;
import com.util.OpenIdResult;
import com.util.WeixinUserInfo;
import com.util.WeixinUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;

/**
 * 微信过滤器
 * 自动获取授权
 */
@WebFilter(value = "/user/*")
public class WxFilter implements Filter {

    Logger log = LoggerFactory.getLogger(WxFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse hsResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();
        HttpSession session = httpRequest.getSession();
        //获取登陆用户标记
        User user = (User) session.getAttribute("C_USER");
        //获取授权标记
        OpenIdResult result = (OpenIdResult) session.getAttribute("C_OPENID");
        //不是手机端判断是否登陆
        try {
            if (!JudgeIsMoblie(httpRequest)) {
                if (user == null) {
                    String urlKey = WeixinUtil.setUrl(requestURI);
                    //跳到登陆页面
                    httpRequest.getRequestDispatcher("/toLogin/" + urlKey).forward(request, response);
                    return;
                }
                filterChain.doFilter(request, response);
                return;
            }


            //已经授权过 放行 未授权还要获取授权
            if (result != null) {
                //自动登陆
                if (user == null) {
                    User customer = new UserService().getUserByOpenId(result.getOpenid());
                    if (customer != null) {
                        session.setAttribute("C_USER", customer);
                    } else {
                        String urlKey = WeixinUtil.setUrl(requestURI);
                        //跳到登陆页面
                        httpRequest.getRequestDispatcher("/toLogin/" + urlKey).forward(request, response);
                        return;
                    }
                }
                filterChain.doFilter(request, response);
                return;
            }

            //获取访问地址
            if (requestURI.contains("getOpenInfo")) {//认证地址 放行
                filterChain.doFilter(request, response);
                return;
            }
            log.error("微信未授权");
            //调用微信授权,记录回调url
            String urlCode = WeixinUtil.setUrl(requestURI);
            hsResponse.sendRedirect(WeixinUtil.getAuthUrl(urlCode));
        } catch (Exception e) {
            e.printStackTrace();
            hsResponse.sendRedirect("/error/" + e.getMessage());
        }

    }


    //判断是否为手机浏览器
    public boolean JudgeIsMoblie(HttpServletRequest request) {
        boolean isMoblie = false;
        String[] mobileAgents = {"iphone", "android", "ipad", "phone", "mobile", "wap", "netfront", "java", "opera mobi",
                "opera mini", "ucweb", "windows ce", "symbian", "series", "webos", "sony", "blackberry", "dopod",
                "nokia", "samsung", "palmsource", "xda", "pieplus", "meizu", "midp", "cldc", "motorola", "foma",
                "docomo", "up.browser", "up.link", "blazer", "helio", "hosin", "huawei", "novarra", "coolpad", "webos",
                "techfaith", "palmsource", "alcatel", "amoi", "ktouch", "nexian", "ericsson", "philips", "sagem",
                "wellcom", "bunjalloo", "maui", "smartphone", "iemobile", "spice", "bird", "zte-", "longcos",
                "pantech", "gionee", "portalmmm", "jig browser", "hiptop", "benq", "haier", "^lct", "320x320",
                "240x320", "176x220", "w3c ", "acs-", "alav", "alca", "amoi", "audi", "avan", "benq", "bird", "blac",
                "blaz", "brew", "cell", "cldc", "cmd-", "dang", "doco", "eric", "hipt", "inno", "ipaq", "java", "jigs",
                "kddi", "keji", "leno", "lg-c", "lg-d", "lg-g", "lge-", "maui", "maxo", "midp", "mits", "mmef", "mobi",
                "mot-", "moto", "mwbp", "nec-", "newt", "noki", "oper", "palm", "pana", "pant", "phil", "play", "port",
                "prox", "qwap", "sage", "sams", "sany", "sch-", "sec-", "send", "seri", "sgh-", "shar", "sie-", "siem",
                "smal", "smar", "sony", "sph-", "symb", "t-mo", "teli", "tim-", "tosh", "tsm-", "upg1", "upsi", "vk-v",
                "voda", "wap-", "wapa", "wapi", "wapp", "wapr", "webc", "winw", "winw", "xda", "xda-",
                "Googlebot-Mobile"};
        if (request.getHeader("User-Agent") != null) {
            String agent = request.getHeader("User-Agent");
            for (String mobileAgent : mobileAgents) {
                if (agent.toLowerCase().indexOf(mobileAgent) >= 0 && agent.toLowerCase().indexOf("windows nt") <= 0 && agent.toLowerCase().indexOf("macintosh") <= 0) {
                    isMoblie = true;
                    break;
                }
            }
        }
        return isMoblie;
    }

}
