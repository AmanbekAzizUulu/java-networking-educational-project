package com.dandaev.edu.udp.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.UUID;

import com.dandaev.edu.entity.Packet;
import com.dandaev.edu.entity.User;
import com.dandaev.edu.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UdpClient {
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 8888;
	private static final int TIMEOUT_MS = 2000;
	private static final int MAX_RETRIES = 3;

	public static void main(String[] args) {
		System.out.println("UDP Client starting...");

		User[] testUsers = {
				new User("john_doe", "john@example.com", 25, true),
				new User("alice_smith", "alice@company.org", 30, true),
				new User("bob_johnson", "bob@gmail.com", 22, false),
				new User("eva_brown", "eva.brown@test.com", 28, true)
		};

		try (DatagramSocket socket = new DatagramSocket()) {
			socket.setSoTimeout(TIMEOUT_MS);
			InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

			Gson gson = new GsonBuilder()
					.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
					.create();

			for (User user : testUsers) {
				String packetId = UUID.randomUUID().toString(); // уникальный ID
				String json = gson.toJson(new Packet(packetId, user));
				byte[] data = json.getBytes();
				DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, SERVER_PORT);

				boolean acknowledged = false;
				int attempt = 0;

				while (!acknowledged && attempt < MAX_RETRIES) {
					attempt++;
					socket.send(packet);
					System.out.printf("Sent user '%s' (packetId=%s, attempt %d)%n",
							user.getUsername(), packetId, attempt);

					try {
						byte[] ackBuffer = new byte[256];
						DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
						socket.receive(ackPacket);

						String ackMessage = new String(ackPacket.getData(), 0, ackPacket.getLength());
						if (ackMessage.equals("ACK:" + packetId)) {
							System.out.println("Server acknowledged packetId: " + packetId);
							acknowledged = true;
						}
					} catch (SocketTimeoutException e) {
						System.out.println("Timeout waiting for ACK. Retrying...");
					}
				}

				if (!acknowledged) {
					System.out.println(
							"No ACK received for packetId " + packetId + " after " + MAX_RETRIES + " attempts.");
				}

				Thread.sleep(1000);
			}

			System.out.println("\nAll users processed.");

		} catch (Exception e) {
			System.err.println("Client error: " + e.getMessage());
		}
	}
}
