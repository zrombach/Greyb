package gov.lab24.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gov.lab24.auth.core.Group;
import gov.lab24.auth.core.Project;
import gov.lab24.auth.resources.GroupsResource;
import gov.lab24.auth.resources.ProjectsAdminResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import testsupport.gov.lab24.TestUserBuilder;

@RunWith(MockitoJUnitRunner.class)
public class UsageRoleChecker_UserUsageTest {

	GroupsResource groupHelper;
	ProjectsAdminResource projectHelper;
	UsageRoleChecker roleChecker;
	UserDetails userRequesting, user, server, user2;
		
	@Before
	public void setup() {		
		groupHelper = mock(GroupsResource.class);
		projectHelper = mock(ProjectsAdminResource.class);
		roleChecker = new UsageRoleChecker(groupHelper, projectHelper);		
		user = TestUserBuilder.buildBasicUser("basic");
		server = TestUserBuilder.buildServerUser();		
	}
	

	@Test
	public void validateSelfRoleIsSelf() {
		UsageRole usageRole = roleChecker.getUsageRoleForUserRecords(user, user.getUsername());
		assertThat(usageRole).isEqualTo(UsageRole.SELF);
	}
	
	@Test
	public void validateServerRoleIsSelfAgainstSelf() {
		UsageRole usageRole = roleChecker.getUsageRoleForUserRecords(server, server.getUsername());
		assertThat(usageRole).isEqualTo(UsageRole.SELF);
	}
	
	@Test
	public void validateServerWithoutReaderPrivilegeIsNonProxy() {
		
		List<Project> serverProjects = new ArrayList<Project>();
		List<Group> serverGroups = new ArrayList<Group>();
		Project testProject = mock(Project.class);
		when(testProject.getProjectGroups()).thenReturn(serverGroups);
		serverProjects.add(testProject);
		
		Set<Group> otherGroups = new HashSet<Group>();
		
		when(projectHelper.listProjectsForUser(server.getUsername())).thenReturn(serverProjects);
		when(groupHelper.getAllGroupsForUserWithMembershipType(user.getUsername(), GroupMembershipType.MEMBER)).thenReturn(otherGroups);
		
		UsageRole usageRole = roleChecker.getUsageRoleForUserRecords(server, user.getUsername());
		//assertThat(usageRole).isEqualTo(UsageRole.OTHER);
		// TODO: We're debating PROXY vs OTHER when getting user information via servers....   See UsageRoleChecker
		assertThat(usageRole).isEqualTo(UsageRole.PROXY);
	}

	@Test
	public void validateServerWithReaderPrivilegeIsProxy() {
		
		List<Project> serverProjects = new ArrayList<Project>();
		List<Group> serverGroups = new ArrayList<Group>();
		
		Group sharedGroup = new Group();
		serverGroups.add(sharedGroup);

		Project testProject = mock(Project.class);
		serverProjects.add(testProject);
		when(testProject.getProjectGroups()).thenReturn(serverGroups);
		
		Set<Group> otherGroups = new HashSet<Group>();
		otherGroups.add(sharedGroup);
		
		when(projectHelper.listProjectsForUser(server.getUsername())).thenReturn(serverProjects);
		when(groupHelper.getAllGroupsForUserWithMembershipType(user.getUsername(), GroupMembershipType.MEMBER)).thenReturn(otherGroups);
		
		UsageRole usageRole = roleChecker.getUsageRoleForUserRecords(server, user.getUsername());
		assertThat(usageRole).isEqualTo(UsageRole.PROXY);
	}

	@Test 
	public void testUsersNoOverlappingGroups() {
		
		UserDetails otherUser = TestUserBuilder.buildBasicUser("other");
		Set<Group> userGroups = new HashSet<Group>();
		Set<Group> otherGroups = new HashSet<Group>();
		
		when(groupHelper.getAllGroupsForUserWithMembershipType(user.getUsername(), GroupMembershipType.OWNERORADMINISTRATOR)).thenReturn(userGroups);
		when(groupHelper.getAllGroupsForUserWithMembershipType(otherUser.getUsername(), GroupMembershipType.MEMBER)).thenReturn(otherGroups);
		
		UsageRole usageRole = roleChecker.getUsageRoleForUserRecords(user, otherUser.getUsername());
		assertThat(usageRole).isEqualTo(UsageRole.OTHER);
	}


	// TODO: Confirm: users have no special privileges on getting user detailed information, even if they're groups with owner/Administrator privileges
/*	@Test 
	public void testUsersOverlappingGroups() {
		
		UserDetails otherUser = TestUserBuilder.buildBasicUser("other");
		Set<Group> userGroups = new HashSet<Group>();
		Group sharedGroup = new Group();
		userGroups.add(sharedGroup);
		
		Set<Group> otherGroups = new HashSet<Group>();
		otherGroups.add(sharedGroup);
		
		when(groupHelper.getAllGroupsForUserWithMembershipType(user.getUsername(), GroupMembershipType.OWNERORADMINISTRATOR)).thenReturn(userGroups);
		when(groupHelper.getAllGroupsForUserWithMembershipType(otherUser.getUsername(), GroupMembershipType.MEMBER)).thenReturn(otherGroups);
		
		UsageRole usageRole = roleChecker.checkUserUsageRole(user, otherUser.getUsername());
		assertThat(usageRole).isEqualTo(UsageRole.PROXY);
	}
*/
	
	// TODO: Confirm: users have no special privileges on getting user detailed information, even if they're groups with owner/Administrator privileges
/*	@Test 
	public void testUsersGroupsSameNames() {
		
		UserDetails otherUser = TestUserBuilder.buildBasicUser("other");
		Set<Group> userGroups = new HashSet<Group>();
		Group group1 = new Group();
		group1.setName("project1!Bash");
		userGroups.add(group1);
		
		Set<Group> otherGroups = new HashSet<Group>();
		Group groupOne = new Group();
		groupOne.setName(group1.name);
		otherGroups.add(groupOne);
		
		when(groupHelper.getAllGroupsForUserWithMembershipType(user.getUsername(), GroupMembershipType.OWNERORADMINISTRATOR)).thenReturn(userGroups);
		when(groupHelper.getAllGroupsForUserWithMembershipType(otherUser.getUsername(), GroupMembershipType.MEMBER)).thenReturn(otherGroups);
		
		UsageRole usageRole = roleChecker.checkUserUsageRole(user, otherUser.getUsername());
		assertThat(usageRole).isEqualTo(UsageRole.PROXY);
	}
*/
}

