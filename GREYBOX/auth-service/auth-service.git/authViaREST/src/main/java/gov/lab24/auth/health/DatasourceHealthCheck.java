package gov.lab24.auth.health;

import com.google.inject.Inject;
import com.hubspot.dropwizard.guice.InjectableHealthCheck;
import com.mongodb.Mongo;

public class DatasourceHealthCheck extends InjectableHealthCheck {

	Mongo mongo;
	
	@Inject
	public DatasourceHealthCheck(Mongo mongo) {
		this.mongo = mongo;
	}
	
	@Override
	protected Result check() {
		
		mongo.getDatabaseNames();	// if this throws an exception, we're unhealthy
		return Result.healthy();

	}

	@Override
	public String getName() {
		return "Database Check";
	}

}
