package com.dandaev.edu.udp.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.dandaev.edu.entity.User;
import com.dandaev.edu.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UdpServer {
	private static final int PORT = 8888;
	private static final int BUFFER_SIZE = 4096;
	private static final String LOG_FILE = "users.log";

	public static void main(String[] args) {
		System.out.println("UDP Server starting on port " + PORT);

		try (DatagramSocket socket = new DatagramSocket(PORT)) {
			Gson gson = new GsonBuilder()
					.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
					.create();

			System.out.println("Server is ready...");

			while (true) {
				byte[] buffer = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

				socket.receive(packet);

				String clientAddress = packet.getAddress().getHostAddress();
				int clientPort = packet.getPort();

				String json = new String(packet.getData(), 0, packet.getLength());
				User receivedUser = gson.fromJson(json, User.class);

				displayUserInfo(receivedUser, clientAddress, clientPort);
				logUserToFile(receivedUser);
			}

		} catch (IOException e) {
			System.err.println("Server error: " + e.getMessage());
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
		System.out.println("====================================\n");
	}

	private static void logUserToFile(User user) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			writer.printf("%s | %s | %s | %d | %s | %s%n", timestamp, user.getUsername(), user.getEmail(), user.getAge(), user.isActive(), user.getRegistrationDate());
			writer.flush();
		} catch (IOException e) {
			System.err.println("Error writing to log file: " + e.getMessage());
		}
	}
}
