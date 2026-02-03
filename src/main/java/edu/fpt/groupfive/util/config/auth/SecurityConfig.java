package edu.fpt.groupfive.util.config.auth;

import edu.fpt.groupfive.service.CustomerUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomerUserDetailsService userDetailsService;
    private final CustomerAuthenticationFailureHandler customerAuthenticationFailureHandler;

    // các url dc truy cập tự do
    private final String[] WHITE_LIST = {"/auth/login","/css/**"};

    @Bean
    public PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder(10);
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
//                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
//                        .requestMatchers(WHITE_LIST)
//                        .permitAll()
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/dept-manager/**").hasAnyRole("DEPARTMENT_MANAGER", "ADMIN")
//                        .requestMatchers("/director/**").hasAnyRole("DIRECTOR", "ADMIN")
//                        .requestMatchers("/purchase-staff/**").hasAnyRole("PURCHASE_STAFF", "ADMIN")
//                        .requestMatchers("/asset-manager/**").hasAnyRole("ASSET_MANAGER", "ADMIN")
//                        .requestMatchers("/warehouse/**").hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
//                        .anyRequest()
//                        .authenticated())
                .authorizeHttpRequests(auth ->
                auth.anyRequest().permitAll());

//                .authenticationProvider(authenticationProvider())
//                .formLogin(f -> f.loginPage("/auth/login")
//                        .loginProcessingUrl("/login")
//                        .defaultSuccessUrl("/home", true)
//                        .failureHandler(customerAuthenticationFailureHandler)
//                        .permitAll())

//
//                .logout(l -> l.logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout=true")
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                        .permitAll())
//                .sessionManagement(s -> s.sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession).maximumSessions(1).maxSessionsPreventsLogin(false));

        return httpSecurity.build();
    }

//    @Bean
//    public AuthenticationProvider authenticationProvider(){
//
//    DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//
//    daoAuthenticationProvider.setUserDetailsService(userDetailsService);
//    daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
//
//    // hiển thị lỗi
//    daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
//
//    return daoAuthenticationProvider;
//    }

}
