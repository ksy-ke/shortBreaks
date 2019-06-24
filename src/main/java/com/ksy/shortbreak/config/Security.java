package com.ksy.shortbreak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@EnableWebSecurity
public @Configuration class Security extends WebSecurityConfigurerAdapter {
    private static final String USER_ROLE_VALUE = "USER";
    public static final GrantedAuthority USER_ROLE = new SimpleGrantedAuthority("ROLE_" + USER_ROLE_VALUE);

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("ksy").password("{noop}123").roles(USER_ROLE_VALUE)
                .and()
                .withUser("bob").password("{noop}123").roles(USER_ROLE_VALUE)
                .and()
                .withUser("admin").password("{noop}admin").roles(USER_ROLE_VALUE, "ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                .and().csrf().disable()
                .formLogin().and()
                .logout().logoutSuccessUrl("/").invalidateHttpSession(true).deleteCookies("JSESSIONID");
    }
}
