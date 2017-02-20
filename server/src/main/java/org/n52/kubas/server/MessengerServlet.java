package org.n52.kubas.server;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.kubas.messenger.Email2GEE;
import org.n52.kubas.messenger.GEE2Email;
import org.n52.kubas.messenger.MessageToEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    private String secOptsPath;
    private GEE2Email mailSender;

    public void init() {

        this.secOptsPath = servletConfig.getInitParameter("secOptspath");

        new Thread(){
            public void run() {
                try {
                    mailSender = new GEE2Email(secOptsPath);
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
    @ResponseStatus(HttpStatus.OK)
    protected void doPost(@RequestBody MessageToEmail mail) throws ServletException, IOException {
        try {
            mailSender.send(mail.getEmailTo(), mail.getSubject(), mail.getText());
        } catch (Exception ex) {
            log.warn("Could not send email: "+ ex.getMessage());
            log.debug(ex.getMessage(), ex);
        }
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
