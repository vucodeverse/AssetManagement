package edu.fpt.groupfive.util.config.auth;

import edu.fpt.groupfive.service.CustomerUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomerUserDetailsService userDetailsService;
        private final CustomerAuthenticationFailureHandler customerAuthenticationFailureHandler;
        private final CustomerAuthenticationSuccessHandler customerAuthenticationSuccessHandler;

        @Value("${app.security.enabled}")
        private boolean securityEnable;

        // các url dc truy cập tự do
        private final String[] WHITE_LIST = { "/auth/login", "/static/**" };

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(10);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

                // tắt security
                if (!securityEnable) {
                        httpSecurity
                                        .csrf(csrf -> csrf.disable())
                                        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
                        return httpSecurity.build();
                }

                // bật bth
                httpSecurity
                                .authorizeHttpRequests(
                                                authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
                                                                .requestMatchers(WHITE_LIST)
                                                                .permitAll()
                                                                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                                                                .requestMatchers("/dept-manager/**")
                                                                .hasAnyAuthority("DEPARTMENT_MANAGER", "ADMIN")
                                                                // Domain URLs: không gắn với role
                                                                .requestMatchers("/purchases/**")
                                                                .hasAnyAuthority("ASSET_MANAGER", "DIRECTOR",
                                                                                "PURCHASE_STAFF", "ASSET-MANAGER",
                                                                                "ADMIN")
                                                                .requestMatchers("/quotations/**")
                                                                .hasAnyAuthority("PURCHASE_STAFF", "DIRECTOR", "ADMIN")
                                                                .requestMatchers("/purchase-orders/**")
                                                                .hasAnyAuthority("PURCHASE_STAFF", "DIRECTOR", "ADMIN")
                                                                // Role dashboards: gắn với role
                                                                .requestMatchers("/director/**")
                                                                .hasAnyAuthority("DIRECTOR", "ADMIN")
                                                                .requestMatchers("/purchase-staff/**")
                                                                .hasAnyAuthority("PURCHASE_STAFF", "ADMIN")
                                                                .requestMatchers("/asset-manager/**")
                                                                .hasAnyAuthority("ASSET_MANAGER", "ADMIN")
                                                                .requestMatchers("/wh/**")
                                                                .hasAnyAuthority("WAREHOUSE_STAFF")
                                                                .anyRequest()
                                                                .authenticated())
                                .authenticationProvider(authenticationProvider())
                                .formLogin(f -> f.loginPage("/auth/login")
                                                .loginProcessingUrl("/login")
                                                .successHandler(customerAuthenticationSuccessHandler)
                                                .failureHandler(customerAuthenticationFailureHandler)
                                                .permitAll())
                                .logout(l -> l.logoutUrl("/logout")
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                                                .logoutSuccessUrl("/auth/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .sessionManagement(
                                                s -> s.sessionFixation(
                                                                SessionManagementConfigurer.SessionFixationConfigurer::newSession)
                                                                .maximumSessions(1).maxSessionsPreventsLogin(false));

                return httpSecurity.build();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {

                DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

                daoAuthenticationProvider.setUserDetailsService(userDetailsService);
                daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

                // hiển thị lỗi
                daoAuthenticationProvider.setHideUserNotFoundExceptions(false);

                return daoAuthenticationProvider;
        }

}
