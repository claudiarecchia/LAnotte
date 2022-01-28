package it.univaq.lanotte.config;

import it.univaq.lanotte.services.MongoUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;

import static java.lang.annotation.RetentionPolicy.SOURCE;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    MongoUserDetailService userDetailsService;

    @Configuration
    @Order(1)
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
                    .antMatcher("/admin*")
                    .authorizeRequests()
                    .anyRequest()
                    .hasRole("ADMIN")

                    .and()
                    .formLogin()
                    // .loginPage("/login")
                    // .loginProcessingUrl("/admin_login")
                    // .failureUrl("/loginAdmin?error=loginError")
                    .defaultSuccessUrl("/adminDashboard", true)
                    .and()
                    .logout()
                    //.logoutUrl("/admin_logout")
                    // .logoutSuccessUrl("/protectedLinks")
                    .deleteCookies("JSESSIONID")

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


    @Configuration
    @Order(2)
    public static class SecurityConfigurationBusinesses extends WebSecurityConfigurerAdapter {

        public SecurityConfigurationBusinesses() {
            super();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http

                    .antMatcher("/business*")
                    .authorizeRequests()
                    .anyRequest()
                    .hasRole("USER")

                    .and()
                    .formLogin()
                    .loginPage("/businessLogin").permitAll()
                    .usernameParameter("businessName")
                    .loginProcessingUrl("/businessLogin")

                    //.loginProcessingUrl("/businessLogin")
                    // .failureUrl("/loginUser?error=loginError")
                    .defaultSuccessUrl("/index", true)

                    .and()
                    .logout()
                    .logoutUrl("/businessLogout")
                    .logoutSuccessUrl("/businessLogin")
                    .deleteCookies("JSESSIONID")

                    .and()
                    .exceptionHandling()
                    .accessDeniedPage("/403")

                    .and()
                    .csrf().disable();

        }

//        @Override
//        protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
//            auth.inMemoryAuthentication()
//                    .withUser("user1").password(passwordEncoder().encode("user1")).roles("USER")
//                    .and()
//                    .withUser("user2").password(passwordEncoder().encode("user2")).roles("USER")
//                    .and()
//                    .withUser("admin").password(passwordEncoder().encode("admin")).roles("ADMIN");
//        }


        @Override
        public void configure(WebSecurity web) throws Exception {
            web
                    .ignoring()
                    .antMatchers("/**/*.js", "/**/*.css")
                    .antMatchers("/resources/**", "/static/**", "/css/**", "/img/**")
                    .antMatchers("static/**", "css/**", "img/**");
        }

    }


//    @Override
//    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser("user1").password(passwordEncoder().encode("user1")).roles("USER")
//                .and()
//                .withUser("user2").password(passwordEncoder().encode("user2")).roles("USER")
//                .and()
//                .withUser("admin").password(passwordEncoder().encode("admin")).roles("ADMIN");
//    }



//    @Bean
//    @Override
//    public UserDetailsService userDetailsService() {
//        UserDetails user =
//                User.withDefaultPasswordEncoder()
//                        .username("user")
//                        .password("password")
//                        .roles("USER")
//                        .build();
//
//        return new InMemoryUserDetailsManager(user);
//    }

//    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser("user1").password(passwordEncoder().encode("user1Pass")).roles("USER")
//                .and()
//                .withUser("user2").password(passwordEncoder().encode("user2Pass")).roles("USER")
//                .and()
//                .withUser("admin").password(passwordEncoder().encode("admin")).roles("ADMIN");
//    }
//
    @Bean
    public static PasswordEncoder passwordEncoder() {
        // return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(userDetailsService);
    }

}






