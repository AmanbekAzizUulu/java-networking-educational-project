package com.dandaev.edu.udp.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;

import com.dandaev.edu.entity.User;
import com.dandaev.edu.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UdpClient {
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 8888;

	public static void main(String[] args) {
		System.out.println("UDP Client starting...");

		User[] testUsers = {
				new User("john_doe", "john@example.com", 25, true),
				new User("alice_smith", "alice@company.org", 30, true),
				new User("bob_johnson", "bob@gmail.com", 22, false),
				new User("eva_brown", "eva.brown@test.com", 28, true)
		};

		try (DatagramSocket socket = new DatagramSocket()) {
			InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);
			Gson gson = new GsonBuilder()
					.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
					.create();

			for (User user : testUsers) {
				String json = gson.toJson(user);
				byte[] data = json.getBytes();

				DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, SERVER_PORT);
				socket.send(packet);

				System.out.println("Sent JSON: " + json);
				Thread.sleep(2000);
			}

			System.out.println("\nAll users sent successfully!");

		} catch (Exception e) {
			System.err.println("Client error: " + e.getMessage());
		}
	}
}
