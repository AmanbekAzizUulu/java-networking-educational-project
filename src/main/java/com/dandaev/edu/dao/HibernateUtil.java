package com.dandaev.edu.dao;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {
	private static final SessionFactory sessionFactory = buildSessionFactory();

	private static SessionFactory buildSessionFactory() {
		try {
			// Создаем реестр сервисов из hibernate.cfg.xml
			StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate.cfg.xml")
					.build();

			// Создаем метаданные из реестра сервисов
			Metadata metadata = new MetadataSources(standardRegistry)
					.getMetadataBuilder()
					.build();

			return metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			System.err.println("❌ Initial SessionFactory creation failed: " + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static void shutdown() {
		getSessionFactory().close();
	}

	// Тестовое подключение
	public static void testConnection() {
		try (var session = getSessionFactory().openSession()) {
			var result = session.createNativeQuery("SELECT version()", String.class).getSingleResult();
			System.out.println("✅ Hibernate connected to: " + result);
		} catch (Exception e) {
			System.err.println("❌ Hibernate connection failed: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
