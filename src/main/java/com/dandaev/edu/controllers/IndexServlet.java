package com.dandaev.edu.controllers;

import java.io.IOException;
import java.util.Enumeration;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class IndexServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Enumeration<String> parameterNames = req.getParameterNames();

		System.out.println("\n---=== PARAMETERS ===---");
		while (parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement();
			String[] values = req.getParameterValues(name);

			System.out.print(name + " : ");
			if (values != null) {
				for (String value : values) {
					System.out.print(value + " ");
				}
				System.out.println();
			}
		}

		RequestDispatcher requestDispatcher = req.getRequestDispatcher("/index.html");
		requestDispatcher.forward(req, resp);
	}

}
