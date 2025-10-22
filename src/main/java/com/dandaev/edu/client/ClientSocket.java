package com.dandaev.edu.client;

import java.io.*;
import java.net.*;

public class ClientSocket {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8081)) {
            var os = new DataOutputStream(socket.getOutputStream());
            var is = new DataInputStream(socket.getInputStream());

            // Send request
            String request = "request from client";
            byte[] requestBytes = request.getBytes();

            os.writeInt(requestBytes.length); // First the length
            os.write(requestBytes); 			// Then the data 
            os.flush();

            System.out.println("Request sent to server");

            // Read response
            int responseLength = is.readInt();
            byte[] responseBuffer = new byte[responseLength];
            is.readFully(responseBuffer);
            String serverResponse = new String(responseBuffer);

            System.out.println("Received from server: " + serverResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
