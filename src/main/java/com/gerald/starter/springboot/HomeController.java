package com.gerald.starter.springboot;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public String index(){
		return "index";
	}
	@GetMapping("/home")
	public String home(){
		return "home";
	}
	@GetMapping("/page1")
	public String page1(){
		return "page1";
	}
	@GetMapping("/page2")
	public String page2(){
		return "page2";
	}
	@GetMapping("/page3")
	public String page3(){
		return "page3";
	}
}
