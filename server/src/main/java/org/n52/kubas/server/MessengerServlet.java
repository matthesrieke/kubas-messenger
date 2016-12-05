package org.n52.kubas.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessengerServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -901653202063235206L;

	private static Logger log = LoggerFactory.getLogger(MessengerServlet.class);
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		String secOptsPath = config.getInitParameter("secOptspath");
		
//		new Thread(){
//			public void run() {
//				try {
//					new SMS2GEE(secOptsPath);
//				} catch (Exception e) {
//					log.error("Could not start SMS2GEE.", e);
//				}
//			};
//		}.start();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		File tmpfile = File.createTempFile("smsgatewaymockup", ".txt");
		
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tmpfile));
		
		BufferedReader bufferedReader = new BufferedReader(req.getReader());
		
		String content = "";
		
		while((content = bufferedReader.readLine()) != null){
			bufferedWriter.write(content + "\n");
		}
		
		bufferedWriter.close();
	}

}
