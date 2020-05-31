package com.controller;

import com.model.User;
import com.service.UserService;
import com.util.WeixinUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class LoginController {

    @Autowired
    UserService userService;

    /**
     * 登陆或者注册
     * @param userName
     * @param passWord
     * @param model
     * @return
     */
    @PostMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response,String userName, String passWord, Model model, String redirect) throws IOException, ServletException {
        HttpSession session = request.getSession();
        User user = new User().setUserName(userName).setPassWord(passWord);
        try{
            User newUser = userService.loginOrRegister(user);
            model.addAttribute("user",newUser);
            session.setAttribute("C_USER",newUser);
            if(StringUtils.isBlank(redirect)){
                redirect = "home";
            }
            session.removeAttribute("login_msg");
            response.sendRedirect(WeixinUtil.getUrl(redirect));
        }catch (Exception e){
            e.printStackTrace();
            // model.addAttribute("msg",e.getMessage());
            //request.getRequestDispatcher("/toLogin/" + redirect).forward(request,response);
            session.setAttribute("login_msg",e.getMessage());
            response.sendRedirect("/toLogin/"+redirect);
            return;
        }
    }


    @RequestMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        session.invalidate();
        response.sendRedirect("/");
    }

}
