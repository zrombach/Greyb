package gov.lab24.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import testsupport.gov.lab24.TestUserBuilder;

/**
 * Two parts: - does the ContainerResponse filter apply the dummy heading in the
 * right places? - does the WriteInterceptor use the dummy heading
 * appropriately?
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JSONCommentsInterceptorTest extends JerseyTest {

	JSONCommentsInterceptor interceptor;
	private static final String FROM_RESOURCE = "-from_resource";

	@Override
	protected ResourceConfig configure() {
		return new ResourceConfig(TestResource.class, JSONCommentsInterceptor.class);
	}

	@Path("test")
	public static class TestResource {

		@PUT
		@Produces({ "application/json", "application/xml" })
		@Consumes("text/plain")
		public String put(String str) {
			System.out.println("resource: " + str);
			return str + FROM_RESOURCE;
		}
	}

	@Before
	public void setup() {
		interceptor = new JSONCommentsInterceptor();
	}

	@Test
	public void testFilterAppliesDummyHeading() throws IOException {

		ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
		ContainerRequest requestContext = mock(ContainerRequest.class);
		MultivaluedMap<String, String> requestHeaders = new MultivaluedHashMap<>();
		MultivaluedMap<String, Object> responseHeaders = new MultivaluedHashMap<>();
		requestHeaders.add(JSONCommentsInterceptor.CSRF_HEADER, "true");
		when(requestContext.getHeaders()).thenReturn(requestHeaders);
		when(responseContext.getHeaders()).thenReturn(responseHeaders);
		when(requestContext.getPath(true)).thenReturn("/not_extras");

		interceptor.filter(requestContext, responseContext);
		assertThat(responseContext.getHeaders().keySet()).contains(JSONCommentsInterceptor.DUMMY_CSRF_HEADER);

	}

	@Test
	public void testFilterDontApplyDummyHeading() throws IOException {

		ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
		ContainerRequest requestContext = mock(ContainerRequest.class);
		MultivaluedMap<String, String> requestHeaders = new MultivaluedHashMap<>();
		MultivaluedMap<String, Object> responseHeaders = new MultivaluedHashMap<>();
		when(requestContext.getHeaders()).thenReturn(requestHeaders);
		when(responseContext.getHeaders()).thenReturn(responseHeaders);
		when(requestContext.getPath(true)).thenReturn("/not_extras");

		interceptor.filter(requestContext, responseContext);
		assertThat(responseContext.getHeaders().keySet()).doesNotContain(JSONCommentsInterceptor.DUMMY_CSRF_HEADER);

	}

	private void buildNonServerUserContext() {
		UserDetails user = TestUserBuilder.buildBasicUser("user");
		Authentication authentication = Mockito.mock(Authentication.class);
		Mockito.when(authentication.getPrincipal()).thenReturn(user);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	private void buildServerUserContext() {
		UserDetails user = TestUserBuilder.buildServerUser();
		Authentication authentication = Mockito.mock(Authentication.class);
		Mockito.when(authentication.getPrincipal()).thenReturn(user);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

	}

	@Test
	public void testUserNoCommentsWrapperFlag() throws WebApplicationException, IOException {

		buildNonServerUserContext();

		client().register(JSONCommentsInterceptor.class);
		WebTarget target = target().path("test");
		String entity = "this is a bogus text entity";

		Response response = target.request().put(Entity.entity(entity, MediaType.TEXT_PLAIN_TYPE));
		InputStream is = response.readEntity(InputStream.class);

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String str = br.readLine();

		assertThat(str).startsWith("/*").endsWith("*/");
		assertThat(response.getHeaders().keySet()).doesNotContain(JSONCommentsInterceptor.DUMMY_CSRF_HEADER);
		
		// our test resource returns JSON as its default
		assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE)).contains(MediaType.APPLICATION_JSON);
	}

	@Test
	public void testServerNoFlagCommentsWrapped() throws IOException {

		buildServerUserContext();

		client().register(JSONCommentsInterceptor.class);
		WebTarget target = target().path("test");
		String entity = "this is a bogus text entity";

		Response response = target.request().accept(MediaType.APPLICATION_JSON).put(Entity.entity(entity, MediaType.TEXT_PLAIN_TYPE));
		InputStream is = response.readEntity(InputStream.class);

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String str = br.readLine();

		assertThat(str).startsWith("/*").endsWith("*/");
		assertThat(response.getHeaders().keySet()).doesNotContain(JSONCommentsInterceptor.DUMMY_CSRF_HEADER);
		assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE)).contains(MediaType.APPLICATION_JSON);

	}

	@Test
	public void testServerWithFlagNotWrapped() throws IOException {

		buildServerUserContext();

		client().register(JSONCommentsInterceptor.class);
		WebTarget target = target().path("test");
		String entity = "this is a bogus text entity";

		Response response = target.request().header(JSONCommentsInterceptor.CSRF_HEADER, "true").accept(MediaType.APPLICATION_JSON).put(Entity.entity(entity, MediaType.TEXT_PLAIN_TYPE));
		InputStream is = response.readEntity(InputStream.class);

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String str = br.readLine();

		assertThat(str).doesNotContain("/*").doesNotContain("*/");
		assertThat(response.getHeaders().keySet()).doesNotContain(JSONCommentsInterceptor.DUMMY_CSRF_HEADER);
		assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE)).contains(MediaType.APPLICATION_JSON);

	}

	@Test
	public void testNotJSONNotWrapped() throws IOException {

		buildNonServerUserContext();

		client().register(JSONCommentsInterceptor.class);
		WebTarget target = target().path("test");
		String entity = "this is a bogus text entity";

		Response response = target.request().accept(MediaType.APPLICATION_XML).put(Entity.entity(entity, MediaType.TEXT_PLAIN_TYPE));
		InputStream is = response.readEntity(InputStream.class);

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		// note that the string won't actually be converted to XML, since we haven't involved Jackson, etc.  We're just testing the interceptor
		String str = br.readLine();

		assertThat(str).doesNotContain("/*").doesNotContain("*/");
		assertThat(response.getHeaders().keySet()).doesNotContain(JSONCommentsInterceptor.DUMMY_CSRF_HEADER);
		assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE)).contains(MediaType.APPLICATION_XML);

	}

}
