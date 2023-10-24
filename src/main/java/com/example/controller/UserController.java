package com.example.controller;

import java.net.URI;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.dto.UserDTO;
import com.example.exception.ResourceNotFoundException;
import com.example.model.EProvider;
import com.example.model.ERole;
import com.example.model.User;
import com.example.model.VerificationToken;
import com.example.repository.UserRepository;
import com.example.repository.VerificationTokenRepository;
import com.example.security.TokenProvider;
import com.example.security.UserPrincipal;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private MessageSource messages;
	@Autowired
	private VerificationTokenRepository veriTokenRepo;
	@Autowired
    private AuthenticationManager authenticationManager;
	@Autowired
	private TokenProvider tokenProvider;

	// đăng kí tài khoản
	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody UserDTO user) {
		if (userRepo.existsByEmail(user.getEmail()))
			return ResponseEntity.status(HttpStatus.IM_USED).body("fail");
//		if (user.getPassword().equals(user.getMatchingPassword()) == false)
//			return ResponseEntity.badRequest().body("Mat khau nhap lai khong dung");
		User u = new User();
		u.setEmail(user.getEmail());
		u.setName(user.getName());
		u.setPassword(passwordEncoder.encode(user.getPassword()));
		if (user.getRole() == null)
			u.setRole(ERole.ROLE_USER);
		else
			u.setRole(ERole.ROLE_ADMIN);
		u.setProvider(EProvider.local);
		u.setEnabled(true);
		u = userRepo.save(u);
		return ResponseEntity.ok().body("success");
	}
	//reset password
	@PostMapping("/resetPassword")
//	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<String> resetPassword(@RequestBody User user) {
		try {
			System.out.println(user.getEmail());
			int min = 10000000; // Số nhỏ nhất có 8 chữ số là 10,000,000
	        int max = 99999999; // Số lớn nhất có 8 chữ số là 99,999,999

	        Random random = new Random();
	        String token =(random.nextInt(max - min + 1) + min) +"";
			// luu ma token
			VerificationToken verificationToken = new VerificationToken(token, user);
			veriTokenRepo.save(verificationToken);
			String recipientAddress = user.getEmail();
			String subject = "Đặt lại mật khẩu";
			String text = "Mã xác nhận đặt lại mật khẩu: "+token;
			SimpleMailMessage email1 = new SimpleMailMessage();
			email1.setTo(recipientAddress);
			email1.setSubject(subject);
			email1.setText(text);
			mailSender.send(email1);
		} catch (MailException ex) {
			return ResponseEntity.ok("fail");
		}
		return ResponseEntity.ok("success");
	}

	// luu mat khau khi reset lai
	@GetMapping("/resetPassword")
//	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<String> savePassword(@RequestParam(name="token")String token, @RequestParam("password")String password) {
		VerificationToken veri=veriTokenRepo.findByToken(token);
		if (veri == null)
			return ResponseEntity.ok("fail");
		Calendar cal = Calendar.getInstance();
		if (veri.getExpiryDate().getTime() - cal.getTime().getTime() <= 0)
			return ResponseEntity.ok("fail");
		User user=veri.getUser();
		user.setPassword(passwordEncoder.encode(password));
		// cap nhat lai mat khau
		userRepo.save(user);
		// xoa token
		veriTokenRepo.delete(veri);
		return ResponseEntity.ok("success");
	}
	
	//thay doi mat khau khi dang nhap thanh cong
//	@PostMapping("/updatePassword")
//	@PreAuthorize("hasRole('USER')")
//	public String changUserPassword(@RequestParam("password")String password, @RequestParam("oldpassword")String oldpassword) {
//		User user=userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
//		if(!passwordEncoder.matches(oldpassword, user.getPassword())){
//			return "mat khau khong dung";}
//		user.setPassword(passwordEncoder.encode(password));
//		userRepo.save(user);
//		return "cap nhat thanh cong";
//	}
	//login
	// neu chua duoc xac thuc thì nó sẽ lỗi 403
	@PostMapping("/login")
	public ResponseEntity<String> user(@RequestBody UserDTO user) {
		Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        user.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.createToken(authentication);
        User user1=new User();
        UserPrincipal userPrincipal=(UserPrincipal)authentication.getPrincipal();
        user1.setEmail(userPrincipal.getEmail());
        user1.setName(userPrincipal.getName());
        user1.setId(userPrincipal.getId());
        user1.setAddress(userPrincipal.getAddress());
        user1.setCreateAt(userPrincipal.getCreateAt());
        user1.setUpdateAt(userPrincipal.getUpdateAt());
        user1.setPhone(userPrincipal.getPhone());
        user1.setProvider(userPrincipal.getProvider());
        user1.setRole(userPrincipal.getRole());
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/getUserInfo")
                .buildAndExpand(user1.getId()).toUri();
        return ResponseEntity.created(location)
                .body(token);
	}
	@GetMapping("/getUserInfo")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public User getUserInfo(@com.example.security.CurrentUser UserPrincipal userPrincipal) {
		return userRepo.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
	}
//	
//	@PostMapping("/updateInfor")
//	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
//	public User updateInfor(@RequestBody User user) {
//		User u=userRepo.findByEmail(user.getEmail());
//		u.setName(user.getName());
//		u.setAddress(user.getAddress());
//		u.setPhone(user.getPhone());
//		return userRepo.save(u);
//	}
	
	@PostMapping("/updateInfor")
	public User updateInfor(@RequestBody User user) {
		User u=userRepo.findByEmail(user.getEmail());
		u.setName(user.getName());
		u.setAddress(user.getAddress());
		u.setPhone(user.getPhone());
		return userRepo.save(user);
	}
}