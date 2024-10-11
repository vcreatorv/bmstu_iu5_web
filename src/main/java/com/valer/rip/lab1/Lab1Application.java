package com.valer.rip.lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "NET4Y API",
                version = "1.0.0",
                description = "REST API провайдера для составления заявок на подключение и просмотра услуг",
                contact = @Contact(
                        name = "Валерий Нагапетян",
                        email = "valery.nagapetyan@yandex.ru"
                )
        )
)
public class Lab1Application {

	public static void main(String[] args) {
		SpringApplication.run(Lab1Application.class, args);
	}
}
