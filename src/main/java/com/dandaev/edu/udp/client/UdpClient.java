package com.dandaev.edu.udp.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.dandaev.edu.entity.User;

public class UdpClient {
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 8888;

	public static void main(String[] args) {
		System.out.println("UDP Client starting...");

		// Создаем тестовых пользователей
		User[] testUsers = {
				new User("john_doe", "john@example.com", 25, true),
				new User("alice_smith", "alice@company.org", 30, true),
				new User("bob_johnson", "bob@gmail.com", 22, false),
				new User("eva_brown", "eva.brown@test.com", 28, true)
		};

		try (DatagramSocket socket = new DatagramSocket()) {
			InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

			for (User user : testUsers) {
				sendUserObject(socket, serverAddress, user);

				// Задержка между отправками
				Thread.sleep(2000);
			}

			System.out.println("\nAll users sent successfully!");

		} catch (Exception e) {
			System.err.println("Client error: " + e.getMessage());
		}
	}

	private static void sendUserObject(DatagramSocket socket, InetAddress address, User user)
			throws IOException {

		// Сериализация объекта в байты
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(user);
		oos.flush();

		byte[] objectData = baos.toByteArray();

		// Создание и отправка пакета
		DatagramPacket packet = new DatagramPacket(objectData, objectData.length, address, SERVER_PORT);

		socket.send(packet);

		System.out.println("Sent user: " + user.getUsername() + " (" + objectData.length + " bytes)");

		oos.close();
	}
}
