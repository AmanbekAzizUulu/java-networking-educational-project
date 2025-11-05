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
		Enumeration<String> headerNames = req.getHeaderNames();

		System.out.println("----------======== \nAll headers from request\n ========----------");
		while (headerNames.hasMoreElements()) {
			var headerName = headerNames.nextElement();
			var headerValue = req.getHeader(headerName);

			System.out.println(headerName + " : " + headerValue);
		}

		RequestDispatcher requestDispatcher = req.getRequestDispatcher("/index.html");
		requestDispatcher.forward(req, resp);
	}

}
