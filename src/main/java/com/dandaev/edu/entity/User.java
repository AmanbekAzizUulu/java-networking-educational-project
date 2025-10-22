package com.dandaev.edu.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private String username;
	private String email;
	private int age;
	private boolean isActive;
	private LocalDateTime registrationDate;

	// Конструктор
	public User(String username, String email, int age, boolean isActive) {
		this.username = username;
		this.email = email;
		this.age = age;
		this.isActive = isActive;
		this.registrationDate = LocalDateTime.now();
	}

	// Геттеры
	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public int getAge() {
		return age;
	}

	public boolean isActive() {
		return isActive;
	}

	public LocalDateTime getRegistrationDate() {
		return registrationDate;
	}

	// Сеттеры
	public void setUsername(String username) {
		this.username = username;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	@Override
	public String toString() {
		return String.format(
				"User{username='%s', email='%s', age=%d, isActive=%s, registrationDate=%s}",
				username, email, age, isActive, registrationDate);
	}

	// Форматированная строка для записи в файл
	public String toFormattedString() {
		return String.format(
				"%-15s | %-25s | %3d | %-5s | %s",
				username, email, age, isActive, registrationDate);
	}
}
