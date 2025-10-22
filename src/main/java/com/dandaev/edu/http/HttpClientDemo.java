package com.dandaev.edu.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpClientDemo {
	public static void main(String[] args) {
		try {
			// 1. Простой клиент c настройками по умолчанию
			HttpClient simpleClient = HttpClient.newHttpClient();
			HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("https://google.com")).GET().build();

			HttpResponse<String> httpResponse = simpleClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

			System.out.println(httpResponse.version() + "/" + httpResponse.statusCode());
			System.out.println();
			System.out.println(httpResponse.headers());
			System.out.println();
			System.out.println(httpResponse.body());


			// 2. Клиент c кастомной конфигурацией через Builder
			HttpClient customClient = HttpClient.newBuilder()
					.version(HttpClient.Version.HTTP_2) // Версия HTTP
					.connectTimeout(Duration.ofSeconds(10)) // Таймаут подключения
					.followRedirects(HttpClient.Redirect.NORMAL) // Следование перенаправлениям
					.priority(1) // Приоритет HTTP/2
					.build();

			// 3. Клиент c аутентификацией
			HttpClient authenticatedClient = HttpClient.newBuilder()
					.authenticator(new java.net.Authenticator() {
						@Override
						protected java.net.PasswordAuthentication getPasswordAuthentication() {
							return new java.net.PasswordAuthentication(
									"username", "password".toCharArray());
						}
					})
					.build();
					
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
