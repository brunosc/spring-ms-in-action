package com.thoughtmechanix.licenses;

import com.thoughtmechanix.licenses.security.OAuth2FeignRequestInterceptor;
import feign.RequestInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@RefreshScope
@EnableFeignClients
@EnableCircuitBreaker
@EnableResourceServer
public class LicensingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LicensingServiceApplication.class, args);
	}

	@Bean
	public RequestInterceptor getUserFeignClientInterceptor() {
		return new OAuth2FeignRequestInterceptor();
	}

//	@Bean
//	public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext,
//												 OAuth2ProtectedResourceDetails details) {
//		return new OAuth2RestTemplate(details, oauth2ClientContext);
//	}

//	@LoadBalanced
//	@Bean
//	public RestTemplate getRestTemplate(){
//		RestTemplate template = new RestTemplate();
//		List interceptors = template.getInterceptors();
//		if (interceptors==null){
//			template.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
//		}
//		else{
//			interceptors.add(new UserContextInterceptor());
//			template.setInterceptors(interceptors);
//		}
//
//		return template;
//	}
}
