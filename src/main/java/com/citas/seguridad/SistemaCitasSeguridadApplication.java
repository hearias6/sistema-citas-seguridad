package com.citas.seguridad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EntityScan({"com.citas.libreria.entidades.models.entity"})
@EnableFeignClients
public class SistemaCitasSeguridadApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaCitasSeguridadApplication.class, args);
	}

}
