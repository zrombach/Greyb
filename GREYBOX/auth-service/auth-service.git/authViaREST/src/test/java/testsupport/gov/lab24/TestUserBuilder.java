package testsupport.gov.lab24;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class TestUserBuilder {

	static SimpleGrantedAuthority roleUser = new SimpleGrantedAuthority("ROLE_USER");
	static SimpleGrantedAuthority roleServer = new SimpleGrantedAuthority("ROLE_SERVER");
	static Collection<GrantedAuthority> userAuthorities = new ArrayList<GrantedAuthority>();
	static Collection<GrantedAuthority> serverAuthorities = new ArrayList<GrantedAuthority>();

	static {
		serverAuthorities.add(roleServer);
		serverAuthorities.add(roleUser);
		
		userAuthorities.add(roleUser);
	}

	public static UserDetails buildBasicUser(String username) {
		UserDetails userRequesting = mock(UserDetails.class);
		if (username!=null) {
			when(userRequesting.getUsername()).thenReturn(username);
		} else {
			when(userRequesting.getUsername()).thenReturn("basic");
		}
		
		//  See 'Mocking a method that returns generics with wildcard': http://stackoverflow.com/questions/15942880/mocking-a-method-that-return-generics-with-wildcard-using-mockito 
		Mockito.<Collection<? extends GrantedAuthority>>when(userRequesting.getAuthorities()).thenReturn(TestUserBuilder.userAuthorities);
		return userRequesting;		
	}

	public static UserDetails buildServerUser() {
		UserDetails userRequesting = mock(UserDetails.class);
		when(userRequesting.getUsername()).thenReturn("server,OU=DX009");
		Mockito.<Collection<? extends GrantedAuthority>>when(userRequesting.getAuthorities()).thenReturn(serverAuthorities);
		return userRequesting;		
	}

}
