package com.dandaev.edu.http.server.entities;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
	private String method;
	private String path;
	private String version;
	private Map<String, String> headers;
	private String body;

	public HttpRequest(String method, String path, String version) {
		this.method = method;
		this.path = path;
		this.version = version;
		this.headers = new HashMap<>();
	}

	// Getters and setters
	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public String getVersion() {
		return version;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	public String getHeader(String key) {
		return headers.get(key);
	}
}
