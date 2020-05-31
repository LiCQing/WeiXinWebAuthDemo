package com.util;

import lombok.Data;
import net.sf.json.JSONArray;

import java.util.List;

@Data
public class WeixinUserInfo {
    private String openId;
    private String nickname;
    private Integer sex;
    private String country;
    private String province;
    private String city;
    private String headImgUrl;
    private List privilegeList;
}
