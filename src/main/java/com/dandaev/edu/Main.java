package com.dandaev.edu;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try {
            // 1. Первый запрос (редирект)
            String redirectHtml = fetchUrl("google.com", "/");
            saveToFile(redirectHtml, "redirect_response.html");

            // 2. Второй запрос (финальный)
            String finalHtml = fetchUrl("www.google.com", "/");
            saveToFile(finalHtml, "final_response.html");

            System.out.println("✓ Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String fetchUrl(String host, String path) throws IOException {
        System.out.println("\n=== Fetching " + host + path + " ===");

        try (Socket socket = new Socket(host, 80)) {
            socket.setSoTimeout(5000); // на случай зависания
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Отправляем HTTP-запрос
            String request = "GET " + path + " HTTP/1.1\r\n" +
                    "Host: " + host + "\r\n" +
                    "User-Agent: JavaRawSocket\r\n" +
                    "Connection: close\r\n\r\n";
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();

            // Читаем ответ
            return readHttpResponse(in);
        }
    }

    private static String readHttpResponse(InputStream in) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        ByteArrayOutputStream headersBuf = new ByteArrayOutputStream();

        // 1️⃣ Читаем заголовки до \r\n\r\n
        int prev = -1, cur;
        boolean headerEnd = false;
        while (!headerEnd && (cur = bin.read()) != -1) {
            headersBuf.write(cur);
            if (prev == '\r' && cur == '\n') {
                byte[] bytes = headersBuf.toByteArray();
                int len = bytes.length;
                if (len >= 4 && bytes[len - 4] == '\r' && bytes[len - 3] == '\n'
                        && bytes[len - 2] == '\r' && bytes[len - 1] == '\n') {
                    headerEnd = true;
                }
            }
            prev = cur;
        }

        String headersStr = headersBuf.toString(StandardCharsets.UTF_8);
        System.out.println("--- HEADERS ---");
        System.out.println(headersStr);

        // 2️⃣ Определяем тип тела (chunked или content-length)
        Map<String, String> headers = parseHeaders(headersStr);
        ByteArrayOutputStream body = new ByteArrayOutputStream();

        if (headers.containsKey("transfer-encoding") &&
            headers.get("transfer-encoding").equalsIgnoreCase("chunked")) {
            System.out.println("Reading body: chunked");
            readChunkedBody(bin, body);
        } else if (headers.containsKey("content-length")) {
            int length = Integer.parseInt(headers.get("content-length"));
            System.out.println("Reading body: fixed length = " + length);
            readFixedBody(bin, body, length);
        } else {
            System.out.println("Reading body until EOF (no length info)");
            bin.transferTo(body);
        }

        return body.toString(StandardCharsets.UTF_8);
    }

    private static void readFixedBody(InputStream in, OutputStream out, int length) throws IOException {
        byte[] buffer = new byte[4096];
        int total = 0, read;
        while (total < length && (read = in.read(buffer, 0, Math.min(buffer.length, length - total))) != -1) {
            out.write(buffer, 0, read);
            total += read;
        }
    }

    private static void readChunkedBody(InputStream in, OutputStream out) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        while (true) {
            String sizeLine = reader.readLine();
            if (sizeLine == null) break;

            sizeLine = sizeLine.trim();
            if (sizeLine.isEmpty()) continue;

            int chunkSize = Integer.parseInt(sizeLine, 16);
            if (chunkSize == 0) {
                // Конец чанков
                reader.readLine(); // читаем пустую строку после "0"
                break;
            }

            char[] chunk = new char[chunkSize];
            int read = 0;
            while (read < chunkSize) {
                int n = reader.read(chunk, read, chunkSize - read);
                if (n == -1) break;
                read += n;
            }
            out.write(new String(chunk).getBytes(StandardCharsets.UTF_8));
            reader.readLine(); // Пропускаем \r\n после чанка
        }
    }

    private static Map<String, String> parseHeaders(String headersStr) {
        Map<String, String> map = new HashMap<>();
        String[] lines = headersStr.split("\r\n");
        for (String line : lines) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                map.put(line.substring(0, idx).trim().toLowerCase(),
                        line.substring(idx + 1).trim());
            }
        }
        return map;
    }

    private static void saveToFile(String content, String filename) {
        try {
            Path dir = Paths.get("src", "main", "resources");
            Files.createDirectories(dir);
            Path path = dir.resolve(filename);
            Files.writeString(path, content, StandardCharsets.UTF_8);
            System.out.println("✓ Saved: " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("✗ Error saving file: " + e.getMessage());
        }
    }
}
