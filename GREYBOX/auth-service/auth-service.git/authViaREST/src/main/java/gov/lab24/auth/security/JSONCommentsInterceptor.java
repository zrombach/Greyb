package gov.lab24.auth.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.glassfish.jersey.server.ContainerRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Provider
public class JSONCommentsInterceptor implements WriterInterceptor, ContainerResponseFilter {
	
	private static final Logger LOG = Log.getLogger(JSONCommentsInterceptor.class);
	final static String CSRF_HEADER = "X-XSRF-UseProtection";	
	final static String DUMMY_CSRF_HEADER = "LOCAL:X-XSRF-UseProtection";
	final static String IS_EXTRAS_PROP = "gov.lab24.auth.is.extras.request";
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		//Flag requests coming from the /extras endpoints so that they can be ignored by the WriteInterceptor
		if (((ContainerRequest) requestContext).getPath(true).startsWith("extras")) {
			requestContext.setProperty(IS_EXTRAS_PROP, true);
		}

		boolean doUseDummy = true; // checking real value here...

		//TODO: look into why a header is being used here. Could this be accomplished by setting a property as in the above?
		// add a cheater header to response, which we can use for checking in aroundWriteTo - 
		//   containerResponsefilters are executed before WriterInterceptors, so a header added here can be checked in interceptor 
		if (hasCSRFFlag(requestContext.getHeaders(), !doUseDummy)) {
			addDummyCSRFFlag(responseContext.getHeaders());
		}
	}
		
	@Override
	public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

		//Don't process requests for /extras as those are not part of the simulated API
		if (context.getPropertyNames().contains(IS_EXTRAS_PROP) && (Boolean) context.getProperty(IS_EXTRAS_PROP)) {
			context.proceed();
			return;
		}

		boolean isServer = UsageRoleChecker.isServer(getCurrentUser());
		boolean hasCsrfFlag = false;
		boolean useDummy = true;

		hasCsrfFlag = hasCSRFFlag(context.getHeaders(), useDummy);

		List<Object> contentTypes = context.getHeaders().get(HttpHeaders.CONTENT_TYPE);
		if (contentTypes.contains(MediaType.APPLICATION_JSON_TYPE) && (!isServer || (isServer && !hasCsrfFlag))) {
		
			context.setOutputStream(new FilterOutputStream(context.getOutputStream()) {
				
				{
					super.write("/*".getBytes());
				}
				
				
				@Override
				public void write(int b) throws IOException {
					super.write(b);
				}
				
				@Override
				public void close() throws IOException {
					super.write("*/".getBytes());
					super.close();
				}
			});
		} else {
			
			context.setOutputStream(new FilterOutputStream(context.getOutputStream()) {
				@Override
				public void close() throws IOException {
					// clean up cheater header
					removeDummyCSRFFlag(context.getHeaders());

					super.close();
					
				}
			});
		}
		context.proceed();

	}
		
	// TODO: Would rather pull this through via @Auth or other injection means, for testability and consistency reasons
	private UserDetails getCurrentUser() {
		return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private boolean hasCSRFFlag( MultivaluedMap<String, ?> headers, boolean useDummy) {
		String checkKey = CSRF_HEADER;
		if (useDummy) {
			checkKey = DUMMY_CSRF_HEADER;
		}
		if (headers.containsKey(checkKey)) {
			// that the value is there at all is our indicator
			return true;
		} 
		return false;
	}

	/* Don't want to use existing value, since it's carried through via auth service */		
	private void addDummyCSRFFlag(MultivaluedMap<String, Object> headers) {
		headers.add(DUMMY_CSRF_HEADER, "true");
	}
	
	private void removeDummyCSRFFlag(MultivaluedMap<String, Object> headers) {
		headers.remove(DUMMY_CSRF_HEADER);
	}
}
