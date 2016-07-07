package gov.lab24.auth.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.base.Splitter;

public class CertIdentifiedUserDetails implements UserDetailsService {
		
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		/**
		 * Implementation assumes we're getting full DN to let us check OU.  And that this is generated per our cert generation tool 
		 */
		if (username.contains("OU=D00X")) {
			authorities.add(new SimpleGrantedAuthority("ROLE_SERVER"));
		}
		
		char split;
		if (username.contains("/")) {
			split = '/';			
		} else {
			split = ',';
		}

		/**
		 * and so we're dropping everything after CN for username
		 */
		Iterable<String> splitUserName = Splitter.on(split).split(username);
		
		return new User(splitUserName.iterator().next(), "password", true, true, true, true, authorities);
	}

}
