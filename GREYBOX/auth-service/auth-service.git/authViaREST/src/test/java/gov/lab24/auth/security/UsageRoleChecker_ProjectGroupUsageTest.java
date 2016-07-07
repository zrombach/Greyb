package gov.lab24.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gov.lab24.auth.core.Project;
import gov.lab24.auth.resources.GroupsResource;
import gov.lab24.auth.resources.ProjectsAdminResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import testsupport.gov.lab24.TestUserBuilder;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class UsageRoleChecker_ProjectGroupUsageTest {

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
	public void serverWithProjectOwnerRoleIsProxy() {
		List<Project> serverProjects = new ArrayList<Project>();
		Project testProject = mock(Project.class);
		testProject.name = "foo";
		serverProjects.add(testProject);
		
		ImmutableList<String> owners = ImmutableList.<String>builder().add(server.getUsername()).build();
		when(testProject.getOwners()).thenReturn(owners);
			
		when(projectHelper.listProjectsForUser(server.getUsername(), testProject.name)).thenReturn(serverProjects);
		
		UsageRole usageRole = roleChecker.getUsageRoleForProjectRecords(server, testProject.name );
		assertThat(usageRole).isEqualTo(UsageRole.PROXY);

	}
	
	@Test
	public void serverWithServerRoleIsProxy() {

		List<Project> serverProjects = new ArrayList<Project>();
		Project testProject = mock(Project.class);
		testProject.name = "foo";
		serverProjects.add(testProject);
		
		ImmutableList<String> servers = ImmutableList.<String>builder().add(server.getUsername()).build();
		when(testProject.getServers()).thenReturn(servers);
			
		when(projectHelper.listProjectsForUser(server.getUsername(), testProject.name)).thenReturn(serverProjects);
		
		UsageRole usageRole = roleChecker.getUsageRoleForProjectRecords(server, testProject.name );
		assertThat(usageRole).isEqualTo(UsageRole.PROXY);

	}
	
	@Test
	public void serverNotInServerRoleOrOwnerIsDenied() {
		
		List<Project> serverProjects = new ArrayList<Project>();
		Project testProject = mock(Project.class);
		testProject.name = "foo";
			
		when(projectHelper.listProjectsForUser(server.getUsername(), testProject.name)).thenReturn(serverProjects);
		
		UsageRole usageRole = roleChecker.getUsageRoleForProjectRecords(server, testProject.name );
		assertThat(usageRole).isEqualTo(UsageRole.NONE);

	}
	
	@Test
	public void userIsDeniedIfNotInReadersOrWriters() {

		// TODO: validate: is user cert denied from getting info on projects???
		//  Don't think so: read access is granted to members of the group's project's Readers and Editors Group
		List<Project> userProjects = new ArrayList<Project>();
		ImmutableList<String> readers = ImmutableList.<String>builder().add(user.getUsername()).build();
		ImmutableList<String> emptyList = ImmutableList.<String>builder().build();
		Project testProject = mock(Project.class);
		when(testProject.getReaders()).thenReturn(readers);
		when(testProject.getEditors()).thenReturn(emptyList);
		when(testProject.getServers()).thenReturn(emptyList);
		testProject.name = "foo";
		userProjects.add(testProject);
			
		when(projectHelper.listProjectsForUser(user.getUsername(), testProject.name)).thenReturn(userProjects);
		
		UsageRole usageRole = roleChecker.getUsageRoleForProjectRecords(user, testProject.name );
		assertThat(usageRole).isEqualTo(UsageRole.PROXY);		

	}
}

