package com.controller;

import com.model.User;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;


    @GetMapping("/info")
    public String getUserInfo(Model model, HttpSession session){
        model.addAttribute("user",session.getAttribute("C_USER"));
        return "user/info";
    }


    @GetMapping("/list")
    @ResponseBody
    public List ListInfo(Model model){
        return userService.getListUser();
    }


}

