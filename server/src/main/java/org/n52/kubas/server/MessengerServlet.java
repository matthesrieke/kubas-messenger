package org.n52.kubas.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.kubas.messenger.Email2GEE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;

@RequestMapping("/smsgateway")
public class MessengerServlet implements ServletContextAware, ServletConfigAware{

	/**
	 *
	 */
	private static final long serialVersionUID = -901653202063235206L;

	private static Logger log = LoggerFactory.getLogger(MessengerServlet.class);

	private ServletConfig servletConfig;

	private ServletContext setServletContext;

	public void init() {

		String secOptsPath = servletConfig.getInitParameter("secOptspath");

		new Thread(){
			public void run() {
				try {
					new Email2GEE(secOptsPath);
				} catch (Exception e) {
					log.error("Could not start Email2GEE.", e);
				}
			};
		}.start();
	}

	@RequestMapping(method = RequestMethod.GET)
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	}

	@RequestMapping(method = RequestMethod.POST)
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

	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;

	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.setServletContext = servletContext;
	}

}
