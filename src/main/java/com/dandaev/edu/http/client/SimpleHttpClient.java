package com.dandaev.edu.http.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimpleHttpClient {
	private static final HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	private static ExecutorService executor = Executors.newFixedThreadPool(3);

	public static void main(String[] args) {
		if (args.length == 0) {
			// Интерактивный режим
			startInteractiveMode();
		} else {
			// Режим командной строки
			executeCommand(args[0]);
		}

		shutdown();
	}

	/**
	 * Интерактивный режим с меню
	 */
	private static void startInteractiveMode() {
		Scanner scanner = new Scanner(System.in);

		System.out.println("╔══════════════════════════════════════╗");
		System.out.println("║      HTTP Client for Testing         ║");
		System.out.println("║         Server: localhost:8081       ║");
		System.out.println("╚══════════════════════════════════════╝");

		while (true) {
			printMenu();
			System.out.print("\nEnter command number or name: ");

			String input = scanner.nextLine().trim();

			if (input.isEmpty()) {
				continue;
			}

			// Обработка команд по номеру или имени
			switch (input.toLowerCase()) {
				case "1":
				case "all":
					sendAllRequests();
					waitForCompletion();
					break;
				case "2":
				case "test":
					sendTestRequests();
					waitForCompletion();
					break;
				case "3":
				case "list":
					listAvailableFiles();
					break;
				case "4":
				case "create":
					createSampleFiles();
					break;
				case "5":
				case "file":
					handleFileCommand(scanner);
					break;
				case "6":
				case "exit":
				case "quit":
					System.out.println("Client stopped.");
					scanner.close();
					return;
				default:
					// Если ввод не распознан как команда, пробуем как имя файла
					if (input.endsWith(".json") || Files.exists(Paths.get("resources", "requests", input + ".json"))) {
						sendSingleRequest(input);
						waitForCompletion();
					} else {
						System.out.println("❌ Unknown command: " + input);
						System.out.println("💡 Tip: Use numbers (1-6) or command names");
					}
			}
		}
	}

	/**
	 * Печатает меню
	 */
	private static void printMenu() {
		System.out.println("\n📋 Available commands:");
		System.out.println("1. all    - Send all requests from resources/requests");
		System.out.println("2. test   - Send test requests to all endpoints");
		System.out.println("3. list   - List available request files");
		System.out.println("4. create - Create sample request files");
		System.out.println("5. file   - Send specific request file");
		System.out.println("6. exit   - Exit client");
	}

	/**
	 * Обработка команды отправки файла
	 */
	private static void handleFileCommand(Scanner scanner) {
		System.out.print("Enter filename (without .json extension): ");
		String fileName = scanner.nextLine().trim();

		if (!fileName.isEmpty()) {
			sendSingleRequest(fileName);
			waitForCompletion();
		} else {
			System.out.println("❌ No filename provided");
		}
	}

	/**
	 * Режим командной строки
	 */
	private static void executeCommand(String command) {
		switch (command.toLowerCase()) {
			case "all":
				sendAllRequests();
				break;
			case "test":
				sendTestRequests();
				break;
			case "list":
				listAvailableFiles();
				return;
			case "create":
				createSampleFiles();
				return;
			default:
				sendSingleRequest(command);
				break;
		}
		waitForCompletion();
	}

	/**
	 * Ждет завершения всех задач
	 */
	private static void waitForCompletion() {
		try {
			executor.shutdown();
			if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
				System.out.println("⏰ Timeout waiting for requests to complete");
			}
			// Пересоздаем ExecutorService для следующих запросов
			executor = Executors.newFixedThreadPool(3);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Корректное завершение
	 */
	private static void shutdown() {
		if (executor != null) {
			executor.shutdownNow();
		}
	}

	/**
	 * Отправляет все JSON файлы из папки resources/requests
	 */
	private static void sendAllRequests() {
		try {
			Path requestsDir = Paths.get("resources", "requests");
			if (!Files.exists(requestsDir)) {
				System.err.println("❌ Directory not found: " + requestsDir.toAbsolutePath());
				System.out.println("📁 Creating sample files...");
				createSampleFiles();
				return;
			}

			System.out.println("📁 Sending all requests from: " + requestsDir.toAbsolutePath());

			var files = Files.list(requestsDir)
					.filter(path -> path.toString().endsWith(".json"))
					.toList();

			if (files.isEmpty()) {
				System.out.println("ℹ️  No JSON files found in resources/requests directory");
				System.out.println("📁 Creating sample files...");
				createSampleFiles();
				files = Files.list(requestsDir)
						.filter(path -> path.toString().endsWith(".json"))
						.toList();
			}

			System.out.println("📤 Found " + files.size() + " request files");

			for (Path file : files) {
				executor.submit(() -> sendRequestFromFile(file));
			}

		} catch (IOException e) {
			System.err.println("❌ Error reading directory: " + e.getMessage());
		}
	}

	/**
	 * Создает примеры JSON файлов
	 */
	private static void createSampleFiles() {
		try {
			Path requestsDir = Paths.get("resources", "requests");
			Files.createDirectories(requestsDir);

			// Создаем примеры файлов
			String[] sampleFiles = {
					"get-home.json",
					"get-time.json",
					"get-info.json",
					"post-echo.json",
					"post-api-data.json",
					"put-resource.json",
					"delete-resource.json"
			};

			String[] sampleContents = {
					"{\n  \"test\": \"home_page_request\",\n  \"timestamp\": \"2024-01-01\"\n}",
					"{\n  \"action\": \"get_time\",\n  \"timezone\": \"UTC\"\n}",
					"{\n  \"request_type\": \"server_info\",\n  \"client\": \"test_client\"\n}",
					"{\n  \"message\": \"Hello Server!\",\n  \"data\": {\n    \"user\": \"test_user\",\n    \"id\": 12345,\n    \"active\": true\n  }\n}",
					"{\n  \"name\": \"Test Object\",\n  \"value\": 42,\n  \"tags\": [\"test\", \"api\", \"json\"],\n  \"metadata\": {\n    \"created_by\": \"client\",\n    \"version\": \"1.0\"\n  }\n}",
					"{\n  \"resource\": {\n    \"id\": 123,\n    \"name\": \"Updated Resource Name\",\n    \"status\": \"active\",\n    \"properties\": {\n      \"color\": \"blue\",\n      \"size\": \"large\"\n    }\n  }\n}",
					"{\n  \"confirmation\": true,\n  \"reason\": \"test_deletion\",\n  \"backup_required\": false\n}"
			};

			int createdCount = 0;
			for (int i = 0; i < sampleFiles.length; i++) {
				Path filePath = requestsDir.resolve(sampleFiles[i]);
				if (!Files.exists(filePath)) {
					Files.writeString(filePath, sampleContents[i]);
					System.out.println("✅ Created: " + filePath.getFileName());
					createdCount++;
				}
			}

			if (createdCount > 0) {
				System.out.println("\n📝 Created " + createdCount + " sample request files.");
			} else {
				System.out.println("ℹ️  All sample files already exist.");
			}

		} catch (IOException e) {
			System.err.println("❌ Error creating sample files: " + e.getMessage());
		}
	}

	/**
	 * Отправляет запрос из конкретного файла
	 */
	private static void sendSingleRequest(String fileName) {
		// Добавляем расширение .json если его нет
		if (!fileName.endsWith(".json")) {
			fileName += ".json";
		}

		Path filePath = Paths.get("resources", "requests", fileName);
		if (!Files.exists(filePath)) {
			System.err.println("❌ File not found: " + filePath.toAbsolutePath());
			System.out.println("📁 Available files in resources/requests:");
			listAvailableFiles();
			return;
		}

		System.out.println("📄 Sending request from: " + fileName);
		executor.submit(() -> sendRequestFromFile(filePath));
	}

	/**
	 * Показывает доступные файлы
	 */
	private static void listAvailableFiles() {
		try {
			Path requestsDir = Paths.get("resources", "requests");
			if (Files.exists(requestsDir)) {
				var files = Files.list(requestsDir)
						.filter(path -> path.toString().endsWith(".json"))
						.toList();

				if (files.isEmpty()) {
					System.out.println("  No files found. Use 'create' command to create sample files.");
				} else {
					System.out.println("  Available files:");
					files.forEach(path -> System.out.println("  - " + path.getFileName()));
				}
			} else {
				System.out.println("  Directory not found. Use 'create' command to create sample files.");
			}
		} catch (IOException e) {
			System.err.println("❌ Error listing files: " + e.getMessage());
		}
	}

	/**
	 * Отправляет тестовые запросы ко всем endpoint'ам сервера
	 */
	private static void sendTestRequests() {
		System.out.println("🧪 Sending test requests to all server endpoints...\n");

		// GET запросы
		sendTestRequest("GET", "/", null);
		sendTestRequest("GET", "/time", null);
		sendTestRequest("GET", "/info", null);
		sendTestRequest("GET", "/nonexistent", null);

		// POST запросы
		String postBody = "{\"message\": \"Hello from test client\", \"test\": true}";
		sendTestRequest("POST", "/echo", postBody);
		sendTestRequest("POST", "/api/data", postBody);
		sendTestRequest("POST", "/nonexistent", postBody);

		// PUT запрос
		String putBody = "{\"name\": \"Test Resource\", \"value\": 123}";
		sendTestRequest("PUT", "/api/resources/999", putBody);
		sendTestRequest("PUT", "/api/resources/invalid", putBody);

		// DELETE запросы
		sendTestRequest("DELETE", "/api/resources/888", null);
		sendTestRequest("DELETE", "/invalid/endpoint", null);

		// HEAD запрос
		sendTestRequest("HEAD", "/", null);

		System.out.println("✅ All test requests sent!");
	}

	private static void sendTestRequest(String method, String endpoint, String body) {
		executor.submit(() -> {
			try {
				HttpRequest request = buildRequest(method, "http://localhost:8081" + endpoint, body);
				HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				printResponse(response, method + " " + endpoint);
			} catch (Exception e) {
				System.err.println("❌ Error sending " + method + " " + endpoint + ": " + e.getMessage());
			}
		});
	}

	private static void sendRequestFromFile(Path filePath) {
		try {
			String fileName = filePath.getFileName().toString();
			String fileContent = Files.readString(filePath);

			// Определяем метод и endpoint из имени файла
			String method = getMethodFromFileName(fileName);
			String endpoint = getEndpointFromFileName(fileName);

			HttpRequest request = buildRequest(method, "http://localhost:8081" + endpoint, fileContent);
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			printResponse(response, fileName);

		} catch (Exception e) {
			System.err.println("❌ Error processing " + filePath.getFileName() + ": " + e.getMessage());
		}
	}

	/**
	 * Определяет HTTP метод из имени файла
	 */
	private static String getMethodFromFileName(String fileName) {
		if (fileName.startsWith("post-"))
			return "POST";
		if (fileName.startsWith("put-"))
			return "PUT";
		if (fileName.startsWith("delete-"))
			return "DELETE";
		if (fileName.startsWith("head-"))
			return "HEAD";
		return "GET"; // по умолчанию
	}

	/**
	 * Определяет endpoint из имени файла
	 */
	private static String getEndpointFromFileName(String fileName) {
		String name = fileName.replace(".json", "");

		switch (name) {
			case "get-home":
				return "/";
			case "get-time":
				return "/time";
			case "get-info":
				return "/info";
			case "post-echo":
				return "/echo";
			case "post-api-data":
				return "/api/data";
			case "put-resource":
				return "/api/resources/123";
			case "delete-resource":
				return "/api/resources/456";
			default:
				return "/";
		}
	}

	/**
	 * Строит HTTP запрос
	 */
	private static HttpRequest buildRequest(String method, String url, String body) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.header("User-Agent", "SimpleHttpClient/1.0")
				.timeout(Duration.ofSeconds(30));

		switch (method.toUpperCase()) {
			case "POST":
				return builder.POST(
						body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody())
						.build();
			case "PUT":
				return builder.PUT(
						body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody())
						.build();
			case "DELETE":
				return builder.DELETE().build();
			case "HEAD":
				return builder.method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
			default: // GET
				return builder.GET().build();
		}
	}

	/**
	 * Выводит ответ в консоль
	 */
	private static void printResponse(HttpResponse<String> response, String requestInfo) {
		String color = getColorForStatus(response.statusCode());
		String reset = "\u001B[0m";

		System.out.println("╔══════════════════════════════════════════════════════════╗");
		System.out.printf("║ %-60s ║\n", "Request: " + requestInfo);
		System.out.printf("║ Status: %s%-3d%s%-51s ║\n", color, response.statusCode(), reset, "");
		System.out.println("╚══════════════════════════════════════════════════════════╝");

		// Тело ответа
		if (!response.body().isEmpty()) {
			System.out.println("Response body:");
			try {
				// Пытаемся красиво отформатировать JSON
				String formatted = response.body()
						.replace("{", "{\n  ")
						.replace("}", "\n}")
						.replace(",", ",\n  ");
				System.out.println(formatted);
			} catch (Exception e) {
				System.out.println(response.body());
			}
		} else {
			System.out.println("(empty response body)");
		}
		System.out.println("\n" + "─".repeat(80) + "\n");
	}

	private static String getColorForStatus(int statusCode) {
		if (statusCode >= 200 && statusCode < 300)
			return "\u001B[32m"; // зеленый
		if (statusCode >= 300 && statusCode < 400)
			return "\u001B[33m"; // желтый
		if (statusCode >= 400 && statusCode < 500)
			return "\u001B[31m"; // красный
		if (statusCode >= 500)
			return "\u001B[35m"; // фиолетовый
		return "\u001B[0m"; // сброс
	}
}
