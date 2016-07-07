package gov.lab24.auth.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;
import gov.lab24.auth.core.Affiliation;
import gov.lab24.auth.security.UsageRole;
import gov.lab24.auth.security.UsageRoleChecker;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mongojack.JacksonDBCollection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import testsupport.gov.lab24.TestDataClient;


/**
 * Unit tests for {@link UserResource}
 * 
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest {

	
	@Mock private JacksonDBCollection<gov.lab24.auth.core.User, String> dbCollection; 
	@Mock private ProjectsResource projectResource;
	@Mock private GroupsResource groupsResource;
	@Mock private UsageRoleChecker roleChecker;
	
	//private final UserResource resource = new UserResource();
	private UserResource resource;
	private Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

	@Before
	public void setup() {
		roleChecker = mock(UsageRoleChecker.class);
		when(roleChecker.getUsageRoleForUserRecords(anyObject(), anyString())).thenReturn(UsageRole.PROXY);
		resource = new UserResource(dbCollection, projectResource, groupsResource, roleChecker);
	}
	
	@Test
	@Ignore
	public void getUserInfoSelf() {
			
		UserDetails userDetails = new User("tina", "blank", authorities);
				
		Object user = resource.getInfo(userDetails, "tina");			
		assertThat(user).isInstanceOf(gov.lab24.auth.core.User.class);
		assertThat( ((gov.lab24.auth.core.User) user).getEmployeeId()).isEqualTo("EMPLID");
	}

	@Test
	@Ignore
	public void getUserInfoOtherNonProxied() {
		UserDetails userDetails = new User("tina", "blank", authorities);
		
		Object user = resource.getInfo(userDetails, "someoneOtherThanTina");
		assertThat(user).isInstanceOf(gov.lab24.auth.core.UserAbbreviated.class);		

	}

	@Test
	@Ignore
	public void getUserInfoOtherProxied() {
/*		resources.client().resource("/users/someoneelse/info?isSelfOrProxied=true");
*/	}
	
	
	@Test
	public void testGetFullUser() {
		
		TestDataClient.getInstance().clearUserData();
		dbCollection = TestDataClient.getInstance().getUserCollection();
		
		TestDataClient.getInstance().addUserData(
				"{ 'dn': 'foo', 'displayName': 'foo bar', 'employeeId': 'bar', 'fullName': 'Foo Bar Baz', 'lastName': 'Bar'," +
			    "'uid': 'fbar1', 'clearances': ['TOP SECRET', 'SECRET', 'UNCLEARED'], 'formalAccess': ['foo', 'bar', 'baz', 'fuzz'], 'country': 'USA', 'affiliations': ['CANDIDATE', 'CONSULTANT']}");
	
		UserDetails userDetails = new User("tina", "blank", authorities);
		authorities.add(new SimpleGrantedAuthority("ROLE_SERVER"));
		
		resource = new UserResource(dbCollection, projectResource, groupsResource, roleChecker);
		
		Object user = resource.getInfo(userDetails, "foo");			

		assertThat(user).isInstanceOf(gov.lab24.auth.core.User.class);
		gov.lab24.auth.core.User checkUser = (gov.lab24.auth.core.User) user;
		assertThat(checkUser.getAffiliations()).contains(Affiliation.CANDIDATE);
	}
}
