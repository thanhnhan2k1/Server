package com.example.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="used_service")
public class UsedService {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name="date_start")
	private Date dateStart;
	@Column(name="date_end")
	private Date dateEnd;
	@Column(name="type_payment")
	private String typePayment;
	@ManyToOne(cascade = CascadeType.MERGE)
	private Service service;
	@ManyToOne(cascade = CascadeType.MERGE)
	private User user;
	private String status;
	
	@OneToOne(cascade = CascadeType.MERGE)
	private TransactionHistory tranHistory;
}
