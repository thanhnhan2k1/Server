package com.example.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.model.Service;


public interface ServiceRepository extends JpaRepository<Service, Integer>{
	Service findById(int id);
	
	@Query(value = "SELECT count(*) FROM service", nativeQuery = true)
	int getAmountDichVu();
	
	@Query(value = "select b.*, sum(b.price) as 'totalMoney' from used_service as a, service as b\n"
			+ "where a.service_id=b.id and a.date_start>=? and a.date_start<=? \n"
			+ "and a.status='SUCCESS'\n"
			+ "group by b.id\n"
			+ "order by totalMoney desc", nativeQuery = true)
	List[] getServiceStat(Date dateStart, Date dateEnd);
}
