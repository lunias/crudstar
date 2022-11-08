package com.ethanaa.crudstar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CrudstarApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrudstarApplication.class, args);
	}

}
