package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.services.EmailService;
import com.smart.entities.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private UserRepository userRepository;

	// method to add common data
	@ModelAttribute
	public void addCommomdata(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME : " + userName);
		// get the user using userName(Email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER : " + user);
		model.addAttribute("user", user);
	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard - Smart Contact Manager");
		return "normal/user_dashboard";
	}

	// open add contact form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact - Smart Contact Manager");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// Processing and uploading file
			if (file.isEmpty()) {
				// if the file is empty then message will display here..
				System.out.println("FILE IS EMPTY!!!");
				contact.setImage("contact.png");
			} else {
				// upload the file to folder and update the name to contact..
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("IMAGE UPLOADED SUCCESSFULLY!!!");
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("Contact added to database..");
			System.out.println("DATA " + contact);
			// success message...
			session.setAttribute("message", new Message("Contact Added Successfully!!!", "success"));
		} catch (Exception e) {
			System.out.println("ERROR : " + e.getMessage());
			e.printStackTrace();
			// error message..
			session.setAttribute("message", new Message("Something went wrong!! try again!!!", "danger"));
		}
		return "normal/add_contact_form";
	}

	// Show contact handler
	// per page = 5(n)
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal)

	{
		m.addAttribute("title", "Show User Contacts");

		// we have to send list of contacts from here

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 3);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";

	}

	// showing perticular contact detail
	@RequestMapping("/{cid}/contact/")
	public String showContactDetails(@PathVariable("cid") Integer cid, Model model, Principal principal) {
		System.out.println("cid " + cid);

		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();

		//
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {

			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	// delete contact handler

	@GetMapping("/delete/{cid}")
	public String deleteContact(Principal principal, @PathVariable("cid") Integer cid, Model model,
			HttpSession session) {

		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();

		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);

		session.setAttribute("message", new Message("Contact Deleted Successfully..","success"));
		return "redirect:/user/show-contacts/0";
	}

	// update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(Model m, @PathVariable("cid") Integer cid) {
		m.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}

	// update processing...
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(Principal principal, @ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, Model m, HttpSession session) {

		try {
			// old contact detail
			Contact oldcontactDetail = this.contactRepository.findById(contact.getCid()).get();
			if (!file.isEmpty()) {
				// Delete Old photo

				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldcontactDetail.getImage());
				file1.delete();

				// Update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldcontactDetail.getImage());
			}

			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your Contact is Updated", "success"));
		} catch (Exception e) {
			e.printStackTrace();

		}

		System.out.println("contact" + contact.getName());
		return "redirect:/user/" + contact.getCid() + "/contact/";
	}
	
	//Your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSetting()
	{
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(HttpSession session,Principal principal ,@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword)
	{
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message",new Message("Your Password Is Successfully changed","alert-success"));
			
		}
		else
		{
			session.setAttribute("message",new Message("Please Enter Correct Old Password","alert-danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
		
	}
	
	// open email form handler
		@GetMapping("/sendemail")
		public String openEmail() {
			return "normal/send";
		}
		@PostMapping("/mailsend")
		public String sendEmail(@RequestParam("subject") String subject,
		                        @RequestParam("message") String message,
		                        @RequestParam("to") String to,
		                        @RequestParam("attachment") MultipartFile attachment,
		                        Model model) throws IOException {
		    boolean result;
		    
		    if (attachment != null && !attachment.isEmpty()) {
		        result = this.emailService.sendEmail(subject, message, to, attachment);
		    } else {
		        result = this.emailService.sendEmail(subject, message, to, null);
		    }
		    
		    if (result) {
		        model.addAttribute("message", "Email is sent successfully");
		        model.addAttribute("successMessage", "Email sent successfully");
		        return "redirect:/user/sendemail#success";

		        
		    } else {
		        model.addAttribute("message", "Email not sent");
		        model.addAttribute("errorMessage", "Error sending email");
		        return "redirect:/user/sendemail#error";

		    }
		    
		  //  return "redirect:/user/sendemail";
		}




}