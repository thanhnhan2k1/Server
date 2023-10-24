package com.example.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Columns;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="user")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	@Column(nullable = false, unique = true)
	private String email;
	
	@JsonIgnore
	private String password;
	private String address;
	private String phone;
	private Date createAt;
	private Date updateAt;
	
	@Enumerated(EnumType.STRING)
	private ERole role;
	
	@Enumerated(EnumType.STRING)
	private EProvider provider;
	
	//khi nguoi dùng được đăng kí, enabled =false khi xác minh tài khoản thành công thì enabled=true
	private boolean enabled;
	@PrePersist
	void setEnable() {
//		this.enabled=false;
		try {
		SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		Date date=new Date(System.currentTimeMillis());
		this.createAt=format.parse(format.format(date));
		this.updateAt=format.parse(format.format(date));
		}catch(ParseException e) {
			e.printStackTrace();
		}
	}
}
