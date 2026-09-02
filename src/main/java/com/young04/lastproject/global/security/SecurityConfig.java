package com.young04.lastproject.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {


    /* =========================================================
       로그인 인증 처리

       CustomUserDetailsService
               +
       BCrypt PasswordEncoder

       를 연결한다.
    ========================================================= */

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        customUserDetailsService
                );

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }


    /* =========================================================
       Security 설정
    ========================================================= */

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler
    ) throws Exception {

        /*
         * Thymeleaf에서
         *
         * ${_csrf.token}
         * ${_csrf.headerName}
         *
         * 형태로 CSRF 토큰을 사용할 수 있게 한다.
         */
        CsrfTokenRequestAttributeHandler csrfHandler =
                new CsrfTokenRequestAttributeHandler();

        csrfHandler.setCsrfRequestAttributeName("_csrf");


        http

                /* =================================================
                   AuthenticationProvider 등록
                ================================================= */

                .authenticationProvider(authenticationProvider)


                /* =================================================
                   CSRF
                ================================================= */

                .csrf(csrf -> csrf
                        .csrfTokenRequestHandler(csrfHandler)
                )


                /* =================================================
                   URL 권한 설정
                ================================================= */

                .authorizeHttpRequests(auth -> auth


                        /*
                         * 로그인하지 않아도 접근 가능
                         */
                        .requestMatchers(
                                "/",
                                "/member/signup",
                                "/member/login",

                                "/reservation",
                                "/guest-reservation",

                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",

                                "/error"
                        )
                        .permitAll()


                        /*
                         * 비회원도 사용할 수 있는 예약 API
                         */
                        .requestMatchers(
                                "/api/reservations/available-times",
                                "/api/reservations/service-menus",
                                "/api/reservations/hair-styles",
                                "/api/reservations/guest/**"
                        )
                        .permitAll()


                        /*
                         * 관리자 전용
                         *
                         * /admin/api/reservations
                         * /admin/api/business-hours
                         * /admin/api/holidays
                         * /admin/api/availability-blocks
                         *
                         * 모두 여기 포함된다.
                         */
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")


                        /*
                         * 로그인 회원 예약
                         */
                        .requestMatchers(
                                "/api/reservations/me",
                                "/api/reservations/me/**"
                        )
                        .authenticated()


                        /*
                         * 기존 회원 전용
                         */
                        .requestMatchers(
                                "/mypage/**",
                                "/member/edit/**",
                                "/member/withdraw/**",
                                "/my-reservations"
                        )
                        .authenticated()


                        /*
                         * 현재 아직 개발 중인 나머지 페이지는 허용
                         *
                         * 기능이 추가될 때 권한을 조금씩 강화한다.
                         */
                        .anyRequest()
                        .permitAll()
                )


                /* =================================================
                   로그인 설정
                ================================================= */

                .formLogin(form -> form

                        /*
                         * 우리가 만든 로그인 페이지
                         */
                        .loginPage("/member/login")


                        /*
                         * 로그인 form이 POST하는 주소
                         *
                         * Controller에서 처리하지 않는다.
                         * Spring Security가 처리한다.
                         */
                        .loginProcessingUrl(
                                "/member/login/process"
                        )


                        /*
                         * 아이디 input name
                         */
                        .usernameParameter("memberId")


                        /*
                         * 비밀번호 input name
                         */
                        .passwordParameter("password")


                        /*
                         * 로그인 성공
                         *
                         * 일단 메인 페이지로 이동
                         */
                        .successHandler(loginSuccessHandler)


                        /*
                         * 로그인 실패
                         */
                        .failureHandler(loginFailureHandler)

                        .permitAll()
                )


                /* =================================================
                   로그아웃 설정
                ================================================= */

                .logout(logout -> logout

                        /*
                         * 로그아웃 요청 주소
                         *
                         * POST 방식으로 요청할 예정
                         */
                        .logoutUrl("/member/logout")


                        /*
                         * 로그아웃 후 로그인 페이지
                         */
                        .logoutSuccessUrl(
                                "/member/login?logout"
                        )


                        /*
                         * 서버 세션 삭제
                         */
                        .invalidateHttpSession(true)


                        /*
                         * JSESSIONID 쿠키 삭제
                         */
                        .deleteCookies("JSESSIONID")

                        .permitAll()
                )


                /* =================================================
                   HTTP Basic 로그인 비활성화

                   전에 브라우저에서 뜨던
                   Username / Password 팝업 방지
                ================================================= */

                .httpBasic(
                        AbstractHttpConfigurer::disable
                );


        return http.build();
    }
}