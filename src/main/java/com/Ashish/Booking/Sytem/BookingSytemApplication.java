package com.Ashish.Booking.Sytem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BookingSytemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingSytemApplication.class, args);
		System.out.println("application started");
	}

}
