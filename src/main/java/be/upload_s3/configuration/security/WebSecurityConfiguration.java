package be.upload_s3.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true,proxyTargetClass=true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JWTUtil jwtTokenUtil;
    private final JwtUserDetailsService jwtUserDetailsService;
    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    public WebSecurityConfiguration(JWTUtil jwtUtil, JwtUserDetailsService jwtUserDetailsService) {
        this.jwtTokenUtil = jwtUtil;
        this.jwtUserDetailsService = jwtUserDetailsService;
    }
    @Bean
    public JWTTokenFilter authenticationJwtTokenFilter() {
        return new JWTTokenFilter();
    }

//  @Autowired
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(PasswordEncoder());
    }

    @Bean
//  @Autowired
    public PasswordEncoder PasswordEncoder () {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().antMatchers("/api/auth/**").permitAll()
                .antMatchers("/swagger-ui.html","/swagger-ui/**","/v3/**").permitAll()
                .anyRequest().authenticated().and().cors().configurationSource(corsConfigurationSource);
        httpSecurity.headers().frameOptions().sameOrigin().cacheControl();
        httpSecurity.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }

}