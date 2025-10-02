package com.usds.regulations.config;

import com.usds.regulations.security.DDOSProtectionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Autowired
    private DDOSProtectionFilter ddosProtectionFilter;

    @Bean
    public FilterRegistrationBean<DDOSProtectionFilter> rateLimitingFilter() {
        FilterRegistrationBean<DDOSProtectionFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(ddosProtectionFilter);
        registrationBean.addUrlPatterns("/api/*"); // Apply to all API endpoints
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Run before other filters
        registrationBean.setName("DDOSProtectionFilter");
        
        return registrationBean;
    }
}