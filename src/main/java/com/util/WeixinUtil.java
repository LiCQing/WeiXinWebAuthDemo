package com.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeixinUtil {
    static Logger log = LoggerFactory.getLogger(WeixinUtil.class);

    //则scope为snsapi_base不会跳页面 snsapi_userinfo 会弹出页面
    private static String auth_url  = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=[appid]&redirect_uri=[projectUrl]/getOpenInfo/[redirectUrl]&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
    //其他url
    public final static String getOpen_id_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    public final static String getNewAccess_token = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=APPID&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";
    //公众号的配置
    public static String appid = "xx";
    public static String appsecret = "xx";
    public static String websiteAndProject = "http://127.0.0.1"; //项目地址
    //回调地址记录，可能部分地址带有参数，避免参数不识别等问题
    private static volatile Map<String,String>  urlMap = new ConcurrentHashMap<>();

    /**
     * 将需要访问的地址置入map中，以md5作为key
     * @param url
     * @return
     * @throws Exception
     */
    public static String setUrl(String url) throws Exception {
        if(StringUtils.isEmpty(url)){ throw new Exception("访问地址为空"); }
        String md5Key = DigestUtils.md5DigestAsHex(url.getBytes());
        urlMap.put(md5Key,url);
        return md5Key;
    }

    /**
     * 根据md5获取访问的全路径
     * @param key
     * @return
     * @throws Exception
     */
    public static String getUrl(String key) throws Exception {
        if(StringUtils.isEmpty(key)){ throw new Exception("key为空"); }
        String url = urlMap.get(key);
        return websiteAndProject + url;
    }

    /**
     * 获取授权地址，拼接
     * @param urlCode 回调地址的md5key
     * @return
     */
    public static String getAuthUrl(String urlCode){
        return auth_url.replace("[appid]",appid).replace("[projectUrl]",websiteAndProject).replace("[redirectUrl]",urlCode);
    }

    /**
     * 发起https请求并获取结果
     * @param requestUrl 请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr 提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);
            if ("GET".equalsIgnoreCase(requestMethod))
                httpUrlConn.connect();
            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.fromObject(buffer.toString());
        } catch (ConnectException ce) {
            log.error("Weixin server connection timed out.");
        } catch (Exception e) {
            log.error("https request error:{}", e);
        }
        return jsonObject;
    }

    /**
     * 获得用户基本信息
     * @param request
     * @param code
     * @param appid
     * @param appsecret
     * @return
     */
    public static OpenIdResult getOpenId(HttpServletRequest request, String code, String appid, String appsecret) {
        String requestURI = request.getRequestURI();
        String param = request.getQueryString();
        if(param!=null){
            requestURI = requestURI+"?"+param;
        }
        String url = getOpen_id_url.replace("APPID",appid).replace("SECRET",appsecret).replace("CODE",code);
        JSONObject jsonObject = httpRequest(url, "POST", null);
        OpenIdResult result = new OpenIdResult();
        if (null != jsonObject) {
            Object obj = jsonObject.get("errcode");
            if (obj == null) {
                result.setAccess_token(jsonObject.getString("access_token"));
                result.setExpires_in(jsonObject.getString("expires_in"));
                result.setOpenid(jsonObject.getString("openid"));
                result.setRefresh_token(jsonObject.getString("refresh_token"));
                result.setScope(jsonObject.getString("scope"));
            }else{
                System.out.println("获取openId回执："+jsonObject.toString()+"访问路径："+requestURI);
                log.error("访问路径:"+requestURI);
                log.error("获取openId失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));
            }
        }
        return result;
    }

  /**
     * 检验授权凭证（access_token）是否有效
     * @param accessToken 凭证
     * @param openid id
     * @return
     */
    public static int checkAccessToken(String accessToken, String openid) {
        String requestUrl = "https://api.weixin.qq.com/sns/auth?access_token="+accessToken+"&openid="+openid;
        JSONObject jsonObject = httpRequest(requestUrl, "GET", null);
        int result = 1;
        // 如果请求成功
        if (null != jsonObject) {
            try {
                result = jsonObject.getInt("errcode");
            } catch (JSONException e) {
                accessToken = null;
                // 获取token失败
                log.error("获取token失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));
            }
        }
        return result;
    }
    /**
     * 用户授权，使用refresh_token刷新access_token
     * @return
     */
    public static OpenIdResult getNewAccess_Token(OpenIdResult open,String refresh_token,String openId) {
        String requestUrl = getNewAccess_token.replace("REFRESH_TOKEN", refresh_token).replace("APPID", openId);
        JSONObject jsonObject = httpRequest(requestUrl, "GET", null);
        // 如果请求成功
        if (null != jsonObject) {
            try {
                open.setAccess_token(jsonObject.getString("access_token"));
            } catch (JSONException e) {
                // 获取token失败
                log.error("获取token失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));
            }
        }
        return open;
    }



    /**
     * 通过网页授权获取用户信息
     * @param accessToken 网页授权接口调用凭证
     * @param openId 用户标识
     * @return WeixinUserInfo
     */
    public static WeixinUserInfo getWeixinUserInfo(String accessToken, String openId) {
        WeixinUserInfo user = null;

        // 拼接请求地址
        String requestUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace("OPENID", openId);
        // 通过网页授权获取用户信息
        JSONObject jsonObject = httpRequest(requestUrl, "GET", null);
        if (null != jsonObject) {
            try {
                user = new WeixinUserInfo();
                // 用户的标识
                user.setOpenId(jsonObject.getString("openid"));
                // 昵称
                user.setNickname(jsonObject.getString("nickname"));
                // 性别（1是男性，2是女性，0是未知）
                user.setSex(jsonObject.getInt("sex"));
                // 用户所在国家
                user.setCountry(jsonObject.getString("country"));
                // 用户所在省份
                user.setProvince(jsonObject.getString("province"));
                // 用户所在城市
                user.setCity(jsonObject.getString("city"));
                // 用户头像
                user.setHeadImgUrl(jsonObject.getString("headimgurl"));
                // 用户特权信息
                user.setPrivilegeList(JSONArray.toList(jsonObject.getJSONArray("privilege"), List.class));
            } catch (Exception e) {
                user = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                log.error("获取用户信息失败 errcode:{} errmsg:{}，reqUrl{}", errorCode, errorMsg);
            }
        }
        return user;
    }
}