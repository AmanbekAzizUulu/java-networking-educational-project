package com.dandaev.edu.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class HttpDownloader {
	public static void main(String[] args) {
		var responseFile = new File("src/main/resources/response.html");
		var copyFile = new File("src/main/resources/response_copy.html");

		try (var os = new FileOutputStream(responseFile);
			 var fileOs = new FileOutputStream(copyFile)) {

			URL googleUrl = new URI("https://google.com").toURL();
			writeToFileFromUrl(os, googleUrl);


			URL localFileUrl = new URI("file:D:/Java/EducationalProjects_VSCode/http-servlets-demo-application/heap-application/http-servlets-demo-application/src/main/resources/response.html").toURL();
			writeToFileFromUrl(fileOs, localFileUrl);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private static void writeToFileFromUrl(OutputStream outputStream, URL url) {
		try (var is = url.openConnection().getInputStream();) {
			is.transferTo(outputStream);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
