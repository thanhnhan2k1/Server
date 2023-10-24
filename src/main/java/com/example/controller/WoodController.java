package com.example.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.model.Image;
import com.example.model.Wood;
import com.example.repository.WoodRepository;
import com.example.service.FireBaseFileService;

@RestController
@RequestMapping("/wood")
public class WoodController {
	@Autowired
	private WoodRepository woodRepo;
	@Autowired
	private FireBaseFileService service;
	@GetMapping("/get")
	private Page<Wood> getWood2(
			@RequestParam(name="category")int  category,
			@RequestParam(name = "pageNum") int pageNum,
			@RequestParam(name = "key", defaultValue = "", required = false) String key,
			@RequestParam(name = "family", required = false) List<String> listFamily,
			@RequestParam(name = "area", required = false) List<String> listArea,
			@RequestParam(name = "color", required = false) List<String> listColor,
			@RequestParam(name = "cites", required = false) List<String> listCistes,
			@RequestParam(name = "preservation", required = false) List<String> listIucns,
			@RequestParam(name = "sort", required = false) String sort
//			@RequestParam(name = "from", required = false) String from,
//			@RequestParam(name = "to", required = false) String to
			, HttpServletRequest request) {
		int size = 8;
		// sort
		String sortField = "vietnameName";
		if (sort != null) {
			if (sort.equalsIgnoreCase("Tên Việt Nam thường gọi"))
				sortField = "vietnameName";
			else if (sort.equalsIgnoreCase("Tên khoa học"))
				sortField = "scientificName";
			else if (sort.equalsIgnoreCase("Tên thương mại"))
				sortField = "commercialName";
			else if (sort.equalsIgnoreCase("Trọng lượng riêng"))
				sortField = "specificGravity";
		}

		// tim kiem theo tu khoa
		Specification<Wood> nameLike = (root, query, criteriaBuilder) -> {
			String likeKey = "%" + key + "%";
			return criteriaBuilder.or(criteriaBuilder.like(root.get("vietnameName"), likeKey),
					criteriaBuilder.like(root.get("scientificName"), likeKey));
		};
		
		// lọc theo tên tiếng việt không null
		
		// tim kiem theo mau

		Specification<Wood> filter = Specification.where(nameLike);
		// tìm kiếm theo category
		
		Specification<Wood> categoryWood = (root, query, criteriaBuilder) -> {
			return criteriaBuilder.equal(root.get("categoryWood"),category);
		};
		filter=filter.and(categoryWood);
		// tim kiem theo ho
		if (listFamily != null) {
			Specification<Wood> belongToFamily = (root, query, criteriaBuilder) -> {
				query.distinct(true);
				return criteriaBuilder.in(root.get("family").get("english")).value(listFamily);
			};
			filter = filter.and(belongToFamily);
		}
		// tim kiem theo cites
		if (listCistes != null) {
			Specification<Wood> belongToAppendixCites = (root, query, criteriaBuilder) -> criteriaBuilder
					.in(root.get("appendixCites").get("name")).value(listCistes);
			filter = filter.and(belongToAppendixCites);
		}
		// tim kiem theo iucns
		if (listIucns != null) {
			Specification<Wood> belongToPreservation = (root, query, criteriaBuilder) -> criteriaBuilder
					.in(root.get("preservation").get("acronym")).value(listIucns);
			filter = filter.and(belongToPreservation);
		}

		// tim kiem theo phan vung
		if (listArea != null) {
			Specification<Wood> belongToArea = (root, query, criteriaBuilder) -> criteriaBuilder
					.in(root.join("listAreas").get("english")).value(listArea);
			filter = filter.and(belongToArea);
		}

		// tim kiem dua tren mau
		if (listColor != null) {
			Specification<Wood> belong = (root, query, criteriaBuilder) -> criteriaBuilder
					.like(root.get("color"), "%" + listColor.get(0) + "%");
			Specification<Wood> belongToColor = Specification.where(belong);
			for (int i = 1; i < listColor.size(); i++) {
				String like = "%" + listColor.get(i) + "%";
				belong = (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("color"), like);
				belongToColor = belongToColor.or(belong);
			}
			filter = filter.and(belongToColor);
		}
		// lay dua tren from and to
//		if (from != null && to != null) {
//			Specification<Wood> belong = (root, query, criteriaBuilder) -> {
//					return criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("specificGravity"), from),
//							criteriaBuilder.lessThanOrEqualTo(root.get("specificGravity"), to));
//			};
//			filter = filter.and(belong);
//		}
		Pageable pageable = PageRequest.of(pageNum - 1, size, Sort.by(sortField).ascending());
		return woodRepo.findAll(filter, pageable);
	}
	@GetMapping("/get-400-species")
	private Page<Wood> getWood400Species(@RequestParam(name = "pageNum") int pageNum,
			@RequestParam(name = "key", defaultValue = "", required = false) String key,
			@RequestParam(name = "family", required = false) List<String> listFamily,
			@RequestParam(name = "area", required = false) List<String> listArea,
			@RequestParam(name = "color", required = false) List<String> listColor,
			@RequestParam(name = "cites", required = false) List<String> listCistes,
			@RequestParam(name = "sort", required = false) String sort) {

		int size = 9;
		// sort
		String sortField = "vietnameName";
		if (sort != null) {
			if (sort.equalsIgnoreCase("Tên khoa học"))
				sortField = "scientificName";
			else if (sort.equalsIgnoreCase("Tên thương mại"))
				sortField = "commercialName";
		}

		// tim kiem theo tu khoa
		Specification<Wood> nameLike = (root, query, criteriaBuilder) -> {
			String likeKey = "%" + key + "%";
			return criteriaBuilder.like(root.get("scientificName"), likeKey);
		};
		
		
		// tim kiem theo mau

		Specification<Wood> filter = Specification.where(nameLike);
		// lọc theo tên tiếng việt không null
		Specification<Wood> VietNameseNull = (root, query, criteriaBuilder) -> {
			return criteriaBuilder.isNull(root.get("vietnameName"));
		};
		filter=filter.and(VietNameseNull);
		// tim kiem theo ho
		if (listFamily != null) {
			Specification<Wood> belongToFamily = (root, query, criteriaBuilder) -> {
				query.distinct(true);
				return criteriaBuilder.in(root.get("family").get("english")).value(listFamily);
			};
			filter = filter.and(belongToFamily);
		}
		// tim kiem theo cites
		if (listCistes != null) {
			Specification<Wood> belongToAppendixCites = (root, query, criteriaBuilder) -> criteriaBuilder
					.in(root.get("appendixCites").get("name")).value(listCistes);
			filter = filter.and(belongToAppendixCites);
		}
		

		// tim kiem theo phan vung
		if (listArea != null) {
			Specification<Wood> belongToArea = (root, query, criteriaBuilder) -> criteriaBuilder
					.in(root.join("listAreas").get("english")).value(listArea);
			filter = filter.and(belongToArea);
		}

		// tim kiem dua tren mau
		if (listColor != null) {
			Specification<Wood> belong = (root, query, criteriaBuilder) -> criteriaBuilder
					.like(root.get("color"), "%" + listColor.get(0) + "%");
			Specification<Wood> belongToColor = Specification.where(belong);
			for (int i = 1; i < listColor.size(); i++) {
				String like = "%" + listColor.get(i) + "%";
				belong = (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("color"), like);
				belongToColor = belongToColor.or(belong);
			}
			filter = filter.and(belongToColor);
		}
		
		Pageable pageable = PageRequest.of(pageNum - 1, size, Sort.by(sortField).ascending());
		return woodRepo.findAll(filter, pageable);
	}
//	@GetMapping("/getMaxSpecificGravity")
//	private int getMaxSpecificGravity() {
//		List[]list=woodRepo.findMaxSpecificGravity();
//		return Integer.valueOf(list[0].get(0).toString());
//	}
//	
//	@GetMapping("/getMinSpecificGravity")
//	private int getMinSpecificGravity() {
//		List[]list=woodRepo.findMinSpecificGravity();
//		return Integer.valueOf(list[0].get(0).toString());
//	}
	@GetMapping("/getAll")
	private List<Wood>getAll(@RequestParam(name="category", defaultValue = "0", required = false)int category){
		if(category==0)
			return woodRepo.findAll();
		else
			return woodRepo.findByCategoryWoodId(category);
	}
	@PostMapping("/save")
	private Wood getWood(@RequestPart("file")MultipartFile[] file, @RequestPart("wood")Wood wood) throws IOException {
		List<Image>listImg=new ArrayList<>();
		for(MultipartFile i:file) {
			Image img=new Image();
			String url=service.saveFile(i);
			img.setPath(url);
			img.setWood(wood);
			listImg.add(img);
		}
		wood.setListImage(listImg);
		return woodRepo.save(wood);
	}
	@PostMapping("/delete")
	private void deleteWood(@RequestBody Wood wood) {
		for(Image i:wood.getListImage()) {
			String pathfile = i.getPath();
			// Tìm index của ký tự "/" cuối cùng trong URL
			int lastIndex = pathfile.lastIndexOf("/");
			// Tìm index của ký tự "?" đầu tiên sau ký tự "/"
			int indexOfQuestionMark = pathfile.indexOf("?", lastIndex);
			// Lấy ra chuỗi con giữa các index tìm được
			String fileName = pathfile.substring(lastIndex + 1, indexOfQuestionMark);
			service.deleteFile(fileName);
		}
		woodRepo.delete(wood);
	}
	@GetMapping("/getById")
	private Wood getById(int id) {
		return woodRepo.findById(id);
	}
	
	@GetMapping("/getAmountWood")
	private int getAmountWood() {
		return woodRepo.getAmountWood();
	}
}
