package gov.lab24.auth.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gov.lab24.auth.core.Group;
import gov.lab24.auth.core.groupsResponses.DeleteResponse;
import gov.lab24.auth.core.groupsResponses.GenericGroupResponse;
import gov.lab24.auth.core.groupsResponses.PutResponse;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.JacksonDBCollection;
import org.springframework.security.core.userdetails.UserDetails;

import testsupport.gov.lab24.TestDataClient;
import testsupport.gov.lab24.TestUserBuilder;

public class GroupsResourceTest {

	private JacksonDBCollection<gov.lab24.auth.core.Project, String> dbCollection;
	private GroupsResource resource;
	UriInfo uriInfo;

	@Before
	public void setup() {
		
		TestDataClient.getInstance().clearProjectData();
		dbCollection = TestDataClient.getInstance().getProjectCollection();
				
		uriInfo = mock(UriInfo.class);
		 try {
			when(uriInfo.getBaseUri()).thenReturn(new URI("http://localhost:8080"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		 
		resource = new GroupsResource(dbCollection);
		resource.uriInfo = uriInfo;

	}
	

	@Test
	public void getGroupsSameName() {
				
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		String groupName = "projectBAZ!goo";
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ], 'editors': ['basic']}"  );
		
		Group retrievedOnce = resource.getGroup(user, groupName, false);
		Group retrievedAgain = resource.getGroup(user, groupName, false);
		assertThat(retrievedOnce).isEqualsToByComparingFields(retrievedAgain);
		assertThat(retrievedOnce).isEqualTo(retrievedAgain);
	}

	@Test
	public void testGetInfoAsHumanUserGroupMembers() {
		
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		String groupName = "projectBAZ!goo";
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group', members: ['basic']} ]}"  );
		try {
			resource.getGroup(user, groupName, false);
			// failure - unable to retrieve for just members		
			fail("Should have thrown WebApplicationException - user should be forbidden");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(403);
		}

	}

	@Test
	public void testGetInfoAsHumanUserGroupOwners() {
		
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		String groupName = "projectBAZ!goo";
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group', owners: ['basic']} ]}"  );

		Group retrieveGroup = resource.getGroup(user, groupName, false);
		// success - no exception, and projectName retrieved is one exepcted
		assertThat(retrieveGroup.getName()).isEqualTo(groupName);
		
		UserDetails otherUser = TestUserBuilder.buildBasicUser("other");
		try {
			retrieveGroup = resource.getGroup(otherUser, groupName, false);
			fail("Should have thrown WebApplicationException - user should be forbidden");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(403);
		}

	}
	
	@Test
	public void testGetInfoAsHumanUserGroupAdministrators() {
		
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		String groupName = "projectBAZ!goo";
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group', administrators: ['basic']} ]}"  );

		Group retrieveGroup = resource.getGroup(user, groupName, false);
		// success - no exception, and projectName retrieved is one exepcted
		assertThat(retrieveGroup.getName()).isEqualTo(groupName);
		
		UserDetails otherUser = TestUserBuilder.buildBasicUser("other");
		try {
			retrieveGroup = resource.getGroup(otherUser, groupName, false);
			fail("Should have thrown WebApplicationException - user should be forbidden");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(403);
		}
	}

	@Test
	public void testGetInfoAsProjectReaders() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();

		String groupName = "projectBAZ!goo";
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'readers': ['" + user.getUsername()+"', '"+ serverUser.getUsername()+ "'], 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Group retrieveGroup = resource.getGroup(user, groupName, false);
		// success - no exception, and projectName retrieved is one exepcted
		assertThat(retrieveGroup.getName()).isEqualTo(groupName);

		retrieveGroup = resource.getGroup(serverUser, groupName, false);
		assertThat(retrieveGroup.getName()).isEqualTo(groupName);

		UserDetails otherUser = TestUserBuilder.buildBasicUser("other");
		try {
			retrieveGroup = resource.getGroup(otherUser, groupName, false);
			fail("Should have thrown WebApplicationException - user should be forbidden");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(403);
		}
	}

	@Test
	public void testGetInfoAsProjectEditors() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'editors': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Group retrieveGroup = resource.getGroup(user, groupName, false);
		// success - no exception, and projectName retrieved is one exepcted
		assertThat(retrieveGroup.getName()).isEqualTo(groupName);
		
		retrieveGroup = resource.getGroup(serverUser, groupName, false);
		assertThat(retrieveGroup.getName()).isEqualTo(groupName);
		
		UserDetails otherUser = TestUserBuilder.buildBasicUser("other");
		try {
			retrieveGroup = resource.getGroup(otherUser, groupName, false);
			fail("Should have thrown WebApplicationException - user should be forbidden");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(403);
		}
	}

	@Test
	public void testGetInfoAsProjectServers() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'servers': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Group retrieveGroup;
		
		try {
			retrieveGroup = resource.getGroup(user, groupName, false);
			fail("Should have thrown WebApplicationException - user should be forbidden");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(403);
		}

		try {
			retrieveGroup = resource.getGroup(serverUser, groupName, false);
			fail("Should have thrown WebApplicationException - user should be forbidden");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(403);
		}

	}

	@Test
	public void testPutInfoAsProjectEditors() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";

		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'editors': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Group updateGroup, checkGroup;
		updateGroup = new Group();
		updateGroup.setName(groupName);
		
		
		Response putResponse = resource.updateGroup(user, groupName, updateGroup);
		checkPutResponseResult(putResponse, 403,  "false");

		putResponse = resource.updateGroup(serverUser, groupName, updateGroup);
		checkPutResponseResult(putResponse, 204, "true");
	}


	@Test
	public void testPutInfoAsProjectReaders() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";

		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'readers': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Group updateGroup, checkGroup;
		updateGroup = new Group();
		updateGroup.setName(groupName);
		
		// non-server user should be forbidden
		Response putResponse = resource.updateGroup(user, groupName, updateGroup);
		checkPutResponseResult(putResponse, 403,  "false");

		// reader user should be forbidden
		putResponse = resource.updateGroup(serverUser, groupName, updateGroup);
		checkPutResponseResult(putResponse, 403,  "false");

	}
	
	@Test
	public void testPutInfoAsProjectServers() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";

		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'servers': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Group updateGroup;
		updateGroup = new Group();
		updateGroup.setName(groupName);
		
		// non-server user should be forbidden
		Response putResponse = resource.updateGroup(user, groupName, updateGroup);
		checkPutResponseResult(putResponse, 403,  "false");

		// server user should be forbidden, unless otherwise an editor
		putResponse = resource.updateGroup(serverUser, groupName, updateGroup);
		checkPutResponseResult(putResponse, 403,  "false");

	}

	@Test
	public void testPutInfoGroupAsAdministrators() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group', 'administrators': ['" + serverUser.getUsername() + "','" + user.getUsername() + "'] } ]}"  );

		Group updateGroup, checkGroup;
		updateGroup = new Group();
		updateGroup.setName(groupName);
		
		// non-server user should be forbidden
		Response putResponse = resource.updateGroup(user, groupName, updateGroup);
		checkPutResponseResult(putResponse, 403,  "false");

		putResponse = resource.updateGroup(serverUser, groupName, updateGroup);
		checkPutResponseResult(putResponse, 204, "true");		// yes, recognize there should be no payload for a 204
	}

	@Test
	public void testPutInfoGroupAsOwners() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group', 'owners': ['" + serverUser.getUsername() + "','" + user.getUsername() + "'] } ]}"  );

		Group updateGroup, checkGroup;
		updateGroup = new Group();
		updateGroup.setName(groupName);
		
		// non-server user should be forbidden
		Response putResponse = resource.updateGroup(user, groupName, updateGroup);
		checkPutResponseResult(putResponse, 403,  "false");

		putResponse = resource.updateGroup(serverUser, groupName, updateGroup);
		checkPutResponseResult(putResponse, 204, "true");
	}


	@Test
	public void testPutInfoGroupAsMembers() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group', 'members': ['" + serverUser.getUsername() + "','" + user.getUsername() + "'] } ]}"  );

		Group updateGroup, checkGroup;
		updateGroup = new Group();
		updateGroup.setName(groupName);
		
		// non-server user should be forbidden
		Response putResponse = resource.updateGroup(user, groupName, updateGroup);
		checkPutResponseResult(putResponse, 403,  "false");

		// server user should be forbidden, if only members
		putResponse = resource.updateGroup(serverUser, groupName, updateGroup);
		checkPutResponseResult(putResponse, 403,  "false");
		
	}
	
	@Test
	public void testDeleteGroupAsProjectReaders() {
		
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'readers': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Response deleteGroup = resource.deleteGroup(user,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");
		
		deleteGroup = resource.deleteGroup(serverUser,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");
		
	}
	
	@Test
	public void testDeleteGroupAsProjectEditors() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'editors': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Response deleteGroup = resource.deleteGroup(user,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");
		
		deleteGroup = resource.deleteGroup(serverUser,  groupName);
		checkDeleteResponseResult( deleteGroup, 200, "true");  // spec is unclear here - leaving as 200, since we do have a DeleteResponse option with callSucceeded = true		

	}
	
	@Test
	public void testDeleteGroupAsProjectServers() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'servers': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Response deleteGroup = resource.deleteGroup(user,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");
		
		deleteGroup = resource.deleteGroup(serverUser,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");

	}

	
	@Test
	public void testDeleteGroupAsOwners() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'projectGroups': [ { 'owners': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Response deleteGroup = resource.deleteGroup(user,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");
		
		// only group editors can delete, even if proxy
		deleteGroup = resource.deleteGroup(serverUser,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");

	}
	

	@Test
	public void testDeleteGroupAsAdministrators() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'projectGroups': [ { 'administrators': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Response deleteGroup = resource.deleteGroup(user,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");
		
		// only group editors can delete, even if proxy
		deleteGroup = resource.deleteGroup(serverUser,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");
	}
	
	@Test
	public void testDeleteGroupAsMembers() {
		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'projectGroups': [ { 'members': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );

		Response deleteGroup = resource.deleteGroup(user,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");
		
		// only group editors can delete, even if proxy
		deleteGroup = resource.deleteGroup(serverUser,  groupName);
		checkDeleteResponseResult( deleteGroup, 403, "false");

	}
	
	private void checkDeleteResponseResult(Response response, int status, String string) {
		
		checkResponse(DeleteResponse.class, response, status, string);
			
	}
	
	private void checkPutResponseResult(Response response, int status, String string) {
		checkResponse(PutResponse.class, response, status, string);
	}

	private void checkResponse( Class<? extends GenericGroupResponse> class1, Response response, int status, String string) {
		assertThat(response.getStatus()).isEqualTo(status);
		assertThat(response.getEntity()).isInstanceOf(class1);
		assertThat( (class1.cast(response.getEntity())).callSucceeded).isEqualTo(string);
		
	}
	
	@Test
	public void testCreatePostGroupDuplicateGroup() {

		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		Group createGroup = new Group();
		createGroup.setName(groupName);
		
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'editors': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ { 'name': '" + groupName + "', 'displayName': 'group', 'description': 'group'} ]}"  );
		
		try {
			resource.createGroup(user, groupName, createGroup);
			fail("Should have thrown WebApplicationException - non-proxy users should be forbidden ");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(403);
		}
		
		try {
			resource.createGroup(serverUser,  groupName, createGroup);	
			fail("Should have thrown WebApplicationException - group already exists ");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(409);
		}
		
	}
	
	@Test
	public void testCreatePostGroupProjectEditor() {

		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		Group createGroup = new Group();
		createGroup.setName(groupName);

		// can't post a group that already exists - see testCreatePostGroupDuplicateGroup
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'editors': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ ]}"  );
		
		try {
			resource.createGroup(user, groupName, createGroup);
			fail("Should have thrown WebApplicationException - non-proxy users should be forbidden ");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(403);
		}
		
		Group createdGroup = resource.createGroup(serverUser,  groupName, createGroup);
		assertThat(createdGroup.name).isEqualTo(groupName);
	}

	@Test
	public void testCreatePostGroupProjectReaders() {

		UserDetails user = TestUserBuilder.buildBasicUser("basic");
		UserDetails serverUser = TestUserBuilder.buildServerUser();
		String groupName = "projectBAZ!goo";
 
		Group createGroup = new Group();
		createGroup.setName(groupName);

		// can't post a group that already exists - see testCreatePostGroupDuplicateGroup
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'readers': ['" + user.getUsername()+"', '" + serverUser.getUsername()+ "'], 'projectGroups': [ ]}"  );
		
		try {
			resource.createGroup(user, groupName, createGroup);
			fail("Should have thrown WebApplicationException - non-proxy users should be forbidden ");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(403);
		}
		
		try {
			resource.createGroup(serverUser,  groupName, createGroup);
			fail("Should have thrown WebApplicationException - readers can't post ");
		} catch (WebApplicationException we) {
			assertThat(we.getResponse().getStatus()).isEqualTo(404);
		}
	}

	@Test
	@Ignore
	public void testGetGroupMembers() {
		fail("Implement tests");
	}
	
	@Test
	@Ignore
	public void testUpdateGroupMembers() {
		
		// testing underlying method called for all updates - feel free to multiply this out in # of test cases...
		fail("Implement tests");
	}
}
