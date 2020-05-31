package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageController {
    /**
     * 首页
     * @param model
     * @return
     */
    @GetMapping("home")
    public String toHome(Model model){
        model.addAttribute("code",200);
        model.addAttribute("msg","hello world");
        return "home";
    }

    /**
     * 登陆页面
     * @param model
     * @return
     */
    @GetMapping("toLogin/{redirect}")
    public String toLogin(Model model, @PathVariable String redirect){
        model.addAttribute("redirect",redirect);
        return "login";
    }

    /**
     * 登陆页面
     * @param model
     * @return
     */
    @RequestMapping("toLogin/")
    public String toLoginWitOutRedict(Model model){
        return "login";
    }

    /**
     * 登陆页面
     * @param model
     * @return
     */
    @RequestMapping("error/{msg}")
    public String toError(Model model, @PathVariable String msg){
        model.addAttribute("msg",msg);
        return "unerror";
    }
}
