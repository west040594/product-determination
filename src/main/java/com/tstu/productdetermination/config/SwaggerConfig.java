package com.tstu.productdetermination.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Optional;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error")))
                .build()
                .apiInfo(metadata())
                .useDefaultResponseMessages(false)
                .securitySchemes(Lists.newArrayList(apiKey()))
                .tags(new Tag("ping", "Just a ping"))
                .genericModelSubstitutes(Optional.class);
    }


    private ApiKey apiKey() {
        return new ApiKey("Bearer %token", "Authorization", "Header");
    }

    private ApiInfo metadata() {
        return new ApiInfoBuilder()
                .title("Product Determination API")
                .description("Product Determination Service")
                .version("1.0.0")
                .license("MIT License").licenseUrl("http://opensource/licenses/MIT")
                .contact(new Contact(null, null, "semen223@yandex.ru"))
                .build();
    }
}
