package gov.lab24.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import gov.lab24.auth.resources.GroupsResource;
import gov.lab24.auth.resources.ProjectsAdminResource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RunWith(MockitoJUnitRunner.class)
public class CertIdentifiedUserDetailsTest {
	
	// drop CN= off beginning, based on regexp used in subject-principal-regex
	private final String serverCertificateTemplate = "${l_hostname}, OU=D00X, O=${l_hostname}, L=${l_hostname}, S=${l_hostname}, C=${DEFAULT_COUNTRY_CODE}";
	private final String userCertificateTemplate = "${l_hostname}, OU=${l_hostname}, O=${l_hostname}, L=${l_hostname}, S=${l_hostname}, C=${DEFAULT_COUNTRY_CODE}";
	private final String serverSlashedForm = "localhost/OU=D00X/O=localhost/L=localhost/ST=localhost/C=US";
	
	private SimpleGrantedAuthority roleUser = new SimpleGrantedAuthority("ROLE_USER");
	private SimpleGrantedAuthority roleServer = new SimpleGrantedAuthority("ROLE_SERVER");
		
	GroupsResource groupHelper = mock(GroupsResource.class);
	ProjectsAdminResource projectHelper = mock(ProjectsAdminResource.class);
	
	
	@Test 
	public void checkUserCertificate() {
		
		CertIdentifiedUserDetails userDetails = new CertIdentifiedUserDetails();
		
		String userCertString = userCertificateTemplate.replaceAll("\\$\\{l_hostname\\}", "foo");
		UserDetails loadedUser = userDetails.loadUserByUsername(userCertString);
		
		assertThat(loadedUser.getUsername()).isEqualTo("foo");
		assertThat(loadedUser.getAuthorities()).hasSize(1);
		
		assertTrue(loadedUser.getAuthorities().contains(roleUser));
	}
	
	@Test 
	public void checkServerCertificate() {
		
		CertIdentifiedUserDetails userDetails = new CertIdentifiedUserDetails();
		
		String serverCertString = serverCertificateTemplate.replaceAll("\\$\\{l_hostname\\}", "serverBar");
		UserDetails loadedUser = userDetails.loadUserByUsername(serverCertString);
		
		assertThat(loadedUser.getUsername()).isEqualTo("serverBar");
		assertThat(loadedUser.getAuthorities()).hasSize(2);
		
		assertTrue(loadedUser.getAuthorities().contains(roleUser));
		assertTrue(loadedUser.getAuthorities().contains(roleServer));
	}

	@Test
	public void checkServerCertSlashedForm() {
		
		CertIdentifiedUserDetails userDetails = new CertIdentifiedUserDetails();
		
		UserDetails loadedUser = userDetails.loadUserByUsername(serverSlashedForm);
		
		assertThat(loadedUser.getUsername()).isEqualTo("localhost");
		assertThat(loadedUser.getAuthorities()).hasSize(2);
		
		assertTrue(loadedUser.getAuthorities().contains(roleUser));
		assertTrue(loadedUser.getAuthorities().contains(roleServer));
	}
}
