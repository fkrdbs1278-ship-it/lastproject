package com.young04.lastproject.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

                        /* AWS Load Balancer / 운영 상태 확인 */
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        )
                        .permitAll()

                        /* 정적 리소스 */
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/siteadmin-upload/**",
                                "/favicon.ico",
                                "/error"
                        )
                        .permitAll()

                        /* 관리자 기능 */
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        /* 로컬/시연용 테스트 기능은 최소 관리자만 접근 */
                        .requestMatchers(
                                "/test/**",
                                "/testcompany/**"
                        )
                        .hasRole("ADMIN")

                        /* 회원 전용 화면 */
                        .requestMatchers(
                                "/member/mypage",
                                "/member/mypage/**",
                                "/member/edit/**",
                                "/member/withdraw/**",
                                "/member/payments",
                                "/my-reservations"
                        )
                        .authenticated()

                        /* 회원 전용 리뷰 기능 */
                        .requestMatchers(
                                "/reviews/my",
                                "/reviews/write",
                                "/reviews/*/edit",
                                "/reviews/*/delete"
                        )
                        .authenticated()

                        /* 로그인 회원 예약 API */
                        .requestMatchers(
                                "/api/reservations/me",
                                "/api/reservations/me/**"
                        )
                        .authenticated()

                        /* 공개 페이지 */
                        .requestMatchers(
                                "/",
                                "/member/signup",
                                "/member/login",
                                "/member/id-check",
                                "/member/find-id",
                                "/member/find-id/**",
                                "/member/reset-password",
                                "/member/reset-password/**",
                                "/member/phone-verification/**",
                                "/reservation",
                                "/guest-reservation",
                                "/hairstyles",
                                "/hairstyles/**"
                        )
                        .permitAll()

                        /* 공개 리뷰 조회만 허용 */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/reviews",
                                "/reviews/*"
                        )
                        .permitAll()

                        /* 메인 랜덤 헤어스타일 API */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/hairstyles/random"
                        )
                        .permitAll()

                        /* 공개 예약 생성 */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/reservations"
                        )
                        .permitAll()

                        /* 공개 예약 조회/선택용 API */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservations/availability-notices",
                                "/api/reservations/available-times",
                                "/api/reservations/price-preview",
                                "/api/reservations/service-menus",
                                "/api/reservations/hair-styles",
                                "/api/reservations/events"
                        )
                        .permitAll()

                        /* 비회원 예약 조회/변경/취소/사진 */
                        .requestMatchers("/api/reservations/guest/**")
                        .permitAll()

                        /*
                         * 위에서 의도적으로 공개하지 않은 신규 URL은
                         * 기본적으로 로그인 사용자에게만 허용한다.
                         * 기존 anyRequest().permitAll()로 인한 실수성 공개를 방지한다.
                         */
                        .anyRequest()
                        .authenticated()
                )


                /* =================================================
                   로그인 설정
                ================================================= */

                .formLogin(form -> form

                        /*
                         * 우리가 만든 로그인 페이지
                         */
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login/process")
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
                        .logoutSuccessUrl("/")
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
