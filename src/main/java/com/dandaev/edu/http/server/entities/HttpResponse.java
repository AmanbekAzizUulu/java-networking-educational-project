package com.dandaev.edu.http.server.entities;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
	private String version;
	private int statusCode;
	private String statusMessage;
	private Map<String, String> headers;
	private String body;

	public HttpResponse(String version, int statusCode, String statusMessage) {
		this.version = version;
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
		this.headers = new HashMap<>();

		// Стандартные заголовки
		this.headers.put("Server", "SimpleJavaHTTPServer/1.0");
		this.headers.put("Date", getCurrentDate());
	}

	// Getters and setters
	public String getVersion() {
		return version;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
		this.headers.put("Content-Length", String.valueOf(body.length()));
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	private String getCurrentDate() {
		return java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(
				java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC));
	}
}
