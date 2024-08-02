
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package userguide.springboot;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.Filter;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;

import org.springframework.context.annotation.Bean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import userguide.springboot.security.webservices.WSLoginFilter;
import userguide.springboot.security.webservices.JWTAuthenticationFilter;
import userguide.springboot.security.webservices.JWTAuthenticationProvider;
import userguide.springboot.security.webservices.HTTPPostOnlyRejectionFilter;
import userguide.springboot.security.webservices.RequestAndResponseValidatorFilter;
import userguide.springboot.security.webservices.RestAuthenticationEntryPoint;

@SpringBootApplication
@EnableAutoConfiguration
@Configuration
public class Axis2Application extends SpringBootServletInitializer {

    private static final Logger logger = LogManager.getLogger(Axis2Application.class);
    public static volatile boolean isRunning = false;

    @Configuration
    @EnableWebSecurity
    @Order(1)
    @PropertySource("classpath:application.properties")
    public static class SecurityConfigurationTokenWebServices {
        private static final Logger logger = LogManager.getLogger(SecurityConfigurationTokenWebServices.class);

        public SecurityConfigurationTokenWebServices() {
        }

        class AnonRequestMatcher implements RequestMatcher {
            
            @Override
            public boolean matches(HttpServletRequest request) {
                String logPrefix = "AnonRequestMatcher.matches , ";
                boolean result = request.getRequestURI().contains(
                        "/services/loginService");
                logger.debug(logPrefix
                        + "inside AnonRequestMatcher.matches, will return result: "
                        + result + " , on request.getRequestURI() : "
                        + request.getRequestURI() + " , request.getMethod() : "
                        + request.getMethod());
                return result;
            }
            
        }

        class AuthorizationFailHandler implements AccessDeniedHandler {

            @Override
            public void handle(final HttpServletRequest request, final HttpServletResponse response, final AccessDeniedException accessDeniedException)
                    throws IOException, ServletException {
                String logPrefix = "AuthorizationFailHandler.handle() , ";
                response.setContentType("application/json");
                try (PrintWriter writer = response.getWriter()) {
                    logger.error(logPrefix + "found error: " + accessDeniedException.getMessage());
                    writer.write("{\"msg\":\" Access Denied\"}");
                }
            }
        }

	// this is about where Spring SEC HTTPInterceptor would go however it was too flaky and inflexible for this use case
        class SecureResouceMetadataSource implements FilterInvocationSecurityMetadataSource {

            @Override
            public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
                String logPrefix = "SecureResouceMetadataSource.getAttributes , ";

                final HttpServletRequest request = ((FilterInvocation) object).getRequest();
                final String url = request.getRequestURI();
                final String method = request.getMethod();
                    
                String[] roles = new String[] { String.format("%s|%s", url, method) };
                logger.debug(logPrefix + "found roles: " + Arrays.toString(roles));
                return SecurityConfig.createList(roles);
            }

            @Override
            public Collection<ConfigAttribute> getAllConfigAttributes() {
                String logPrefix = "SecureResouceMetadataSource.getAllConfigAttributes , ";
                logger.debug(logPrefix + "returning ROLE_USER ...");
                List<ConfigAttribute> attrs = SecurityConfig.createList("ROLE_USER");
                return attrs;
            }

            /**
             * true if the implementation can process the indicated class
             */
            @Override
            public boolean supports(final Class<?> clazz) {
                return true;
            }

        }
        
        class StatelessSecurityContextRepository extends HttpSessionSecurityContextRepository {

            @Override
            public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
                String logPrefix = "StatelessSecurityContextRepository.loadContext , ";
                logger.debug(logPrefix + "inside loadContext() ... invoking createEmptyContext()");
                return SecurityContextHolder.createEmptyContext();
            }

            @Override
            public void saveContext(SecurityContext context,
                HttpServletRequest request, HttpServletResponse response) {
                String logPrefix = "StatelessSecurityContextRepository.saveContext , ";
                logger.debug(logPrefix + "inside saveContext() ... no action taken");
            }

            @Override
            public boolean containsContext(final HttpServletRequest request) {
                String logPrefix = "StatelessSecurityContextRepository.containsContext , ";
                logger.debug(logPrefix + "inside containsContext() ... returning false");
                return false;
            }

        }

        class GenericAccessDecisionManager implements AccessDecisionManager {

            @Override
            public void decide(final Authentication authentication, final Object object, final Collection<ConfigAttribute> configAttributes)
                    throws AccessDeniedException, InsufficientAuthenticationException {
        
                /* TODO role based auth can go here 
                boolean allowAccess = false;
        
                for (final GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
        
                    for (final ConfigAttribute attribute : configAttributes) {
                        allowAccess = attribute.getAttribute().equals(grantedAuthority.getAuthority());
                        if (allowAccess) {
                            break;// this loop
                        }
                    }
        
                }
        
                if (!allowAccess) {
                    logger.warn("Throwing access denied exception");
                    throw new AccessDeniedException("Access is denied");
                }
                */
            }

            @Override
            public boolean supports(final ConfigAttribute attribute) {
                return true;
            }
        
            @Override
            public boolean supports(final Class<?> clazz) {
                return true;
            }
        }

        @Autowired
        private JWTAuthenticationProvider jwtAuthenticationProvider;

        @Autowired
        private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

        @Autowired
        public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(jwtAuthenticationProvider);
        }
        
        @Bean
        WSLoginFilter wsLoginFilter() throws Exception {
          final WSLoginFilter filter = new WSLoginFilter();
          return filter;
        }

        @Bean
        JWTAuthenticationFilter jwtAuthenticationFilter() throws Exception {
          final JWTAuthenticationFilter filter = new JWTAuthenticationFilter();
          return filter;
        }
      
        @Bean
        HTTPPostOnlyRejectionFilter httpPostOnlyRejectionFilter() throws Exception {
          final HTTPPostOnlyRejectionFilter filter = new HTTPPostOnlyRejectionFilter();
          return filter;
        }

        @Bean
        public ProviderManager authenticationManager() {
            return new ProviderManager(Arrays.asList(jwtAuthenticationProvider));
        }

        public ExceptionTranslationFilter exceptionTranslationFilter() {
            final ExceptionTranslationFilter exceptionTranslationFilter = new ExceptionTranslationFilter(restAuthenticationEntryPoint);
            exceptionTranslationFilter.setAccessDeniedHandler(new AuthorizationFailHandler());
            return exceptionTranslationFilter;
        }
    
        @Bean
        public SecureResouceMetadataSource secureResouceMetadataSource() {
            return new SecureResouceMetadataSource();// gives allowed roles
        }

        @Bean
        AffirmativeBased accessDecisionManager() {
            List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<>();
            voters.add(new RoleVoter());
            AffirmativeBased decisionManager = new AffirmativeBased(voters);
            decisionManager.setAllowIfAllAbstainDecisions(false);
            return decisionManager;
        }

        @Bean
        public GenericAccessDecisionManager genericAccessDecisionManager() {
            return new GenericAccessDecisionManager();
        }
   
	// Note: This nethod is invoked only on token validation after a successful login 
        // See https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html
	// AuthorizationFilter supersedes FilterSecurityInterceptor. To remain backward compatible, FilterSecurityInterceptor remains the default.
        public FilterSecurityInterceptor filterSecurityInterceptor() throws Exception {
            final FilterSecurityInterceptor filterSecurityInterceptor = new FilterSecurityInterceptor();
            filterSecurityInterceptor.setAuthenticationManager(authenticationManager());
            filterSecurityInterceptor.setAccessDecisionManager(genericAccessDecisionManager());
            filterSecurityInterceptor.setSecurityMetadataSource(secureResouceMetadataSource());
            return filterSecurityInterceptor;
        }

        @Bean
        public StatelessSecurityContextRepository statelessSecurityContextRepository() {
            return new StatelessSecurityContextRepository();
        }

        @Bean
        public Filter sessionManagementFilter() {
            StatelessSecurityContextRepository repo = statelessSecurityContextRepository();
            repo.setAllowSessionCreation(false); 
            SessionManagementFilter filter = new SessionManagementFilter(repo);
            return filter;
        }

        @Bean
        public HeaderWriterFilter headerWriterFilter() {
            HeaderWriter headerWriter = new HeaderWriter() {
                public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
                    response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
                    response.setHeader("Expires", "0");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("X-Frame-Options", "SAMEORIGIN");
                    response.setHeader("X-XSS-Protection", "1; mode=block");
                    response.setHeader("x-content-type-options", "nosniff");
                }
            };
            List<HeaderWriter> headerWriterFilterList = new ArrayList<HeaderWriter>();
            headerWriterFilterList.add(headerWriter);
            HeaderWriterFilter headerFilter = new HeaderWriterFilter(headerWriterFilterList);
            return headerFilter;
        }
      
        // these two chains are a binary choice. 
        // A login url will match, otherwise invoke jwtAuthenticationFilter

        @Bean(name = "springSecurityFilterChainLogin")
	@Order(1)
        public SecurityFilterChain springSecurityFilterChainLogin() throws ServletException, Exception {
            String logPrefix = "GenericAccessDecisionManager.springSecurityFilterChain , ";
            logger.debug(logPrefix + "inside main filter config ...");

            SecurityFilterChain securityFilterChain1 = new DefaultSecurityFilterChain(new AnonRequestMatcher(), headerWriterFilter(), httpPostOnlyRejectionFilter(), requestAndResponseValidatorFilter(), wsLoginFilter(), sessionManagementFilter());

            return securityFilterChain1;
        }

        @Bean(name = "springSecurityFilterChainToken")
        public SecurityFilterChain springSecurityFilterChainToken() throws ServletException, Exception {
            String logPrefix = "GenericAccessDecisionManager.springSecurityFilterChain , ";
            logger.debug(logPrefix + "inside main filter config ...");

            SecurityFilterChain securityFilterChain2 = new DefaultSecurityFilterChain(new NegatedRequestMatcher(new AnonRequestMatcher()), headerWriterFilter(), httpPostOnlyRejectionFilter(), requestAndResponseValidatorFilter(), jwtAuthenticationFilter(), sessionManagementFilter(), exceptionTranslationFilter(), filterSecurityInterceptor());

            return securityFilterChain2;
        }

        /**
         * Disable Spring boot automatic filter registration since we are using FilterChainProxy.
         */
        @Bean
        FilterRegistrationBean disableWSLoginFilterAutoRegistration(final WSLoginFilter wsLoginFilter) {
            String logPrefix = "GenericAccessDecisionManager.disableWSLoginFilterAutoRegistration , ";
            logger.debug(logPrefix + "executing registration.setEnabled(false) on wsLoginFilter ...");
            final FilterRegistrationBean registration = new FilterRegistrationBean(wsLoginFilter);
            registration.setEnabled(false);
            return registration;
        }

        /**
         * Disable Spring boot automatic filter registration since we are using FilterChainProxy.
         */
        @Bean
        FilterRegistrationBean disableJWTAuthenticationFilterAutoRegistration(final JWTAuthenticationFilter filter) {
            String logPrefix = "GenericAccessDecisionManager.disableJWTAuthenticationFilterAutoRegistration , ";
            logger.debug(logPrefix + "executing registration.setEnabled(false) on JWTAuthenticationFilter ...");
            final FilterRegistrationBean registration = new FilterRegistrationBean(filter);
            registration.setEnabled(false);
            return registration;
        }
      
        /**
         * Disable Spring boot automatic filter registration since we are using FilterChainProxy.
         */
        @Bean
        FilterRegistrationBean disableHTTPPostOnlyRejectionFilterAutoRegistration(final HTTPPostOnlyRejectionFilter filter) {
            String logPrefix = "GenericAccessDecisionManager.disableHTTPPostOnlyRejectionFilterAutoRegistration , ";
            logger.debug(logPrefix + "executing registration.setEnabled(false) on HTTPPostOnlyRejectionFilter ...");
            final FilterRegistrationBean registration = new FilterRegistrationBean(filter);
            registration.setEnabled(false);
            return registration;
        }

        /**
         * Disable Spring boot automatic filter registration since we are using FilterChainProxy.
         */
        @Bean
        FilterRegistrationBean disableRequestAndResponseValidatorFilterAutoRegistration(final RequestAndResponseValidatorFilter filter) {
            String logPrefix = "GenericAccessDecisionManager.disableRequestAndResponseValidatorFilterAutoRegistration , ";
            logger.debug(logPrefix + "executing registration.setEnabled(false) on RequestLoggingFilter ...");
            final FilterRegistrationBean registration = new FilterRegistrationBean(filter);
            registration.setEnabled(false);
            return registration;
        }
        
        @Bean
        public RequestAndResponseValidatorFilter requestAndResponseValidatorFilter() {
            RequestAndResponseValidatorFilter filter = new RequestAndResponseValidatorFilter();
            return filter;
        }
        
	/*
        @Bean()
        FilterRegistrationBean FilterRegistrationBean() {
            final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
            filterRegistrationBean.setFilter(new DelegatingFilterProxy("springSecurityFilterChain"));
            filterRegistrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
            filterRegistrationBean.setName("springSecurityFilterChain");
            filterRegistrationBean.addUrlPatterns("/*");
            return filterRegistrationBean;
        }
	*/

        @Bean
        AuthenticationEntryPoint forbiddenEntryPoint() {
           return new HttpStatusEntryPoint(FORBIDDEN);
        }

        // demo purposes only
        @SuppressWarnings("deprecation")
        @Bean
        public static NoOpPasswordEncoder passwordEncoder() {
            return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
        }

    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        setRegisterErrorPageFilter(false);
        return application.sources(Axis2Application.class);
    }

    public static void main(String[] args) throws Exception {
        String logPrefix = "Axis2Application.main , ";
        if (!isRunning) {
            SpringApplication ctx = new SpringApplication(Axis2Application.class);
            ApplicationContext applicationContext = ctx.run(args);
            String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
            for (String profile : activeProfiles) {
                logger.debug(logPrefix + "Spring Boot profile: " + profile);
            }
        }
        isRunning = true;
    }



}
