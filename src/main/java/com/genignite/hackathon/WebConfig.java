package com.genignite.hackathon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Value("${app.cors}") String origins;
  public void addCorsMappings(CorsRegistry r){ r.addMapping("/api/**").allowedOrigins(origins.split(",")).allowedMethods("GET","POST"); }
}
