package com.rokin.celltracker;

import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

  private static final String HTTPS_SITES_GOOGLE_COM_VIEW_ROKININC = "https://sites.google.com/view/rokininc";

  /**
   * Get product api.
   * @return
   */
  @Bean
  public Docket productApi() {
    return new Docket(DocumentationType.SWAGGER_2).select()
        .apis(RequestHandlerSelectors.basePackage("com.rokin.celltracker.controller"))
        .paths(regex("/api/.*")).build().apiInfo(metaData());
  }

  private ApiInfo metaData() {
    return new ApiInfoBuilder().title("Cell Tracker Api").description("Cell Tracker Api")
        .termsOfServiceUrl(HTTPS_SITES_GOOGLE_COM_VIEW_ROKININC)
        .contact(new Contact("Pervez Sajjad", HTTPS_SITES_GOOGLE_COM_VIEW_ROKININC,
            "contact.rokin@gmail.com"))
        .license("Apache License Version 2.0").licenseUrl(HTTPS_SITES_GOOGLE_COM_VIEW_ROKININC)
        .version("1.0").build();
  }
}
