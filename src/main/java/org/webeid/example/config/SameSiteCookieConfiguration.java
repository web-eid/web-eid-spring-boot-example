package org.webeid.example.config;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SameSiteCookieConfiguration implements WebMvcConfigurer {

    @Bean
    public TomcatContextCustomizer configureSameSiteCookies() {
        return context -> {
            final Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
            cookieProcessor.setSameSiteCookies("strict");
            context.setCookieProcessor(cookieProcessor);
        };
    }
}
