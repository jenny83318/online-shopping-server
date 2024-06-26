package com.Jenny.JOLServer.fun;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.Jenny.JOLServer.common.Type;
import com.Jenny.JOLServer.dao.OrderInfoDao;
import com.Jenny.JOLServer.model.Order;
import com.Jenny.JOLServer.model.Request;
import com.Jenny.JOLServer.tool.CustomException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * { "fun":"JOLOrderInfo", "account":"peter1234", "body":{ "type":"ADD",
 * "totalAmt":3630, "shipNo":"263622", "isOrderDetail":0, "status":"prepare"
 * }
 * 
 */
@Service
public class JOLOrderInfo {
	private static final Logger log = LoggerFactory.getLogger(JOLOrderInfo.class);

	@Autowired
	private OrderInfoDao orderDao;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BODY {
		private int orderNo;
		private String email;
		private int totalAmt;
		private String status;
		private String deliveryWay;
		private String deliveryNo;
		private String orderName;
		private String orderPhone;
		private String orderCity;
		private String orderDistrict;
		private String orderAddress;
		private String sendName;
		private String sendPhone;
		private String sendCity;
		private String sendDistrict;
		private String sendAddress;
		private String vehicle;
		private String vehicleType;
		private String payBy;
	}

	@Data
	@Builder
	public static class OUT {
		@Builder.Default
		private int code = 0;
		@Builder.Default
		private String msg = "";
		@Builder.Default
		private List<Order> orderList = new ArrayList<>();
	}

	protected void check(Request req)  {
		Field[] fields =  BODY.builder().build().getClass().getDeclaredFields();
		for (Field field : fields) {
			String key = field.getName();
			Object value = req.getBody().get(key);
			if ("UPDATE".equals(req.getType()) && "orderNo".equals(key) && value == null) {
					log.error("PARAM NOT FOUND: orderNo");
					throw new CustomException("PARAM NOT FOUND: orderNo");
			}
			if (("ADD".equals(req.getType()) && !"orderNo".equals(key)) || "UPDATE".equals(req.getType())) {
				if (value == null) {
					log.error("PARAM NOT FOUND: {}",key);
					throw new CustomException("PARAM NOT FOUND: " + key);
				}
			}
			if("OTHER".equals(req.getType())) {
				if(("orderNo".equals(key) || "status".equals(key)) && value == null) {
					log.error("PARAM NOT FOUND: {}",key);
					throw new CustomException("PARAM NOT FOUND: " + key);
				}
			}
		}
	}

	public BODY parser(Map<String, Object> map) {
		ModelMapper modelMapper = new ModelMapper();
		return modelMapper.map(map, BODY.class);
	}

	public OUT doProcess(Request req) {
		check(req);
		BODY body = parser(req.getBody());
		log.info("body:{}", body.toString());
		Type type = Type.getType(req.getType());
		List<Order> dataList = new ArrayList<>();
		switch (Objects.requireNonNull(type)) {
		case SELECT:
			dataList = orderDao.findByAccountOrderByOrderNoDesc(req.getAccount());
			break;
		case ADD:
		case UPDATE:
			Order o = orderDao.save(bodyToDB(body, req));
			dataList.add(o);
			break;
		case OTHER:
			Order order  = orderDao.findByOrderNoAndAccount(body.getOrderNo(), req.getAccount());
			order.setStatus(body.getStatus());
			orderDao.save(order);
		default:
			break;
		}
		return OUT.builder().code(HttpStatus.OK.value()).msg("execute success.").orderList(dataList).build();
	}

	public Order bodyToDB(BODY body, Request req) {
		ZonedDateTime taipeiTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Asia/Taipei"));
		return Order.builder().orderNo(body.getOrderNo()).account(req.getAccount()).email(body.getEmail())
				.totalAmt(body.getTotalAmt()).orderTime(taipeiTime).status(body.getStatus()).deliveryWay(body.getDeliveryWay())
				.deliveryNo(body.getDeliveryNo()).orderName(body.getOrderName()).orderCity(body.getOrderCity())
				.orderPhone(body.getOrderPhone()).orderDistrict(body.getOrderDistrict())
				.orderAddress(body.getOrderAddress()).sendName(body.getSendName()).sendCity(body.getSendCity())
				.sendPhone(body.getSendPhone()).sendDistrict(body.getSendDistrict()).sendAddress(body.getSendAddress())
				.updateDt(taipeiTime).vehicle(body.getVehicle()).sendPhone(body.getSendPhone())
				.vehicleType(body.getVehicleType()).payBy(body.getPayBy()).build();
	}

}
