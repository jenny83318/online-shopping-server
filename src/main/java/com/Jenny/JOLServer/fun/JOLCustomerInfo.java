package com.Jenny.JOLServer.fun;

import java.lang.reflect.Field;
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
import com.Jenny.JOLServer.dao.CustomerInfoDao;
import com.Jenny.JOLServer.model.Customer;
import com.Jenny.JOLServer.model.Request;
import com.Jenny.JOLServer.tool.CustomException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Request JSON
// 0:查全部、1:查單一、2:新增、3:編輯、4刪除
{
    "account":"jenny83318",
    "type":0,
    "fun":"JOLCustomerInfo",
    "body":{
    "account": "abc1235",
    "password": "abc8288383",
    "name": "林曉涵",
    "phone": "0915526665",
    "email": "anfj@gmail.com",
    "status": 1
}
}
 */
@Service
public class JOLCustomerInfo {
	private static final Logger log = LoggerFactory.getLogger(JOLCustomerInfo.class);

	@Autowired
	private CustomerInfoDao customerDao;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BODY {
		private String password;
		private String name;
		private String phone;
		private String email;
		private String city;
		private String district;
		private String address;
		private Integer status;
		private String tokenExpired;
	}

	@Data
	@Builder
	public static class OUT {
		private Integer code;
		private String msg;
		private List<Customer> custList;
	}

	protected void check(Request req) {
		Field[] fields = BODY.builder().build().getClass().getDeclaredFields();
		for (Field field : fields) {
			String key = field.getName();
			Object value = req.getBody().get(key);
			if (("UPDATE".equals(req.getType()) || ("ADD".equals(req.getType())&& !"tokenExpired".equals(key))) && value == null ) {
				log.info("key:{}", key);
				log.info("value:{}", value);
				log.error("PARAM NOT FOUND: {}", key);
				throw new CustomException("PARAM NOT FOUND: " + key);
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
		List<Customer> custList = new ArrayList<>();
		Type type = Type.getType(req.getType());
		switch (Objects.requireNonNull(type)) {
		case ALL:
			custList = customerDao.findAll();
			break;
		case SELECT:
			Customer c = customerDao.findByAccount(req.getAccount());
			custList.add(c == null ? Customer.builder().build() : c);
			break;
		case ADD:
		case UPDATE:
			Customer customer = customerDao.findByAccount(req.getAccount());
			Customer customerEmail = customerDao.findByEmail(body.getEmail());
			if ("ADD".equals(type.getTypeName()) && customer != null) {
				return OUT.builder().custList(custList).code(HttpStatus.BAD_REQUEST.value()).msg("此帳號已註冊過，請換個帳號名稱試試")
						.build();
			} else if ("ADD".equals(type.getTypeName()) && customerEmail != null) {
				return OUT.builder().custList(custList).code(HttpStatus.BAD_REQUEST.value()).msg("此email已註冊，請換個email試試")
						.build();
			} else if ("UPDATE".equals(type.getTypeName()) && customer == null) {
				throw new CustomException("此帳號不存在，無法更新");
			} else {
				Customer newCustomer = Customer.builder().account(req.getAccount()).address(body.address)
						.email(body.getEmail()).name(body.getName()).password(body.getPassword()).phone(body.getPhone())
						.city(body.getCity()).district(body.getDistrict()).status(body.getStatus())
						.token(req.getToken()).tokenExpired(body.getTokenExpired()).build();
				Customer updCustomer = customerDao.save(newCustomer);
                custList.add(updCustomer);
            }
			break;
		case DELETE:
			customerDao.deleteByAccount(req.getAccount());
			break;
		default:
			break;
		}
		log.info("customerList:{}", custList);
		return OUT.builder().custList(custList).code(HttpStatus.OK.value()).msg("execute success.").build();
	}

}
