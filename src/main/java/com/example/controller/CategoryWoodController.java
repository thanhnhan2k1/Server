package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.CategoryWood;
import com.example.repository.CategoryWoodRepository;

@RestController
@RequestMapping("/category-wood")
public class CategoryWoodController {
	@Autowired
	private CategoryWoodRepository cateRepo;
	@GetMapping("/getAll")
	private List<CategoryWood> getAll(){
		return cateRepo.findAll();
	}
}
