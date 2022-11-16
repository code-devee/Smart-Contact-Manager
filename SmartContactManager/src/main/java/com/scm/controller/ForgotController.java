package com.scm.controller;


import java.util.Random;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.dao.UserRepository;
import com.scm.entities.User;
import com.scm.helper.Message;
import com.scm.service.EmailService;

@Controller
public class ForgotController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("/forgot")
	public String showForgotPage(Model model) {
		
		model.addAttribute("title", "Forget password");
		
		return "forgot";
	}
	
	@PostMapping("/process-forgot")
	public String sendOtp(@RequestParam("email") String email, Model model, HttpSession session) throws MessagingException {
		

		model.addAttribute("title", "Verify otp here");
		System.out.println("Email is "+email);
		
		User user = this.userRepository.getUserByUserName(email);
		
		if(user == null) {
			
			System.out.println("Check your mail");
			session.setAttribute("message", new Message("Email doesn't exist", "alert-danger"));
			return "forgot";
			
		}
		else {
			
			Random random = new Random();
			int otp = random.nextInt(999999 - 100000) + 100000;
			System.out.println("OTP is "+otp);
			
			boolean sendEmail = emailService.sendEmail("deveeprasadpanda@gmail.com", email, otp);
				System.out.println("Otp sent to email");
				session.setAttribute("myotp", otp);
				session.setAttribute("myemail", email);
				return "verify_otp";
		}

		
	}
	
	@PostMapping("/process-otp")
	public String otpProcess(@RequestParam("otp") int otp, Model model, HttpSession session) {
		
		
		//success - to password change view
		
		int myotp = (int)session.getAttribute("myotp");
		
		if(myotp == otp) {
			
			model.addAttribute("title", "Reset Password");
			
			return "new_password";
		}
		
		//failed - same page
		else {
			
			session.setAttribute("message", new Message("You have entered the wrong OTP","alert-danger"));
			
			System.out.println("Otp doesn't match");
			return "verify_otp";
		}		
		
	}
	
	
	@PostMapping("/reset-password")
	public String setNewPassword(@RequestParam("password") String password,
			
			HttpSession session, Model model) {
		
		String currentEmail = (String) session.getAttribute("myemail");
		User user = this.userRepository.getUserByUserName(currentEmail);
		
		if(password.length() >= 4) {
			
			user.setPassword(passwordEncoder.encode(password));
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Password changed successfully,login here to continue","alert-success"));
		
			return "redirect:/signin";
		}
		else {
			
			session.setAttribute("message", new Message("Password shouldbe more than 4 characters","alert-warning"));
			
			return "new_password";
		}
		
		
		
		
	}
			
	
	
	
}
