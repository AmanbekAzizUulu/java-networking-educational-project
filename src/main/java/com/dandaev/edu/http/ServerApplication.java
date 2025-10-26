package com.dandaev.edu.http;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dandaev.edu.http.server.HttpServer;

public class ServerApplication {
	private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);
	private static HttpServer server;

	public static void main(String[] args) {
		// Настройка порта по умолчанию
		int port = 8081;

		// Чтение порта из аргументов командной строки
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				logger.warn("Invalid port number: {}. Using default port: {}", args[0], port);
			}
		}

		// Проверка валидности порта
		if (port < 1 || port > 65535) {
			logger.error("Port must be between 1 and 65535");
			System.exit(1);
		}

		// Создание сервера
		server = new HttpServer(port);

		// Добавляем shutdown hook для graceful shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Received shutdown signal");
			if (server != null) {
				server.stop();
			}
		}));

		// Запуск сервера в отдельном потоке
		Thread serverThread = new Thread(() -> {
			try {
				server.start();
			} catch (Exception e) {
				logger.error("Server stopped with error", e);
				System.exit(1);
			}
		}, "Server-Main-Thread");

		serverThread.start();

		// Ожидание запуска сервера
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Проверяем, запустился ли сервер
		if (!server.isRunning()) {
			logger.error("Server failed to start on port {}", port);
			System.exit(1);
		}

		// Консольный интерфейс для управления
		startConsoleInterface();
	}

	private static void startConsoleInterface() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Server is running. Commands: [stop, quit, exit]");

		while (server.isRunning()) {
			try {
				System.out.print("> ");
				String command = scanner.nextLine().trim().toLowerCase();

				switch (command) {
					case "stop":
					case "quit":
					case "exit":
						logger.info("Received stop command from console");
						server.stop();
						break;
					case "status":
						System.out.println("Server is " + (server.isRunning() ? "running" : "stopped"));
						break;
					case "":
						// Пустая команда - продолжаем
						break;
					default:
						System.out.println("Unknown command: " + command);
						System.out.println("Available commands: stop, quit, exit, status");
				}
			} catch (Exception e) {
				logger.error("Error in console interface", e);
				break;
			}
		}

		scanner.close();
		logger.info("Console interface stopped");
	}
}
