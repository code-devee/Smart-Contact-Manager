package com.scm.service;


import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	@Autowired
	private JavaMailSender sender;
	
	public boolean sendEmail(String from, String to, int otp) throws MessagingException {
		
//		SimpleMailMessage message = new SimpleMailMessage();
//		
//
//		message.setFrom(from);
//		message.setTo(to);
//		message.setSubject("Smart Contact Manager-Forget password request");
//		message.setText("<h2>Your OTP for password change request is: "+otp+"</h2>");
		
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
		String htmlMsg = "<h3 style='color: blue;'>Your OTP for password change request is: "+otp+"</h3>";
		helper.setText(htmlMsg, true);
		helper.setTo(to);
		helper.setSubject("Smart Contact Manager-Forget password request");
		helper.setFrom(from);
		
		sender.send(message);
		
		return true;
	}

}
