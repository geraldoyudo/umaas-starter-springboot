package com.gerald.starter.springboot.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {
	
	@Value("${umaas.registrationUrl:http://test.isslserv.com:8070/umaas-registration/app/register?domain=com.gerald.general}")
	private String registrationUrl;
	
	@RequestMapping(value = "/login")
    public String login(){
        return "login";
    }
	@RequestMapping(value = "/register")
    public String register(){
        return "redirect:" + registrationUrl;
    }
}
