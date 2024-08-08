package gift.config.web;

import gift.config.jwt.JwtAuthenticationFilter;
import gift.validation.LoginMemberArgumentResolver;
import gift.util.JwtTokenUtil;
import gift.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Autowired
    public WebMvcConfig(JwtTokenUtil jwtTokenUtil, UserRepository userRepository, LoginMemberArgumentResolver loginMemberArgumentResolver) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtAuthenticationFilter(jwtTokenUtil));
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
