package com.tstu.productdetermination;

import com.tstu.productdetermination.service.ProductClassificationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ProductdeterminationApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProductdeterminationApplication.class, args);
	}
}
