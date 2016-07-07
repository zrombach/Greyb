package gov.lab24.auth.verify;

import io.dropwizard.servlets.tasks.Task;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;


/**
 * A task, executable via curl -X POST "http://[dropWizardURL]:8081/tasks/verify?env1=...&env2=..[&test=testName]"
 * 
 * - Quotes around URL are important to get second(+) parameters for environment
 * - Expects information in the application YAML (authServiceConfig.yml) for the env instances against which we're interacting.
 * 
 * Objective: interrogate information in this service, versus that in another, and make sure that responses are the same, in terms of 
 *  - field names
 *  - field types
 *  - error codes (done)
 *  - content types (done)
 *   
 * Intent: allow to verify for dev and production in enterprise environment, as well as against what this service provides
 * 
 * All output needs to be handled within local scope - tasks are reused.  That specifically impacts output logs - see handle to file stream passed around/through.
 */
public class VerifyServiceResults extends Task {

	static final String TASK_HANDLE = "verify";
	private VerifyEnvConfiguration factory;
	static final String ALL_TASKS = "all";
 	
	final static Logger logger = LoggerFactory.getLogger(VerifyServiceResults.class);
	
	@Inject
	public VerifyServiceResults(VerifyEnvConfiguration env) {
		super(TASK_HANDLE);
		
		this.factory = env;
	}

	@Override
	public void execute(ImmutableMultimap<String, String> args, PrintWriter writer) throws Exception {

		writer.println("Executing tests to compare service results");
		
		String envRequest1 = args.get("env1").asList().get(0);
		String envRequest2 = args.get("env2").asList().get(0);

		FileWriter outputLog = setupUniqueLog(envRequest1, envRequest2);
		LocalLogger localLogger = new LocalLogger(outputLog, writer);
		localLogger.logTestData(String.format("AuthService: Comparing environment[%s] to environment[%s], with test run begun at %s", envRequest1, envRequest2,(new Date()).toString()));
		
		VerifyContext environment1 = factory.getEnvironment(envRequest1);
		VerifyContext environment2 = factory.getEnvironment(envRequest2);
		localLogger.logTestData(String.format("  %s: %s", envRequest1, environment1.toString()));
		localLogger.logTestData(String.format("  %s: %s", envRequest2, environment2.toString()));

		try {						
			ImmutableCollection<String> testArg = args.get("test");
			if (testArg != null && testArg.size() > 0) {
				String test = testArg.asList().get(0);		
				if  (test.equals(ALL_TASKS)) {
					localLogger.logTestData("Executing all tests - explicit");
					executeAllTasks(envRequest1, envRequest2, localLogger);
				} else {
					Iterator<Entry<String, String>> iterator = factory.endpoints.entrySet().iterator();
					do {
						Entry<String, String> endpointEntry = iterator.next();
						String endpointKey = endpointEntry.getValue();
						if (endpointKey != null) {
							if (endpointKey.equals(test)) {
								localLogger.logTestData("Executing single test: " + endpointEntry.getKey());
								executeSingleTest(endpointEntry.getKey(), envRequest1, envRequest2, localLogger);
								break;
							}
						}
					} while (iterator.hasNext());										
				}
			} else {
				localLogger.logTestData("Executing all tests - default");
				executeAllTasks(envRequest1, envRequest2, localLogger);
			}
			
			localLogger.logTestData("AuthService: completed execution.");
		} finally {			
			outputLog.flush();
			outputLog.close();
		}
	}

	private void executeAllTasks(String envRequest1, String envRequest2, LocalLogger localLogger) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, ParserConfigurationException, SAXException {
		
		for (String endpoint : factory.endpoints.keySet()) {
			executeSingleTest(endpoint, envRequest1, envRequest2, localLogger );					
		}
	}

	/**
	 * Run once with env1, once with env2, and then compare
	 *   Do this for requestType of json, and then again with requestType for xml
	 * @param endpoint
	 * @param envRequest1
	 * @param envRequest2
	 * @param localLogger
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private void executeSingleTest( String endpoint, String envRequest1, String envRequest2, LocalLogger localLogger) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, 
		CertificateException, FileNotFoundException, IOException, ParserConfigurationException, SAXException {
		
		String requestType, response1String, response2String;			

		VerifyContext environment1 = factory.getEnvironment(envRequest1);
		List<String> environment1Endpoints = environment1.endpointSubstitution(endpoint);		
		Client client1 = buildClient(environment1);

		VerifyContext environment2 = factory.getEnvironment(envRequest2);
		List<String> environment2Endpoints = environment2.endpointSubstitution(endpoint);		
		Client client2 = buildClient(environment2);
		
		// making a base assumption that we end up with the same number of endpoint substitutions per environment...
		if (environment1Endpoints.size() != environment2Endpoints.size()) {
			throw new RuntimeException("Verify configuration of environments - do not end up with matching quantities of endpoints to validate.");
		}
				
		for (int i= 0; i < environment1Endpoints.size(); i++)  {
			
			requestType = "application/json";
			WebTarget target1 = client1.target(environment1Endpoints.get(i));
			Response response1 = target1.request(requestType).get();

			WebTarget target2 = client2.target(environment2Endpoints.get(i));
			Response response2 = target2.request(requestType).get();

			// compare responses
			//    status codes
			//    content-type
			//    responses back
			compareResponse(localLogger, endpoint, envRequest1, response1, envRequest2, response2, requestType);

			if (response1.getStatus() < 400 && response2.getStatus() < 400) {
				response1String = target1.request(requestType).get(String.class);		
				response2String = target1.request(requestType).get(String.class);
				compareContentAsJSON(localLogger, envRequest1, response1String, envRequest2, response2String);
			} else {
				localLogger.logTestData( "Unable to compare response structures returned, based on response codes.");
			}

			requestType = "application/xml";
			response1 = target1.request(requestType).get();
			response2 = target2.request(requestType).get();
			compareResponse(localLogger, endpoint, envRequest1, response1, envRequest2, response2, requestType);

			// Format the full response returned as a String, to let us do parsing without requiring specific knowledge of type
			if (response1.getStatus() < 400 && response2.getStatus() < 400) {
				response1String = target1.request(requestType).get(String.class);
				response2String = target2.request(requestType).get(String.class);
				compareContentAsXML(localLogger, envRequest1, response1String, envRequest2, response2String);
			} else {
				localLogger.logTestData("Unable to compare response structures returned, based on response codes.");
			}
		}								

	}
	
	/**
	 * Want one log file per Task execution 
	 *   Indicate request/response, etc.
	 * Note: since tasks are run via servlet, one could get 5 POST requests in a row: each invocation should get its own log file
	 * @throws IOException 
	 */
	private FileWriter setupUniqueLog(String envRequest1, String envRequest2) throws IOException {
				
		// use seconds + threadId to generate uniqueID: if it's occurring at the same time, it'd be in a separate thread
		//  if it's not occurring at the same time, the threadID may be reused
		long threadId = Thread.currentThread().getId();
		
		String fileName = String.format("Verify_%s_%s-%d-%d", envRequest1, envRequest2, threadId, new Date().getTime());  				
		File uniqueLog = new File(factory.getVerifyLogsLocation() + fileName);
		FileWriter logWriter = new FileWriter(uniqueLog);

		return logWriter;
	}


	private Client buildClient(VerifyContext environment) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {
		
		SSLContext sslContext = SSLContext.getInstance("TLS");
		
		KeyStore trustStore;
		trustStore = KeyStore.getInstance("JKS");
		trustStore.load(new FileInputStream(environment.keyStorePath), environment.getDeobfuscatedPassword().toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(trustStore);
		
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(trustStore, environment.getDeobfuscatedPassword().toCharArray());
				
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
		Client client1 = ClientBuilder.newBuilder()
				.sslContext(sslContext)
				.build();		

		return client1;
	}
	
	void compareResponse( LocalLogger log, String requestURL, String envRequest1, Response response1,  String envRequest2, Response response2, String requestType ) {
		
		log.logTestData(String.format("Test of %s: comparing responses for %s and %s using requestType %s", 
				requestURL, envRequest1, envRequest2, requestType));
		
		StatusType statusInfo1 = response1.getStatusInfo();
		StatusType statusInfo2 = response2.getStatusInfo();
		
		if (statusInfo1.getStatusCode() != statusInfo2.getStatusCode()) {
			log.logTestData(String.format("  @@@@Status Code: %s returned %s in %s vs %s in %s", 
					requestURL, statusInfo1.getStatusCode(), envRequest1, statusInfo2.getStatusCode(), envRequest2));
		} else {
			log.logTestData(String.format("  Status Code: %s returned %s in %s vs %s in %s", 
					requestURL, statusInfo1.getStatusCode(), envRequest1, statusInfo2.getStatusCode(), envRequest2));			
		}
		if (!statusInfo1.getReasonPhrase().equals(statusInfo2.getReasonPhrase())) {
			log.logTestData(String.format("  @@@@Status Reason: %s returned %s in %s vs %s in %s", 
					requestURL, statusInfo1.getReasonPhrase(), envRequest1, statusInfo2.getReasonPhrase(), envRequest2));
		} else {
			log.logTestData(String.format("  Status Reason: %s returned %s in %s vs %s in %s", 
					requestURL, statusInfo1.getReasonPhrase(), envRequest1, statusInfo2.getReasonPhrase(), envRequest2));
		}
		
		String contentType1 = response1.getHeaderString(HttpHeaders.CONTENT_TYPE);
		String contentType2 = response2.getHeaderString(HttpHeaders.CONTENT_TYPE);
		if (!contentType1.equals(contentType2)) {
			log.logTestData(String.format("  @@@@ContentType: %s returned %s in %s vs %s in %s.", 
					requestURL, contentType1, envRequest1, contentType2, envRequest2));
		} else {
			log.logTestData(String.format("  ContentType: %s returned %s in %s vs %s in %s.", 
					requestURL, contentType1, envRequest1, contentType2, envRequest2));
		}
				
	}


	//  Would like to generally map to Object
	void compareContentAsJSON(LocalLogger log, String envRequest1, String response1, String envRequest2, String response2) {

		boolean isWrapped1 = false, isWrapped2 = false;
		
		// first, strip off any comment wrappers, so we can appropriately parse
		if (response2.endsWith("*/")) {
			response2 = response2.substring(0, response2.length() - 2);
			if (response2.startsWith("/*")) {
				response2 = response2.substring(2);
			}
			isWrapped2 = true;
		}
		
		if (response1.endsWith("*/")) {
			response1 = response1.substring(0, response1.length() - 2);
			if (response1.startsWith("/*")) {
				response1 = response1.substring(2);
			}
			isWrapped1 = true;
		}

		if (isWrapped1 != isWrapped2) {
			log.logTestData( String.format("  @@@@JSON wrapping: JSON in %s is %b, in %s is %b", envRequest1, isWrapped1, envRequest2, isWrapped2));
		}
		
		BasicDBObject parsedResponse1 = (BasicDBObject) JSON.parse(response1);
		BasicDBObject parsedResponse2 = (BasicDBObject) JSON.parse(response2);
		
		CheckResponseJSONConsumer checkResponses = new CheckResponseJSONConsumer();
		checkResponses.setContext(envRequest1, parsedResponse1);
		parsedResponse2.keySet().forEach(checkResponses);

		checkResponses.setContext(envRequest2, parsedResponse2);
		parsedResponse1.keySet().forEach(checkResponses);
				
		outputFieldCompareResults(log, checkResponses.deltas);
		
	}
	
	private void outputFieldCompareResults(LocalLogger log, Map<String, String> deltas) {
		// now, output results...
		if (!deltas.isEmpty()) {
			log.logTestData( "  @@@@Fields do not match - see below for field existing in other response, but missing in the listed one.");
			
			Iterator<Entry<String, String>> iterator = deltas.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> difference = iterator.next();
				log.logTestData(String.format("  -- %s: %s", difference.getValue(), difference.getKey()));
			}
			
		}		
	}

	// Again, generically parsing and examining fields, since we'd like to use this in more contexts and don't want to assume that our JSON mappings for this completely map well.
	void compareContentAsXML(LocalLogger log, String envRequest1, String response1, String envRequest2, String response2) throws ParserConfigurationException, SAXException, IOException {
	
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		
		LocalSAXParser parsedXML1 = parseXML(spf, response1);
		LocalSAXParser parsedXML2 = parseXML(spf, response2);
		
		// compare token sets received...
		List<String> entriesXML1 = parsedXML1.tokens;
		List<String> entriesXML2 = parsedXML2.tokens;
	
		CheckResponseXMLConsumer checkResponses = new CheckResponseXMLConsumer();
		checkResponses.setContext(envRequest1, entriesXML1);
		entriesXML2.forEach(checkResponses);

		checkResponses.setContext(envRequest2, entriesXML2);
		entriesXML1.forEach(checkResponses);
		
		outputFieldCompareResults(log, checkResponses.deltas);
				
	}
	
	private LocalSAXParser parseXML( SAXParserFactory spf, String responseAsString ) throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = spf.newSAXParser();
		InputStream xmlStream1 = new ByteArrayInputStream(responseAsString.getBytes());
		LocalSAXParser responseParser = new LocalSAXParser();
		parser.parse(xmlStream1, responseParser );
		
		return responseParser;
	}

	private class CheckResponseJSONConsumer implements Consumer<String> {
		
		BasicDBObject compareTo;
		String compareName;
		
		// using TreeMap so when we get the values out, they'll be sorted in alphabetical order...
		Map<String, String> deltas = new TreeMap<String, String>();
		
		public void accept(String t) {
			if (!(compareTo.keySet().contains(t))) {
				deltas.put(t, compareName);
			}
		}
		
		public void setContext (String compareSetName, BasicDBObject compareObject) {
			this.compareName = compareSetName;
			this.compareTo = compareObject;
		}
	}
	
	private class CheckResponseXMLConsumer implements Consumer<String> {

		List<String> compareTo;
		String compareName;

		// using TreeMap so when we get the values out, they'll be sorted in alphabetical order...
		Map<String, String> deltas = new TreeMap<String, String>();

		@Override
		public void accept(String t) {
			if (!(compareTo.contains(t))) {
				deltas.put(t, compareName);
			}

			
		}
		public void setContext (String compareSetName, List<String> compareObject) {
			this.compareName = compareSetName;
			this.compareTo = compareObject;
		}

	}
	
	/**
	 * Since what we're parsing is pretty flat, XML-wise, using a very simple approach of just collecting keys
	 * 
	 */
	private class LocalSAXParser extends DefaultHandler {

		List<String> tokens = new ArrayList<String>();
				
		@Override
		public void startDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.startDocument();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			tokens.add(qName);
		}
		
		
	}
	
	class LocalLogger {
		
		private FileWriter writer;
		private PrintWriter taskWriter;
		
		LocalLogger(FileWriter writer, PrintWriter taskWriter) {
			this.writer = writer;
			this.taskWriter = taskWriter;
		}
		
		void logTestData(String testData) {
			
			try {
				writer.write(testData +"\n");
			} catch (IOException e) {
				taskWriter.write("Caught exception in logging.. ");
				e.printStackTrace(taskWriter);
			}
		}
	
	}
}
