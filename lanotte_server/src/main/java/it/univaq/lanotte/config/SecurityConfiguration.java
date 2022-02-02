package it.univaq.lanotte.config;

import it.univaq.lanotte.services.MongoUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;



@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    MongoUserDetailService userDetailsService;

    @EnableWebSecurity
    @Configuration
    @Order(2)
    public static class App1ConfigurationAdapter extends WebSecurityConfigurerAdapter {
        public App1ConfigurationAdapter() {
            super();
        }
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatcher(new AntPathRequestMatcher("/admin*"))
                    .authorizeRequests()
                    .antMatchers("/admin*")
                    .hasRole("ADMIN")

                    .and()
                    .formLogin()
                    .permitAll()
                    .defaultSuccessUrl("/adminDashboard", true)
                    .and()
                    .logout()

                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true)

                    .and()
                    .exceptionHandling()

                    .and()
                    .csrf().disable();
        }
    }

    @EnableWebSecurity
    @Configuration
    @Order(1)
    public static class SecurityConfigurationBusinesses extends WebSecurityConfigurerAdapter {

        public SecurityConfigurationBusinesses() {
            super();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatcher(new AntPathRequestMatcher("/business*"))
                    .authorizeRequests()
                    .antMatchers("/business**")
                    .hasRole("BUSINESS")

                    .and()

                    .formLogin()
                    .loginPage("/businessLogin").permitAll()
                    .usernameParameter("businessName")
                    .loginProcessingUrl("/businessLogin")
                    .defaultSuccessUrl("/businessDashboard", true)

                    .and()
                    .logout()
                    .clearAuthentication(true)
                    .logoutUrl("/businessLogout")
                    .logoutSuccessUrl("/businessLogin")
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true)

                    .and()
                    .exceptionHandling()

                    .and()
                     .csrf().disable();
        }

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/api/**")
                .antMatchers("/businessRegistration")
                .antMatchers("/**/*.js", "/**/*.css")
                .antMatchers("/resources/**", "/static/**", "/css/**", "/img/**")
                .antMatchers("static/**", "css/**", "img/**");
    }

    @Override
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(userDetailsService);
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}






