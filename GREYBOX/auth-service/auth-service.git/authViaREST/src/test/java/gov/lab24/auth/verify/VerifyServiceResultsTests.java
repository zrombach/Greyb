package gov.lab24.auth.verify;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import gov.lab24.auth.verify.VerifyServiceResults.LocalLogger;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.xml.sax.SAXException;


public class VerifyServiceResultsTests {
	
	private VerifyServiceResults verifyTask;
	private LocalLogger logger;
	
	String envFoo = "foo";
	String envBar = "bar";
	
	@Before
	public void setup() {
		VerifyEnvConfiguration env = mock(VerifyEnvConfiguration.class);
		verifyTask = new VerifyServiceResults(env);
		
		logger = mock(LocalLogger.class);
	}
	
	@Test
	public void JSONCompareWrappingResponseNotMatching() {
				
		String response1 = "/*{\"dn\":\"foo\"}*/";
		String response2 = "{\"dn\":\"foo\"}";
		verifyTask.compareContentAsJSON(logger, envFoo, response1, envBar, response2);
		verify(logger).logTestData(contains("JSON wrapping"));
				
	}
	
	@Test
	public void JSONCompareWrappingResponseMatching() {
				
		String response1 = "/*{\"dn\":\"foo\"}*/";
		String response2 = "/*{\"dn\":\"foo\"}*/";
		verifyTask.compareContentAsJSON(logger, envFoo, response1, envBar, response2);
		
		// no messages...
		verify(logger, times(0)).logTestData(anyString());		
	}
	
	@Test
	public void JSONCompareFieldsOverlap() {
		
		String responseFoo = "/*{\"dn\":\"foo\"}*/";
		String responseBar = "/*{\"dn\":\"foo\", \"organization\": []}*/";
        
		verifyTask.compareContentAsJSON(logger, envFoo, responseFoo, envBar, responseBar);
		verify(logger).logTestData(contains("Fields do not match"));
		verify(logger).logTestData(contains(String.format("%s: %s", envFoo, "organization")));
	
	}
	
	@Test
	public void JSONCompareFieldsDoNotOverlap() {
		
		String responseFoo = "/*{\"bug\":\"foo\"}*/";
		String responseBar = "/*{\"dn\":\"foo\", \"organization\": []}*/";
        
		verifyTask.compareContentAsJSON(logger, envFoo, responseFoo, envBar, responseBar);
		verify(logger).logTestData(contains("Fields do not match"));
		verify(logger).logTestData(contains(String.format("%s: %s", envBar, "bug")));
		verify(logger).logTestData(contains(String.format("%s: %s", envFoo, "dn")));
		verify(logger).logTestData(contains(String.format("%s: %s", envFoo, "organization")));

	}

	@Test
	public void JSONCompareSimilarButDifferentNames() {
		
		String responseFoo = "/*{\"organization\": []}*/";
		String responseBar = "/*{\"organizations\": []}*/";
        
		verifyTask.compareContentAsJSON(logger, envFoo, responseFoo, envBar, responseBar);
		verify(logger).logTestData(contains("Fields do not match"));
		verify(logger).logTestData(contains(String.format("%s: %s", envFoo, "organizations")));
		verify(logger).logTestData(contains(String.format("%s: %s", envBar, "organization")));
	
	}

	
	@Test
	public void JSONHandleWrappingAndFieldNames() {
		
		String responseFoo = "{\"organization\": []}";
		String responseBar = "/*{\"organizations\": []}*/";
        
		verifyTask.compareContentAsJSON(logger, envFoo, responseFoo, envBar, responseBar);
		verify(logger).logTestData(contains("JSON wrapping"));
		verify(logger).logTestData(contains("Fields do not match"));
		verify(logger).logTestData(contains(String.format("%s: %s", envFoo, "organizations")));
		verify(logger).logTestData(contains(String.format("%s: %s", envBar, "organization")));
	
	}
	
	@Test 
	public void XMLCompareFieldsOverlap() throws ParserConfigurationException, SAXException, IOException {
		
		String responseFoo = "<user><displayName>foo bar</displayName><dn>foo</dn><fullName>Foo Bar Baz</fullName><lastName>Bar</lastName><uid>fbar1</uid></user>";
		String responseBar = "<user><displayName>foo bar</displayName><dn>foo</dn><fullName>Foo Bar Baz</fullName><lastName>Bar</lastName></user>";
		
		verifyTask.compareContentAsXML(logger, envFoo, responseFoo, envBar, responseBar);
		verify(logger).logTestData(contains("Fields do not match"));
		verify(logger).logTestData(contains(String.format("%s: %s", envBar, "uid")));
	
	}
	
	@Test
	public void XMLCompareFieldsDoNotOverlap() throws Exception {
		
		String responseFoo = "<user><bug>foo bar</bug><fullName>Foo Bar Baz</fullName><lastName>Bar</lastName><uid>fbar1</uid></user>";
		String responseBar = "<user><organization>foo bar</organization><dn>foo</dn><fullName>Foo Bar Baz</fullName><lastName>Bar</lastName></user>";
        
		verifyTask.compareContentAsXML(logger, envFoo, responseFoo, envBar, responseBar);
		verify(logger).logTestData(contains("Fields do not match"));
		verify(logger).logTestData(contains(String.format("%s: %s", envBar, "bug")));
		verify(logger).logTestData(contains(String.format("%s: %s", envFoo, "dn")));
		verify(logger).logTestData(contains(String.format("%s: %s", envFoo, "organization")));

	}

	
	@Test
	public void XMLCompareSimilarButDifferentNames() throws Exception {
		String responseFoo = "<user><displayName>foo bar</displayName><organizations>foo</organizations><fullName>Foo Bar Baz</fullName><lastName>Bar</lastName><uid>fbar1</uid></user>";
		String responseBar = "<user><displayName>foo bar</displayName><organization>foo</organization><fullName>Foo Bar Baz</fullName><lastName>Bar</lastName><uid>fbar1</uid></user>";
		
		verifyTask.compareContentAsXML(logger, envFoo, responseFoo, envBar, responseBar);
		verify(logger).logTestData(contains("Fields do not match"));
		verify(logger).logTestData(contains(String.format("%s: %s", envFoo, "organization")));
		verify(logger).logTestData(contains(String.format("%s: %s", envBar, "organizations")));

	}

}
