package gov.lab24.auth.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gov.lab24.auth.core.Project;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.JacksonDBCollection;

import testsupport.gov.lab24.TestDataClient;

public class ProjectAdminResourceTest {

	private JacksonDBCollection<gov.lab24.auth.core.Project, String> dbCollection;
	private ProjectsAdminResource resource;
	
	@Before
	public void setup() {
		
		TestDataClient.getInstance().clearProjectData();
		dbCollection = TestDataClient.getInstance().getProjectCollection();
		
		resource = new ProjectsAdminResource(dbCollection);
		
		UriInfo uriInfo = mock(UriInfo.class);
		 try {
			when(uriInfo.getBaseUri()).thenReturn(new URI("http://localhost:8080"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		resource.uriInfo = uriInfo;

	}
	
	@Test
	public void listProjectsForServerNoDataLoaded() {
		
		List<Project> projects = resource.listProjectsForUser("foo");
	
		assertThat(projects.size()).isEqualTo(0);
	}

	@Test
	public void listProjectsForServerWithDataLoaded() {
		
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'owners': [ 'foo']}"  );
		List<Project> projects = resource.listProjectsForUser("foo");
		
		assertThat(projects.size()).isEqualTo(1);

	}
	
	@Test
	public void listProjectsForServerWithOtherOwner() {
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'owners': [ 'baz']}"  );
		
		List<Project> projects = resource.listProjectsForUser("foo");
		
		assertThat(projects.size()).isEqualTo(0);

	}
	
	@Test
	public void listCheckMultipleProjects() {
		
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'owners': [ 'baz']}"  );
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAR', 'owners': [ 'baz']}"  );
		List<Project> projects = resource.listProjectsForUser("baz");
		
		assertThat(projects.size()).isEqualTo(2);
	}
	
	
	@Test
	public void checkProjectForOwner() {
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'owners': [ 'baz', 'bar', 'zoo']}"  );
		
		List<Project> projects = resource.listProjectsForUser("baz", "projectBAZ");
		
		assertThat(projects.size()).isEqualTo(1);
	}

	@Test
	public void checkProjectForOwnerNegative() {
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ', 'owners': [ 'baz']}"  );
		
		List<Project> projects = resource.listProjectsForUser("foo", "projectBAZ");
		
		assertThat(projects.size()).isEqualTo(0);
	}
	
	@Test
	public void testUpdateOwnersForProjects() {
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ'}"  );
		
		List<Project> projects = resource.listProjects();
		assertThat(projects.size()).isEqualTo(1);
		Project project = projects.get(0);
		assertThat(project.getOwners().size()).isEqualTo(0);
		
		project.getOwners().add("bar");
		project.getOwners().add("baz");
		
		resource.createOrUpdateProject(project);
		
		projects = resource.listProjects();
		assertThat(projects.size()).isEqualTo(1);
		project = projects.get(0);
		assertThat(project.getOwners().size()).isEqualTo(2);
	}
	
	@Test
	public void testUpdateServersForProjects() {
		TestDataClient.getInstance().addProjectData("{ 'name': 'projectBAZ'}"  );
		
		List<Project> projects = resource.listProjects();
		assertThat(projects.size()).isEqualTo(1);
		Project project = projects.get(0);
		assertThat(project.getServers().size()).isEqualTo(0);
		
		project.getServers().add("bar");
		project.getServers().add("baz");
		
		resource.createOrUpdateProject(project);
		
		projects = resource.listProjects();
		assertThat(projects.size()).isEqualTo(1);
		project = projects.get(0);
		assertThat(project.getServers().size()).isEqualTo(2);
	}

}
