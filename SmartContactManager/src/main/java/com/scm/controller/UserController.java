package com.scm.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.scm.dao.ContactRepository;
import com.scm.dao.UserRepository;
import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.helper.Message;


@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	//method for adding common data to response
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("User Name " +userName);
		
		//get the userdetails by username 
		
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		
	}
	
	//dashboad home
	@GetMapping("index")
	public String dashboard(Model model, Principal principal)
	{
		model.addAttribute("titile", "My Dashboard");
		return "normlusr/userdashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normlusr/add_contact";
	}
	
	//process data from contact from
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact,  BindingResult result,
			@RequestParam("profileImage") MultipartFile file, 
			Principal principal,
			HttpSession session, Model model) {
		
		
		if(result.hasErrors()) {
			
			System.out.println("Errors "+result.toString());
			model.addAttribute("contact", contact);
			return "normlusr/add_contact";
			
		}
		
		try {
			System.out.println("contacts : "+contact);
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);
			
			contact.setUser(user);
			
			if(file.isEmpty()) {
				contact.setImage("contact.png");
				System.out.println("File is Empty");
				
			}
			else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			
			user.getContacts().add(contact);
			
			userRepository.save(user);
			System.out.println("Added to Database");
			session.setAttribute("message", new Message("Contact added successfully!!", "alert-success"));
			return "normlusr/add_contact";
		} 
		
		catch (Exception e) {
			System.out.println("error occured");
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong!! "+e.getMessage(), "alert-danger"));
			return "normlusr/add_contact";
		}
		
	}
	
	
	// show or view contacts handler
	
	@GetMapping("/viewcontacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, 
			Model model,
			Principal principal) {
		
		model.addAttribute("title", "view all contacts");
		
		String name = principal.getName();
		User user = userRepository.getUserByUserName(name);
		
		Pageable pageable = PageRequest.of(page, 4);
		
		Page<Contact> contacts = contactRepository.findContactByUser(user.getId(), pageable);
		
		model.addAttribute("contacts", contacts);
		
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normlusr/all_contacts";
	}
	
	
	// handler for showing single contact details
	@GetMapping("/{id}/contact")
	public String viewDetails(@PathVariable("id") int cid, Model model, Principal principal) {
		
		String name = principal.getName();
		System.out.println("principal name is : " +name);
		
		User user = this.userRepository.getUserByUserName(name);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		
		if(user.getId() == contact.getUser().getId() ) {
			
			model.addAttribute("contact", contact);
			return "normlusr/contact_details";
		}
		else {
			return "normlusr/contact_details";
		}
		
	}
	
	
	// handler for delete contact
	
	@RequestMapping("/delete/{id}")
	public String deleteContact(@PathVariable("id") int id, Model model, Principal principal, HttpSession session) {
		
		String name = principal.getName();
		
		User user = this.userRepository.getUserByUserName(name);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(id);
		Contact contact = contactOptional.get();
		
		if(user.getId() == contact.getUser().getId()) {
			
			contactRepository.deleteById(id);
			model.addAttribute("contact", contact);
			session.setAttribute("message", new Message("Contact deleted successfully!!", "alert-success"));
			return "redirect:/user/viewcontacts/0";
		}
		else {
			session.setAttribute("message", new Message("This contact doesn't exist!!", "alert-danger"));
			return "redirect:/user/viewcontacts/0";
		}
	
	}
	
	
	// To show update form
	// PostMapping is by-default secure

	@PostMapping("/update/{id}")
	public String showUpdateForm(@PathVariable("id") int id, Model model, Principal principal) {
		
		model.addAttribute("title", "Update here");
		
		Contact contact = this.contactRepository.findById(id).get();
		
		model.addAttribute("contact", contact);
		
		return "normlusr/update_form";
	}
	
	//update form handler
	
	@PostMapping("/update-contact")
	public String updateProcess(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Model model,
			Principal principal,
			HttpSession session) {
		
		try {
			
			Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty())
			{
				//rewrite with the same file
				//delete old photo and replace new one
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();
				//update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
			}
			else 
			{
				contact.setImage(oldContactDetails.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated..", "alert-success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Conatact name "+contact.getName());
		System.out.println("Contact id "+contact.getcId());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	// User profile will show here
	
	@GetMapping("/profile")
	public String showYourProfile(Model model, Principal principal) {
		
		
		model.addAttribute("title", "Your Profile");
		
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		int size = user.getContacts().size();
		
		model.addAttribute("user", user);
		model.addAttribute("size", size);
		
		return "normlusr/userprofile";
	}
	
	@GetMapping("/setting")
	public String showSetting(Model model) {
		model.addAttribute("title", "Setting");
		return "normlusr/setting";
	}
	
	//handler for changing the password
	@PostMapping("/process-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, 
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		String currentPassword = user.getPassword();
		
		if(newPassword.length() >= 4) {
			
			if(passwordEncoder.matches(oldPassword, currentPassword)) {
				System.out.println("password matches");
				user.setPassword(passwordEncoder.encode(newPassword));
				this.userRepository.save(user);
				
				session.setAttribute("message", new Message("Password changed successfully", "alert-success"));
			}
			else {
				session.setAttribute("message", new Message("Password didn't match", "alert-danger"));
			}
			
			return "redirect:/user/setting";
			
		}
		else {
			
			session.setAttribute("message", new Message("Password shouldbe more than 4 characters","alert-warning"));
			
			return "normlusr/setting";
		}
		
	
	}
	
	
}
