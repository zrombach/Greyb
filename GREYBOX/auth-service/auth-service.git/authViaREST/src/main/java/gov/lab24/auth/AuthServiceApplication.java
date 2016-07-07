package gov.lab24.auth;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import gov.lab24.auth.core.User;
import gov.lab24.auth.db.DataSourceFactory;
import gov.lab24.auth.db.MongoLoader;
import gov.lab24.auth.security.SpringAuthFactory;
import gov.lab24.auth.security.SpringAuthenticator;
import gov.lab24.auth.verify.VerifyServiceResults;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthFactory;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.hubspot.dropwizard.guice.GuiceBundle;
import com.bazaarvoice.dropwizard.redirect.RedirectBundle;
import com.bazaarvoice.dropwizard.redirect.PathRedirect;

public class AuthServiceApplication extends Application<AuthServiceConfiguration> {


	public static void main(String[] args) throws Exception {
		new AuthServiceApplication().run(args);
	}


	private GuiceBundle<AuthServiceConfiguration> guiceBundle;

	@Override
	public void initialize(Bootstrap<AuthServiceConfiguration> bootstrap) {
		guiceBundle = GuiceBundle.<AuthServiceConfiguration>newBuilder()
				.addModule(new AuthServiceModule())
				.setConfigClass(AuthServiceConfiguration.class)
				.enableAutoConfig(getClass().getPackage().getName())
				.build();
		bootstrap.addBundle(guiceBundle);
		bootstrap.addBundle(new AssetsBundle("/assets/adminClient", "/admin"));
	}

	@Override
	public void run(AuthServiceConfiguration configuration, Environment env) throws Exception {
        MongoLoader dbLoader = new MongoLoader(configuration, env);
        dbLoader.bootstrap();

		/**
		 * Enabling CORS on the /extras endpoints for purposes of client application.
		 * Note that CORS isn't supported by the system which this service stubs out.
		 */
		configureCors(env);

		/**
		 * Set up Spring security in the application
		 */
		String[] springConfigs = configuration.getSpringSecurityConfiguration();
		ApplicationContext applicationContext = new FileSystemXmlApplicationContext(springConfigs);

		final XmlWebApplicationContext wctx = guiceBundle.getInjector().getInstance(XmlWebApplicationContext.class);
		wctx.setParent(applicationContext);
		wctx.setConfigLocation("");
		wctx.refresh();
		env.servlets().addServletListeners(new ServletContextListener() {
			@Override
			public void contextInitialized(ServletContextEvent servCtx) {
				servCtx.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
						wctx);
				wctx.setServletContext(servCtx.getServletContext());
			}

			@Override
			public void contextDestroyed(ServletContextEvent arg0) {
				// TODO Auto-generated method stub
			}
		});

		// Register first factory (hence isClone = false)
		SpringAuthFactory authFactory = new SpringAuthFactory(new SpringAuthenticator(), applicationContext, env, UserDetails.class, false);
		env.jersey().register(AuthFactory.binder(authFactory));
		
/*		SpringSecurityAuthProvider authProvider = guiceBundle.getInjector().getInstance(SpringSecurityAuthProvider.class);
		authProvider.setApplicationContext(applicationContext);
		authProvider.registerProvider(env);
*/
		
	}

	private void configureCors(Environment environment) {
		Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/extras/*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		filter.setInitParameter("allowedHeaders",
				"Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
		filter.setInitParameter("allowCredentials", "true");
	}
	
}
