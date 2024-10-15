package com.valer.rip.lab1.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

// @Configuration
// public class SwaggerConfig {

//     @Bean
//     public OpenAPI customOpenAPI() {
//         return new OpenAPI()
//                 .info(new Info()
//                         .title("NET4Y API")
//                         .version("1.0.0")
//                         .description("REST API провайдера для составления заявок на подключение и просмотра услуг"))
//                 .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
//                 .components(new Components()
//                         .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
//     }

//     private SecurityScheme createAPIKeyScheme() {
//         return new SecurityScheme().type(SecurityScheme.Type.HTTP)
//                 .bearerFormat("JWT")
//                 .scheme("bearer");
//     }
// }

@OpenAPIDefinition(
        info = @Info(
                title = "NET4Y API",
                description = "REST API провайдера для составления заявок на подключение и просмотра услуг.\n\n Полезные ссылки:\n<ul><li>https://github.com/vcreatorv/bmstu_iu5_web</li></ul>", version = "1.0.0",
                contact = @Contact(
                        name = "Валерий Нагапетян",
                        email = "valery.nagapetyan@yandex.ru",
                        url = "https://vk.com/yep_idk"
                )
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenAPIConfig {

}