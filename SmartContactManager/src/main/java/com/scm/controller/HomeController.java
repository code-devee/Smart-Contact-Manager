package com.scm.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.dao.UserRepository;
import com.scm.entities.User;
import com.scm.helper.Message;


@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	
	//for Home view
	@GetMapping("/")
	public String showHome(Model m) {
		m.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	//for signup page
	
	@GetMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Signup - Register Here");
		m.addAttribute("user", new User());
		return "signup";
	}

	// register handler
	
	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value="agreement", defaultValue = "false") boolean agreement,
			Model m , HttpSession session) {
		
		try {
			
			if(!agreement) {
				System.out.println("You have not agreed the terms and conditions.");
				throw new Exception("You have not agreed the terms and conditions.");
			}
			
			if(result.hasErrors()) {
				System.out.println("Errors "+result.toString());
				m.addAttribute("user", user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImage("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			User res = userRepository.save(user);
			
			m.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("User", user);
			session.setAttribute("message", new Message("Something went wrong "+e.getMessage(), "alert-danger"));
			
			return "signup";
		}
		
		
		
		 
	}

	// for login 
	
	@GetMapping("/signin")
	public String loginForm() {
		return "login";
	}


}
