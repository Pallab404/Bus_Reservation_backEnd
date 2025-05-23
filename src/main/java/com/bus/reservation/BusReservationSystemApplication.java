package com.bus.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BusReservationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusReservationSystemApplication.class, args);
	}

}
