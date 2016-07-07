package gov.lab24.auth.verify;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Condition;

/**
 * Simplification to let us chain assertions for verifying the config information received.
 * 
 * Still experimenting with whether to better use custom assertions or conditions
 *
 */
public class VerifyConfigAssert extends AbstractAssert<VerifyConfigAssert, VerifyContext> {

	public VerifyConfigAssert(VerifyContext actual) {
		super(actual, VerifyConfigAssert.class);
	}
	
	public static VerifyConfigAssert assertThat(VerifyContext actual) {
		return new VerifyConfigAssert(actual);
	}
	
	public VerifyConfigAssert isFullyPopulated() {
		
		
		//TODO: is there a way to do this with soft assertions, so we get the full set here?
		String fullyPopulatedMessage = " path, password & baseURL must be populated.  %s was null";
		isNotNull();
		
		if (actual.keyStorePath == null) {
			failWithMessage(fullyPopulatedMessage, "keyStorePath");
		}
		if (actual.baseURL == null) {
			failWithMessage(fullyPopulatedMessage, "baseURL");			
		}
		if (actual.keyStorePassword == null) {
			failWithMessage(fullyPopulatedMessage, "keyStorePassword");
		}
		return this;
	}
	
	static final Condition<VerifyContext> populated = new Condition<VerifyContext>("populated") {
		
		@Override
		public boolean matches(VerifyContext value) {
			if (value.keyStorePath == null) {
				return false;
				//failWithMessage(fullyPopulatedMessage, "keyStorePath");
			}
			if (value.baseURL == null) {
				return false;
				///failWithMessage(fullyPopulatedMessage, "baseURL");			
			}
			if (value.keyStorePassword == null) {
				return false;
				//failWithMessage(fullyPopulatedMessage, "keyStorePassword");
			}
			return true;
		}
	};
}
