package gov.lab24.auth.security;

import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.setup.Environment;

import java.util.EnumSet;

import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.google.common.base.Optional;

@Singleton
public class SpringAuthFactory<T> extends AuthFactory<UserDetails, T> {

	public static final String DEFAULT_FILTER_BEAN_NAME = "org.springframework.security.filterChainProxy";
	private static final Logger logger = LoggerFactory.getLogger(SpringAuthFactory.class);
	private ApplicationContext context;
	private Environment env;
	private Class<T> generatedClass;

	public SpringAuthFactory(Authenticator<UserDetails, T> authenticator, ApplicationContext context, Environment env, Class<T> generatedClass, boolean isClone) {
		super(authenticator);
		this.context = context;
		this.env = env;
		this.generatedClass = generatedClass;
		
		// Note: use of @Singleton annotation was not sufficient - needed to specifically be aware of whether we'd already registered filters to 
		//  avoid a chain of 'Overriding the existing filter' messages
		if (!isClone) {
			registerFilters(env);
		}
	}

	@Override
	public T provide() {
		Optional<T> result;
		try {
			result = authenticator().authenticate(null);
			if (result.isPresent()) {
				return result.get();
			}
		} catch (AuthenticationException e) {
			logger.warn("Unable to authenticate due to exception in authentication handling: ", e);
		}
		
		return null;
	}

	@Override
	public AuthFactory clone(boolean required) {
		return new SpringAuthFactory<>(authenticator(), this.context, this.env, this.generatedClass, true );
	}

	@Override
	public Class<T> getGeneratedClass() {
		return this.generatedClass;
	}
	
	private void registerFilters(Environment environment) {
		
		Object proxyObject = this.context.getBean(DEFAULT_FILTER_BEAN_NAME);
		if (null == proxyObject) {
			logger.info("No FilterChainProxy found in the spring container, using default DelegatingFilterProxy");
			
			// 0.6.2 implementation 
			//environment.servlets().addFilter("/*", DelegatingFilterProxy.class)
			//		.setInitParameter(DEFAULT_FILTER_BEAN_NAME, DEFAULT_FILTER_BEAN_NAME);
			
			// 0.7* implementation: http://stackoverflow.com/questions/22980509/integrating-dropwizard-with-spring-security
			FilterRegistration.Dynamic filterRegistration = environment.servlets().addFilter("springSecurityFilterChain", DelegatingFilterProxy.class);
			filterRegistration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
		} else {
			FilterChainProxy proxy = (FilterChainProxy) proxyObject;
			logger.info("FilterChainProxy ({}) found, assigning to DelegatingFilterProxy", DEFAULT_FILTER_BEAN_NAME);
			//environment.servlets().addFilter("/*", new DelegatingFilterProxy(proxy));
			
			// 0.7* implementation: http://stackoverflow.com/questions/22980509/integrating-dropwizard-with-spring-security
			FilterRegistration.Dynamic filterRegistration = environment.servlets().addFilter("springSecurityFilterChain", DelegatingFilterProxy.class);
			filterRegistration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
		}			

	}

	@Override
	public void setRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub
		
	}

}
