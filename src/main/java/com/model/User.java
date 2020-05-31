package com.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String openId;
    private String userName;
    private String passWord;
    private int status;

    public void checkNull() throws Exception {
        if(StringUtils.isBlank(userName)  ){
            throw new Exception("用户名不能为空");
        }

        if(StringUtils.isBlank(passWord)){
            throw new Exception("密码不能为空");
        }
    }
}
