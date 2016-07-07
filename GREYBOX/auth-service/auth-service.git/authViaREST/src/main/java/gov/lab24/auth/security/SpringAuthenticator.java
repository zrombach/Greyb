package gov.lab24.auth.security;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Optional;

public class SpringAuthenticator implements Authenticator<Object, UserDetails> {

	@Override
	// We're completely ignoring credentials - pulling from the SecurityConext set up through SpringAuthFactory's registration of Spring security filters
	public Optional<UserDetails> authenticate(Object credentials) throws AuthenticationException {
		
		boolean authenticationRequired = true;
		UserDetails userDetails = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (!(auth instanceof AnonymousAuthenticationToken)) {
			userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}
		if (userDetails == null && authenticationRequired) {
			return Optional.absent();
		}
		return Optional.of(userDetails);

		
	}

}
