package com.example.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.service.FireBaseFileService;


@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "*")
public class UploadFile {
	@Autowired
	private FireBaseFileService service;
	
	@PostMapping("/saveFile")
	private String saveFile(@RequestBody MultipartFile file) throws IOException {
		return service.saveFile(file);
	}
	@GetMapping("/deleteFile")
	private boolean deleteFile(@RequestParam("name")String name) {
		return service.deleteFile(name);
	}
	
}
