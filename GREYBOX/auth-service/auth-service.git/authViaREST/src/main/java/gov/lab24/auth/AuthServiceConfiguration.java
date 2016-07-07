package gov.lab24.auth;

import gov.lab24.auth.db.DataSourceFactory;
import gov.lab24.auth.verify.VerifyEnvConfiguration;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthServiceConfiguration extends Configuration {

	@NotEmpty
	private String version;

	@JsonProperty
	public String getVersion() {
		return version;
	}

	@Valid
	@NotNull
	private DataSourceFactory factory = new DataSourceFactory();

	@JsonProperty("datasource")
	public DataSourceFactory getDataSourceFactory() {
		return factory;
	}

	@JsonProperty("datasource")
	public void setDataSourceFactory(DataSourceFactory factory) {
		this.factory = factory;
	}

	@NotEmpty
	private String[] springSecurityConfiguration;

	public String[] getSpringSecurityConfiguration() {
		// TODO Auto-generated method stub
		return springSecurityConfiguration;
	}

	private VerifyEnvConfiguration verifyInfo = new VerifyEnvConfiguration(); 
	@JsonProperty("verifyEnv")
	public VerifyEnvConfiguration getVerifyConfig() {
		return verifyInfo;
	}
	
	@JsonProperty("verifyEnv")
	public void setVerifyConfig(VerifyEnvConfiguration factory) {
		this.verifyInfo = factory;
	}

}
