package com.example.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.Service;
import com.example.model.TransactionHistory;
import com.example.model.UsedService;
import com.example.model.User;
import com.example.repository.ServiceRepository;
import com.example.repository.UsedServiceRepository;
import com.example.repository.UserRepository;



@RestController
@RequestMapping("/usedService")
public class UsedServiceController {
	@Autowired
	private UsedServiceRepository usedServiceRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ServiceRepository serviceRepo;
	@GetMapping("/get")
	private UsedService getUsedService(@RequestParam("id") int id) {

		UsedService usedService = new UsedService();
		Calendar calendar = Calendar.getInstance();
		usedService = usedServiceRepo.findByUserIdAndStatusAndDateEndGreaterThan(id, "SUCCESS", calendar.getTime());
		return usedService;
	}

	@PostMapping("/payment")
	private String save(@RequestBody UsedService usedService,HttpServletRequest req, HttpServletResponse response) throws IOException {

		System.out.println("da vao day");
		// tao va luu tru thong tin order
		usedService = usedServiceRepo.save(usedService);

		// tao hoa don thanh toan
		String vnp_Version = "2.1.0";
		String vnp_Command = "pay";
		String orderType = "other";
		long amount = 100000 * 100;

//		String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
		String vnp_IpAddr = VNPayConfig.getIpAddress(req);

		String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

		Map<String, String> vnp_Params = new HashMap<>();
		vnp_Params.put("vnp_Version", vnp_Version);
		vnp_Params.put("vnp_Command", vnp_Command);
		vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
		vnp_Params.put("vnp_Amount", String.valueOf((int) usedService.getService().getPrice() * 100));
		vnp_Params.put("vnp_CurrCode", "VND");

		vnp_Params.put("vnp_TxnRef", usedService.getId() + "");
		vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + usedService.getDateStart());
		vnp_Params.put("vnp_OrderType", orderType);

		vnp_Params.put("vnp_Locale", "vn");
		vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
		//vnp_Params.put("vnp_IpnURL", VNPayConfig.vnp_IpnURL);
		vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

		Calendar cld = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		cld.add(Calendar.HOUR, 7);
		String vnp_CreateDate = formatter.format(cld.getTime());
		System.out.println(vnp_CreateDate);
		vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
		cld.add(Calendar.MINUTE, 15);
		String vnp_ExpireDate = formatter.format(cld.getTime());
		System.out.println(vnp_ExpireDate);
		vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

		List fieldNames = new ArrayList(vnp_Params.keySet());
		Collections.sort(fieldNames);
		StringBuilder hashData = new StringBuilder();
		StringBuilder query = new StringBuilder();
		Iterator itr = fieldNames.iterator();
		while (itr.hasNext()) {
			String fieldName = (String) itr.next();
			String fieldValue = (String) vnp_Params.get(fieldName);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				// Build hash data
				hashData.append(fieldName);
				hashData.append('=');
				hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
				// Build query
				query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
				query.append('=');
				query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
				if (itr.hasNext()) {
					query.append('&');
					hashData.append('&');
				}
			}
		}
		String queryUrl = query.toString();
		String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
		queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
		String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
		//response.sendRedirect(paymentUrl);
		return paymentUrl;
	}
	/*
	 * Cập nhật kết quả thanh toán từ vnpay - Kiểm tra checksum - tìm giao dịch
	 * trong database - kiểm tra số tiền giữa hai hệ thống - kiểm tra tình trạng của
	 * giao dịch trước khi cập nhật - Cấp nhật kết quả vào database - Trả kết quả
	 * ghi nhận lại cho vnpays
	 */
	@GetMapping("/vnpay_return")
	public void vnpay_ipn(HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException {
		System.out.println("da vao day");
		
		Map<String, String> fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
           String fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
           System.out.println(fieldName);
           String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
           System.out.println(fieldValue);
           if ((fieldValue != null) && (fieldValue.length() > 0)) {
               fields.put(fieldName, fieldValue);
           }
       }
		String vnp_SecureHash = request.getParameter("vnp_SecureHash");
		System.out.println(request.getParameter("vnp_BankTranNo"));
		if (fields.containsKey("vnp_SecureHashType")) {
			fields.remove("vnp_SecureHashType");
		}
		if (fields.containsKey("vnp_SecureHash")) {
			fields.remove("vnp_SecureHash");
		}
		String signValue = VNPayConfig.hashAllFields(fields);
		if (signValue.equals(vnp_SecureHash)) {
			UsedService usedService = usedServiceRepo.findById(Integer.parseInt(fields.get("vnp_TxnRef")));
			if (usedService != null) {
				int amount = Integer.parseInt(fields.get("vnp_Amount")) / 100;
				if(usedService.getStatus().equals("PENDING")) {
					if (amount == usedService.getService().getPrice()) {
						if ("00".equals(fields.get("vnp_ResponseCode")))
							usedService.setStatus("SUCCESS");
						else
							usedService.setStatus("FAILED");
						TransactionHistory tranHistory = new TransactionHistory();
						tranHistory.setAmount(amount);
						tranHistory.setBankCode(fields.get("vnp_BankCode"));
						tranHistory.setBankTranNo(fields.get("vnp_BankTranNo"));
						tranHistory.setCardType(fields.get("vnp_CardType"));
						tranHistory.setOrderInfor(fields.get("vnp_OrderInfo"));
						tranHistory.setPayDate(fields.get("vnp_PayDate"));
						tranHistory.setResponseCode(fields.get("vnp_ResponseCode"));
						tranHistory.setTmnCode(fields.get("vnp_TmnCode"));
						tranHistory.setTransactionNo(fields.get("vnp_TransactionNo"));
						tranHistory.setTransactionStatus("vnp_TransactionStatus");
						usedService.setTranHistory(tranHistory);
						usedService = usedServiceRepo.save(usedService);
//						System.out.println("dung");
//						return ResponseEntity.ok(usedService);
//						String url=request.getRequestURL().toString();
						String url="https://da-production.up.railway.app/usedService/vnpay_return?";
						String query=request.getQueryString();
						response.sendRedirect(url+query);
					}
				}
			}
		}
		else
			System.out.println("khong dung");
	}
	@GetMapping("/getAmountDoanhThu")
	private double getTotalAmount() {
		return usedServiceRepo.getAmountDoanhThu();
	}
	@GetMapping("/getListUsedServiceByServiceId")
	private List<UsedService> getListUsedServiceByServiceId(@RequestParam(name="dateStart")java.sql.Date dateStart,
			@RequestParam(name="dateEnd")java.sql.Date dateEnd,
			@RequestParam(name="id")int id) {
		return usedServiceRepo.findByDateStartBetweenAndServiceIdAndStatus(dateStart, dateEnd, id, "SUCCESS");
	}
	@GetMapping("/getListUsedService")
	private List<UsedService> getListUsedService(){
		return usedServiceRepo.findAll();
	}
	@GetMapping("/getListUsedServiceLoc")
	private List<UsedService> getListUsedServiceLoc(
			@RequestParam(name="status")String status){
		return usedServiceRepo.findByStatus(status);
	}
}
//@GetMapping("/save")
//private boolean save() {
//	UsedService usedService = new UsedService();
//	User user = userRepo.findById(13);
//	Service service = serviceRepo.findById(3);
//	usedService.setService(service);
//	usedService.setUser(user);
//	Calendar cal = Calendar.getInstance();
//	usedService.setDateStart(cal.getTime());
//	cal.add(Calendar.MONTH, service.getDuration());
//	usedService.setDateEnd(cal.getTime());
//	usedServiceRepo.save(usedService);
//	return true;
//}
