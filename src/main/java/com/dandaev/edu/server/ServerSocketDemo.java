package com.dandaev.edu.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketDemo {
    public static void main(String[] args) {
        try (var serverSocket = new ServerSocket(8081)) {
            System.out.println("Server started on port 8081...");

            while (true) {
                try (Socket acceptedClientSocket = serverSocket.accept()) {
                    System.out.println("Client connected: " + acceptedClientSocket.getInetAddress());

                    var responseToClient = new DataOutputStream(acceptedClientSocket.getOutputStream());
                    var requestFromClient = new DataInputStream(acceptedClientSocket.getInputStream());

                    // Read message length
                    int messageLength = requestFromClient.readInt();

					// Read the message itself
					byte[] buffer = new byte[messageLength];
                    requestFromClient.readFully(buffer);
                    String clientMessage = new String(buffer);

                    System.out.println("Received from client: " + clientMessage);

                    // Send response
                    String response = "hello from server!";
                    byte[] responseBytes = response.getBytes();

                    responseToClient.writeInt(responseBytes.length); 	// First the length
                    responseToClient.write(responseBytes); 			// Then the data
                    responseToClient.flush();

                    System.out.println("Response sent to client");

                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
