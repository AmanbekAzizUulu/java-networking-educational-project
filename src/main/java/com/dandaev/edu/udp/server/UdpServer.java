package com.dandaev.edu.udp.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.dandaev.edu.entity.User;

public class UdpServer {
	private static final int PORT = 8888;
	private static final int BUFFER_SIZE = 4096;
	private static final String LOG_FILE = "/src/main/resources/users.log";

	public static void main(String[] args) {
		System.out.println("UDP Server starting on port " + PORT);

		try (DatagramSocket socket = new DatagramSocket(PORT)) {
			// Создаем файл для логирования, если не существует
			initializeLogFile();

			while (true) {
				// Подготовка буфера для приема данных
				byte[] buffer = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

				System.out.println("\nWaiting for incoming data...");

				// Ожидание пакета (блокирующий вызов)
				socket.receive(packet);

				// Обработка полученных данных в отдельном потоке
				processReceivedData(packet);
			}

		} catch (SocketException e) {
			System.err.println("Socket error: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("IO error: " + e.getMessage());
		}
	}

	private static void initializeLogFile() throws IOException {
		File logFile = new File(LOG_FILE);
		if (!logFile.exists()) {
			try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
				writer.println("=== User Registration Log ===");
				writer.println("Time | Username       | Email                    | Age | Active | Registration Date");
				writer.println(
						"----------------------------------------------------------------------------------------");
			}
			System.out.println("Created new log file: " + LOG_FILE);
		}
	}

	private static void processReceivedData(DatagramPacket packet) {
		try {
			// Получаем информацию об отправителе
			String clientAddress = packet.getAddress().getHostAddress();
			int clientPort = packet.getPort();

			System.out.println("Received data from " + clientAddress + ":" + clientPort);

			// Десериализация объекта из байтов
			ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
			ObjectInputStream ois = new ObjectInputStream(bais);

			User receivedUser = (User) ois.readObject();

			// Вывод в терминал
			displayUserInfo(receivedUser, clientAddress, clientPort);

			// Запись в файл
			logUserToFile(receivedUser, clientAddress);

			ois.close();

		} catch (IOException e) {
			System.err.println("Error processing data: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Error: Received unknown object type");
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
		}
	}

	private static void displayUserInfo(User user, String clientAddress, int clientPort) {
		System.out.println("\n ===== RECEIVED USER DATA =====");
		System.out.println("From: " + clientAddress + ":" + clientPort);
		System.out.println("Username: " + user.getUsername());
		System.out.println("Email: " + user.getEmail());
		System.out.println("Age: " + user.getAge());
		System.out.println("Active: " + user.isActive());
		System.out.println("Registered: " + user.getRegistrationDate());
		System.out.println("Full object: " + user);
		System.out.println("====================================\n");
	}

	private static void logUserToFile(User user, String clientAddress) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			String logEntry = String.format("%s | %-15s | %-25s | %3d | %-5s | %s",
					timestamp, user.getUsername(), user.getEmail(),
					user.getAge(), user.isActive(), user.getRegistrationDate());

			writer.println(logEntry);
			writer.flush();

			System.out.println("User data saved to: " + LOG_FILE);

		} catch (IOException e) {
			System.err.println("Error writing to log file: " + e.getMessage());
		}
	}
}
