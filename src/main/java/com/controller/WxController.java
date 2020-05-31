package com.controller;

import com.model.User;
import com.service.UserService;
import com.util.OpenIdResult;
import com.util.WeixinUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Controller
public class WxController {
   Logger log = LoggerFactory.getLogger(WxController.class);

   @Autowired
   UserService userService;

    private String TOKEN = "good";


    /**
     * 后他回调绑定验证接口
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @return
     */
    @GetMapping("/auth")
    @ResponseBody
    public String test(@RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("echostr") String echostr) {
        //排序
        String sortString = sort(TOKEN, timestamp, nonce);
        //加密
        String myString = sha1(sortString);
        //校验
        if (myString != null && myString != "" && myString.equals(signature)) {
            System.out.println("签名校验通过");
            //如果检验成功原样返回echostr，微信服务器接收到此输出，才会确认检验完成。
            return echostr;
        } else {
            System.out.println("签名校验失败");
            return "";
        }
    }

    public String sort(String token, String timestamp, String nonce) {
        String[] strArray = {token, timestamp, nonce};
        Arrays.sort(strArray);
        StringBuilder sb = new StringBuilder();
        for (String str : strArray) {
            sb.append(str);
        }

        return sb.toString();
    }

    public String sha1(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(str.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 微信网页授权获得微信详情
     * @param code
     * @param state
     * @param redirect 授权后跳转的视图 md5 key
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/getOpenInfo/{redirect}")
    public void getOpenInfo(Model model, @RequestParam("code") String code, @RequestParam("state") String state, @PathVariable("redirect") String redirect, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.error("授权成功回调");
        HttpSession session = request.getSession();
        User user = (User)session.getAttribute("C_USER");
        // 用户同意授权
        if (!"authdeny".equals(code)) {
            //获取OpenId
            OpenIdResult open = WeixinUtil.getOpenId(request, code, WeixinUtil.appid, WeixinUtil.appsecret);

            //检验授权凭证（access_token）是否有效
       /*     int result = WeixinUtil.checkAccessToken(open.getAccess_token(), open.getOpenid());
            if(0 != result){
                open = WeixinUtil.getNewAccess_Token(open,open.getRefresh_token(),TimedTask.appid);
            }*/
            // 网页授权接口访问凭证
           // String accessToken = open.getAccess_token();
            String openId = open.getOpenid();
            //获取微信用户详细信息，如果你不需要授权，可跳过该步骤，直接以微信的OpenId，查找是否已经绑定，没有跳转到绑定界面
            //WeixinUserInfo wxUser = WeixinUtil.getWeixinUserInfo(accessToken, openId);

            //从数据库获取当前微信绑定的用户
            User customer = userService.getUserByOpenId(open.getOpenid());
            log.error("数据库的用户" + customer);
            if(customer!=null){//当前用户已经绑定了
                if(customer.getStatus()==2){ //用户不可用
                    response.sendRedirect("/error/用户不可用");
                    return ;
                }else {
                    session.setAttribute("C_USER",customer);
                }
                //customer.setHeadPhoto(user.getHeadImgUrl());
            }else{
                log.error("未绑定用户");
               //未绑定用户，判断是否登陆，如果未登陆还要登陆
                if(user == null){
                    //未登陆前往登陆
                    response.sendRedirect("/toLogin/"+redirect);
                    return ;
                }else{
                    user.setOpenId(openId);
                    //绑定
                    userService.updateUser(user);
                    session.setAttribute("C_USER",user);
                }

            }
            session.setAttribute("C_OPENID", open);
            response.setContentType("text/html; charset=UTF-8");
            try {
                response.sendRedirect(WeixinUtil.getUrl(redirect));
            } catch (Exception e) {
                e.printStackTrace();
            }
          return ;
        }else{
            response.setContentType("text/html; charset=UTF-8");
            try {
                response.sendRedirect("/error/"+"取消授权");
            } catch (IOException e) {
                e.printStackTrace();
            }
        return ;
        }
    }


}
