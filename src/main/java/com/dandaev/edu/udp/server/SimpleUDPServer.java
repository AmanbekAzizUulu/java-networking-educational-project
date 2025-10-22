package com.dandaev.edu.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class SimpleUDPServer {
	public static void main(String[] args) {
		try (var datagramServerSocket = new DatagramSocket(9876)) {
			var receivedData = new byte[1024];
			while(true){
				var recievedPacket = new DatagramPacket(receivedData, receivedData.length);

				datagramServerSocket.receive(recievedPacket);

				var messageFromClient = new String(recievedPacket.getData(), 0, recievedPacket.getLength());
				var clientInetAddress = recievedPacket.getAddress();
				var clientPort = recievedPacket.getPort();

				var infoToPrint =
				"""
				Received from:
						address:      %s
						client_port:  %d
						message:      %s
				""".formatted(clientInetAddress, clientPort, messageFromClient);

				System.out.println(infoToPrint);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}



	}
}
