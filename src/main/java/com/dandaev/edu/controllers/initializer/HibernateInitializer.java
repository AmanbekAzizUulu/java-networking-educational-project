package com.dandaev.edu.controllers.initializer;

import com.dandaev.edu.dao.HibernateUtil;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class HibernateInitializer implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("🚀 Starting Hibernate initialization...");

		try {
			// Просто получаем SessionFactory - это запустит создание таблиц
			var sessionFactory = HibernateUtil.getSessionFactory();
			System.out.println("✅ Hibernate SessionFactory created successfully");

			// Тестируем подключение и создание таблиц
			HibernateUtil.testConnection();

		} catch (Exception e) {
			System.err.println("❌ Hibernate initialization failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		HibernateUtil.shutdown();
		System.out.println("✅ Hibernate SessionFactory closed");
	}
}
