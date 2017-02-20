package org.n52.kubas.messenger;

import com.sun.mail.smtp.SMTPTransport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GEE2Email {

    private static final Logger LOG = LoggerFactory.getLogger(GEE2Email.class);

    private String username;
    private String password;
    private String host;

    public GEE2Email(String secOptsPath) throws FileNotFoundException, IOException {
        InputStream propertiesInputstream = new FileInputStream(new File(secOptsPath));

        Properties mailAccountProperties = new Properties();

        mailAccountProperties.load(propertiesInputstream);

        if (!mailAccountProperties.containsKey(Email2GEE.usernameKey) || !mailAccountProperties.containsKey(Email2GEE.passwordKey)
                || !mailAccountProperties.containsKey(Email2GEE.smtpHostKey)) {
            LOG.error("Properties not present.");
            throw new RuntimeException("Properties not present.");
        }

        this.username = mailAccountProperties.getProperty(Email2GEE.usernameKey);
        this.password = mailAccountProperties.getProperty(Email2GEE.passwordKey);
        this.host = mailAccountProperties.getProperty(Email2GEE.smtpHostKey);
    }

    private GEE2Email(String user, String pw, String host) {
        this.username = user;
        this.password = pw;
        this.host = host;
    }

    public void send(String emailTo, String subject, String message) throws AddressException, MessagingException {
        send(username, password, host, emailTo, null, subject, message);
    }

    public void send(final String username, final String password, String host, String emailTo, String emailCC, String title, String message) throws AddressException, MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.ssl.enable", "false");
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", "25");
        props.setProperty("mail.smtp.socketFactory.port", "25");
        props.setProperty("mail.smtp.ssl.trust", "*");

        Session session = Session.getInstance(props, null);

        final MimeMessage msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress("kubas-dev@52north.org"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo, false));

        if (emailCC != null) {
            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(emailCC, false));
        }

        msg.setSubject(title);
        msg.setText(message, "utf-8");
        msg.setSentDate(new Date());

        SMTPTransport t = (SMTPTransport) session.getTransport("smtp");

        t.connect(host, 25, username, password);
        t.sendMessage(msg, msg.getAllRecipients());
        t.close();
    }

    public static void main(String[] args) throws IOException, MessagingException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("User?");
        String user = br.readLine();
        System.out.println("Password?");
        String pw = br.readLine();

        new GEE2Email(user, pw, "smtp.52north.org").send("leland@bobmail.info", "Hi hallo Java", "wundervoll! wie das klappt!");
    }


}
