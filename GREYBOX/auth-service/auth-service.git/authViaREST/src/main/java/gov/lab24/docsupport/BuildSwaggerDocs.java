package gov.lab24.docsupport;

import io.github.robwin.swagger2markup.Swagger2MarkupConverter;

import java.io.File;
import java.net.URL;

public class BuildSwaggerDocs {
	
	public static void main(String[] argument) throws Exception {
		
		URL resource = BuildSwaggerDocs.class.getResource("/docs/swagger.yaml");
		String fileName = resource.getFile();
		File file = new File(fileName);
		Swagger2MarkupConverter.from(file.getAbsolutePath()).build().intoFolder("docs");
		
	}
}
