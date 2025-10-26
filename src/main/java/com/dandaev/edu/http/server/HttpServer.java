package com.dandaev.edu.http.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dandaev.edu.http.server.entities.HttpRequest;
import com.dandaev.edu.http.server.entities.HttpResponse;

public class HttpServer {
	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
	private final int port;
	private final ExecutorService threadPool;
	private volatile boolean isRunning = false;
	private ServerSocket serverSocket;
	private Thread serverThread;

	public HttpServer(int port) {
		this.port = port;
		this.threadPool = Executors.newFixedThreadPool(10);
	}

	public HttpServer(int port, int threadPoolSize) {
		this.port = port;
		this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
	}

	public void start() {
		if (isRunning) {
			logger.warn("Server is already running");
			return;
		}

		serverThread = new Thread(this::runServer, "HTTP-Server-Thread");
		serverThread.start();
	}

	private void runServer() {
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(1000); // Таймаут 1 секунда для accept()
			isRunning = true;

			logger.info("HTTP Server started on port {}", port);
			logger.info("Server is listening for connections...");
			logger.info("Access the server at: http://localhost:{}/", port);

			while (isRunning) {
				try {
					Socket clientSocket = serverSocket.accept();
					logger.debug("New connection from: {}", clientSocket.getInetAddress().getHostAddress());

					// Обрабатываем клиента в отдельном потоке
					threadPool.submit(() -> processSocket(clientSocket));

				} catch (SocketTimeoutException e) {
					// Таймаут - нормальная ситуация, проверяем флаг isRunning
					continue;
				} catch (IOException e) {
					if (isRunning) {
						logger.error("Error accepting client connection", e);
					}
					break;
				}
			}
		} catch (IOException e) {
			logger.error("Failed to start HTTP server on port {}", port, e);
		} finally {
			stop();
		}
	}

	public void stop() {
		if (!isRunning) {
			return;
		}

		isRunning = false;
		logger.info("Shutting down HTTP server...");

		// Закрываем server socket
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				logger.error("Error closing server socket", e);
			}
		}

		// Останавливаем пул потоков
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
				threadPool.shutdownNow();
				logger.warn("Forced thread pool shutdown");
			}
		} catch (InterruptedException e) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}

		logger.info("HTTP server stopped successfully");
	}

	public boolean isRunning() {
		return isRunning;
	}

	private void processSocket(Socket socket) {
		String clientAddress = socket.getInetAddress().getHostAddress();

		try (socket; // Java 9+ try-with-resources с переменной
				var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				var writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

			// Устанавливаем таймаут на чтение
			socket.setSoTimeout(30000); // 30 секунд

			// Чтение HTTP запроса
			HttpRequest httpRequest = parseHttpRequest(reader);
			if (httpRequest == null) {
				logger.warn("Empty or invalid request from {}", clientAddress);
				return;
			}

			logger.info("{} {} {} from {}",
					httpRequest.getMethod(),
					httpRequest.getPath(),
					httpRequest.getVersion(),
					clientAddress);

			// Обработка запроса
			HttpResponse httpResponse = handleHttpRequest(httpRequest);

			// Логирование ответа
			logger.info("Response: {} {} for {} {}",
					httpResponse.getStatusCode(),
					httpResponse.getStatusMessage(),
					httpRequest.getMethod(),
					httpRequest.getPath());

			// Отправка HTTP ответа
			sendHttpResponse(writer, httpResponse);

		} catch (IOException e) {
			logger.error("Error processing request from {}", clientAddress, e);
		} catch (Exception e) {
			logger.error("Unexpected error processing request from {}", clientAddress, e);
		}
	}

	// Улучшенный парсинг запроса
	private HttpRequest parseHttpRequest(BufferedReader reader) throws IOException {
		String requestLine = reader.readLine();
		if (requestLine == null || requestLine.isEmpty()) {
			return null;
		}

		// Парсим первую строку: METHOD PATH VERSION
		String[] requestParts = requestLine.split(" ", 3); // limit = 3 чтобы не разбивать путь с пробелами
		if (requestParts.length != 3) {
			throw new IOException("Invalid request line: " + requestLine);
		}

		String method = requestParts[0];
		String path = requestParts[1];
		String version = requestParts[2];

		HttpRequest request = new HttpRequest(method, path, version);

		// Читаем заголовки
		String headerLine;
		while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
			int colonIndex = headerLine.indexOf(':');
			if (colonIndex > 0) {
				String key = headerLine.substring(0, colonIndex).trim();
				String value = headerLine.substring(colonIndex + 1).trim();
				request.addHeader(key, value);
			}
		}

		// Читаем тело запроса, если есть
		String contentLengthHeader = request.getHeader("Content-Length");
		if (contentLengthHeader != null) {
			try {
				int contentLength = Integer.parseInt(contentLengthHeader);
				if (contentLength > 0 && contentLength <= 1024 * 1024) { // Максимум 1MB
					char[] bodyChars = new char[contentLength];
					int totalRead = 0;
					while (totalRead < contentLength) {
						int bytesRead = reader.read(bodyChars, totalRead, contentLength - totalRead);
						if (bytesRead == -1) {
							break;
						}
						totalRead += bytesRead;
					}
					if (totalRead == contentLength) {
						request.setBody(new String(bodyChars, 0, totalRead));
					}
				}
			} catch (NumberFormatException e) {
				logger.warn("Invalid Content-Length: {} from request", contentLengthHeader);
			}
		}

		return request;
	}

	// Обработка HTTP запроса
	private HttpResponse handleHttpRequest(HttpRequest request) {
		String method = request.getMethod();
		String path = request.getPath();

		System.out.println("Processing " + method + " " + path);

		HttpResponse response;

		try {
			switch (method) {
				case "GET":
					response = handleGetRequest(request);
					break;
				case "POST":
					response = handlePostRequest(request);
					break;
				case "PUT":
					response = handlePutRequest(request);
					break;
				case "DELETE":
					response = handleDeleteRequest(request);
					break;
				case "HEAD":
					response = handleHeadRequest(request);
					break;
				default:
					response = new HttpResponse("HTTP/1.1", 405, "Method Not Allowed");
					response.setBody("<!DOCTYPE html><html><head><title>405 Method Not Allowed</title></head>" +
							"<body><h1>405 Method Not Allowed</h1><p>Method " + method
							+ " is not supported.</p></body></html>");
					response.addHeader("Allow", "GET, POST, PUT, DELETE, HEAD");
			}
		} catch (Exception e) {
			response = new HttpResponse("HTTP/1.1", 500, "Internal Server Error");
			response.setBody("<!DOCTYPE html><html><head><title>500 Internal Server Error</title></head>" +
					"<body><h1>500 Internal Server Error</h1><p>" + e.getMessage() + "</p></body></html>");
		}

		// Добавляем общие заголовки
		response.addHeader("Connection", "close");

		return response;
	}

	// Обработка GET запроса
	private HttpResponse handleGetRequest(HttpRequest request) {
		String path = request.getPath();
		HttpResponse response;

		if ("/".equals(path)) {
			response = new HttpResponse("HTTP/1.1", 200, "OK");
			response.addHeader("Content-Type", "text/html; charset=utf-8");
			String html = "<!DOCTYPE html>" +
					"<html>" +
					"<head><title>Simple HTTP Server</title></head>" +
					"<body>" +
					"<h1>Welcome to Simple HTTP Server</h1>" +
					"<p>Server is running successfully!</p>" +
					"<ul>" +
					"<li><a href='/time'>Current Time</a></li>" +
					"<li><a href='/info'>Server Info</a></li>" +
					"</ul>" +
					"</body>" +
					"</html>";
			response.setBody(html);
		} else if ("/time".equals(path)) {
			response = new HttpResponse("HTTP/1.1", 200, "OK");
			response.addHeader("Content-Type", "application/json; charset=utf-8");
			String json = "{\"current_time\": \"" + java.time.LocalDateTime.now() + "\"}";
			response.setBody(json);
		} else if ("/info".equals(path)) {
			response = new HttpResponse("HTTP/1.1", 200, "OK");
			response.addHeader("Content-Type", "application/json; charset=utf-8");
			String json = "{" +
					"\"server\": \"SimpleJavaHTTPServer\"," +
					"\"version\": \"1.0\"," +
					"\"java_version\": \"" + System.getProperty("java.version") + "\"," +
					"\"available_memory\": \"" + Runtime.getRuntime().freeMemory() + " bytes\"" +
					"}";
			response.setBody(json);
		} else {
			response = new HttpResponse("HTTP/1.1", 404, "Not Found");
			response.addHeader("Content-Type", "text/html; charset=utf-8");
			String html = "<!DOCTYPE html>" +
					"<html>" +
					"<head><title>404 Not Found</title></head>" +
					"<body>" +
					"<h1>404 Not Found</h1>" +
					"<p>The requested URL " + path + " was not found on this server.</p>" +
					"</body>" +
					"</html>";
			response.setBody(html);
		}

		return response;
	}

	// Обработка POST запроса
	private HttpResponse handlePostRequest(HttpRequest request) {
		String path = request.getPath();
		String body = request.getBody();
		HttpResponse response;

		if ("/echo".equals(path)) {
			response = new HttpResponse("HTTP/1.1", 200, "OK");
			response.addHeader("Content-Type", "text/plain; charset=utf-8");
			response.setBody("Echo: " + (body != null ? body : "No body received"));
		} else if ("/api/data".equals(path)) {
			// Простая имитация API endpoint
			response = new HttpResponse("HTTP/1.1", 201, "Created");
			response.addHeader("Content-Type", "application/json; charset=utf-8");
			String jsonResponse = "{" +
					"\"status\": \"success\"," +
					"\"message\": \"Data received\"," +
					"\"received_data\": " + (body != null ? body : "null") +
					"}";
			response.setBody(jsonResponse);
		} else {
			response = new HttpResponse("HTTP/1.1", 404, "Not Found");
			response.addHeader("Content-Type", "application/json; charset=utf-8");
			String json = "{\"error\": \"Endpoint not found\", \"path\": \"" + path + "\"}";
			response.setBody(json);
		}

		return response;
	}

	// Обработка PUT запроса
	private HttpResponse handlePutRequest(HttpRequest request) {
		String path = request.getPath();
		HttpResponse response;

		if (path.startsWith("/api/resources/")) {
			response = new HttpResponse("HTTP/1.1", 200, "OK");
			response.addHeader("Content-Type", "application/json; charset=utf-8");
			String resourceId = path.substring("/api/resources/".length());
			String json = "{" +
					"\"status\": \"updated\"," +
					"\"resource_id\": \"" + resourceId + "\"," +
					"\"message\": \"Resource updated successfully\"" +
					"}";
			response.setBody(json);
		} else {
			response = new HttpResponse("HTTP/1.1", 404, "Not Found");
			response.addHeader("Content-Type", "application/json; charset=utf-8");
			String json = "{\"error\": \"Resource not found\", \"path\": \"" + path + "\"}";
			response.setBody(json);
		}

		return response;
	}

	// Обработка DELETE запроса
	private HttpResponse handleDeleteRequest(HttpRequest request) {
		String path = request.getPath();
		HttpResponse response;

		if (path.startsWith("/api/resources/")) {
			response = new HttpResponse("HTTP/1.1", 200, "OK");
			response.addHeader("Content-Type", "application/json; charset=utf-8");
			String resourceId = path.substring("/api/resources/".length());
			String json = "{" +
					"\"status\": \"deleted\"," +
					"\"resource_id\": \"" + resourceId + "\"," +
					"\"message\": \"Resource deleted successfully\"" +
					"}";
			response.setBody(json);
		} else {
			response = new HttpResponse("HTTP/1.1", 404, "Not Found");
			response.addHeader("Content-Type", "application/json; charset=utf-8");
			String json = "{\"error\": \"Resource not found\", \"path\": \"" + path + "\"}";
			response.setBody(json);
		}

		return response;
	}

	// Обработка HEAD запроса
	private HttpResponse handleHeadRequest(HttpRequest request) {
		// HEAD запрос аналогичен GET, но без тела
		HttpResponse response = handleGetRequest(request);
		response.setBody(""); // Убираем тело для HEAD запроса
		return response;
	}

	// Отправка HTTP ответа
	private void sendHttpResponse(PrintWriter writer, HttpResponse response) {
		// Status line
		writer.print(response.getVersion() + " " +
				response.getStatusCode() + " " +
				response.getStatusMessage() + "\r\n");

		// Headers
		for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
			writer.print(header.getKey() + ": " + header.getValue() + "\r\n");
		}

		// Empty line between headers and body
		writer.print("\r\n");

		// Body (если есть)
		if (response.getBody() != null && !response.getBody().isEmpty()) {
			writer.print(response.getBody());
		}

		writer.flush();
	}
}
