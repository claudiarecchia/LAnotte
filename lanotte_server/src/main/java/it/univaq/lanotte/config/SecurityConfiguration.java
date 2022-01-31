package it.univaq.lanotte.config;

import it.univaq.lanotte.services.MongoUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static java.lang.annotation.RetentionPolicy.SOURCE;


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
//                    .authorizeRequests()
//                    .antMatchers("/adminDashboard").hasRole("ADMIN")
//                    .and()
//                    .formLogin()
//                    // .loginPage("/businessLogin")
//                    // .loginProcessingUrl("/login")
//                    .defaultSuccessUrl("/adminDashboard", true)
//                    .permitAll()
//                    .and()
//                    .logout()
//                    .permitAll();

            http
                    .requestMatcher(new AntPathRequestMatcher("/admin*"))
                    .authorizeRequests()
                    .antMatchers("/admin*")
                    .hasRole("ADMIN")
                    // .hasAuthority("ROLE_ADMIN")
                    // .anyRequest()
                    // .authenticated()

                    .and()
                    .formLogin()
                    .permitAll()
                    // .loginProcessingUrl("/admin_login")
                    // .failureUrl("/loginAdmin?error=loginError")
                    .defaultSuccessUrl("/adminDashboard", true)
                    .and()
                    .logout()
                    //.logoutUrl("/admin_logout")
                    // .logoutSuccessUrl("/protectedLinks")
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true)


                    .and()
                    .exceptionHandling()
                    .accessDeniedPage("/403")

                    .and()
                    .csrf().disable();
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web
                    .ignoring()
                    .antMatchers("/**/*.js", "/**/*.css")
                    .antMatchers("/resources/**", "/static/**", "/css/**", "/img/**")
                    .antMatchers("static/**", "css/**", "img/**");
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
                    //.authorizeRequests()
                    .requestMatcher(new AntPathRequestMatcher("/business*"))
                    .authorizeRequests()
                    .antMatchers("/business**")
                    //.anyRequest()
                    //.authenticated()
                    //.hasAuthority("ROLE_BUSINESS")
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
                    .accessDeniedPage("/error403")


                    .and()
                     .csrf().disable();

        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web
                    .ignoring()
                    .antMatchers("/businessRegistration")
                    .antMatchers("/**/*.js", "/**/*.css")
                    .antMatchers("/resources/**", "/static/**", "/css/**", "/img/**")
                    .antMatchers("static/**", "css/**", "img/**");
        }

    }

    @Override
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(userDetailsService);
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // return NoOpPasswordEncoder.getInstance();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}






