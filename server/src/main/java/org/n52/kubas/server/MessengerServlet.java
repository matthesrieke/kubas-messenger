package org.n52.kubas.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.kubas.messenger.Email2GEE;
import org.n52.kubas.messenger.GEE2Email;
import org.n52.kubas.messenger.MessageToEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ServletConfigAware;

@RestController
@RequestMapping(value = "/smsgateway", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
public class MessengerServlet implements ServletConfigAware, InitializingBean, DisposableBean {

    private static Logger log = LoggerFactory.getLogger(MessengerServlet.class);

    private ServletConfig servletConfig;

    private String secOptsPath;
    private GEE2Email mailSender;
    private ExecutorService executor;

    public void init() {

        this.secOptsPath = servletConfig.getInitParameter("secOptspath");

        new Thread(){
            @Override
            public void run() {
                try {
                    mailSender = new GEE2Email(secOptsPath);
                    new Email2GEE(secOptsPath).startListening();
                } catch (Exception e) {
                    log.error("Could not start Email2GEE.", e);
                }
            };
        }.start();
    }

    @RequestMapping(method = RequestMethod.GET)
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void doPost(@RequestBody MessageToEmail mail) throws ServletException, IOException {
        this.executor.submit(() -> {
            try {
                log.debug("Sending mail to: "+mail.getEmailTo());
                mailSender.send(mail.getEmailTo(), mail.getSubject(), mail.getText());
                log.debug("mail sent to: "+mail.getEmailTo());
            } catch (Exception ex) {
                log.warn("Could not send email: "+ ex.getMessage());
                log.debug(ex.getMessage(), ex);
            }
        });
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void destroy() throws Exception {
        this.executor.shutdown();
    }

}
