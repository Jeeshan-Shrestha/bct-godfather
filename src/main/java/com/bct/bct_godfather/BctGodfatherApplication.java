package com.bct.bct_godfather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BctGodfatherApplication {

	public static void main(String[] args) {
		SpringApplication.run(BctGodfatherApplication.class, args);
	}

}
