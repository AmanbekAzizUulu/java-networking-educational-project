package com.dandaev.edu.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class SimpleUDPClient {
	public static void main(String[] args) {
		try (var datagramClientSocket = new DatagramSocket(8081)) {

			var messageToServerString = "hello from client!";
			var messageToServerByteArray = messageToServerString.getBytes();
			var lengthOfMessageToServerByteArray = messageToServerByteArray.length;
			var clientInetAddress = InetAddress.getByName("localhost");
			var serverPort = 9876;

			var datagramPacketToServer = new DatagramPacket(messageToServerByteArray, lengthOfMessageToServerByteArray, clientInetAddress, serverPort);

			datagramClientSocket.send(datagramPacketToServer);

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
