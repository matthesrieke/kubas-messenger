package org.n52.kubas.messenger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.commons.httpclient.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sun.mail.imap.IMAPFolder;

public class Email2GEE {

	private static Logger log = LoggerFactory.getLogger(Email2GEE.class);

	private String usernameKey = "username";
	private String passwordKey = "password";
	private String hostKey = "host";
	private String inboxKey = "inbox";
	private String geeEndpointURLKey = "geeendpointurl";

	private final String email = "email";
	private final String phone = "phone";
	private final String userId = "userId";
	private final String details = "details";

	private String username;
	private String password;
	private String host;
	private String inbox;
	private String geeEndpointURL;

	private HttpClient client;

	public Email2GEE(String secOptsPath) throws Exception {

		InputStream propertiesInputstream = new FileInputStream(new File(secOptsPath));

		Properties mailAccountProperties = new Properties();

		mailAccountProperties.load(propertiesInputstream);

		if(!mailAccountProperties.containsKey(usernameKey) || !mailAccountProperties.containsKey(passwordKey)
				|| !mailAccountProperties.containsKey(hostKey) || !mailAccountProperties.containsKey(inboxKey)
				|| !mailAccountProperties.containsKey(geeEndpointURLKey)){
			log.error("Properties not present.");
			throw new RuntimeException("Properties not present.");
		}

		setUsername(mailAccountProperties.getProperty(usernameKey));
		setPassword(mailAccountProperties.getProperty(passwordKey));
		setHost(mailAccountProperties.getProperty(hostKey));
		setInbox(mailAccountProperties.getProperty(inboxKey));
		setGeeEndpointURL(mailAccountProperties.getProperty(geeEndpointURLKey));

		client = createClient();

		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		Session session = Session.getDefaultInstance(props, null);

		Store store = session.getStore("imaps");
		store.connect(this.getHost(), this.getUsername(), this.getPassword());
		IMAPFolder folder = (IMAPFolder) store.getFolder(this.getInbox());

		if (!folder.isOpen()) {
			folder.open(Folder.READ_WRITE);
		}

		folder.addMessageCountListener(new MessageCountListener() {

			@Override
			public void messagesRemoved(MessageCountEvent e) {}

			@Override
			public void messagesAdded(MessageCountEvent e) {
				Message[] messages = e.getMessages();

				for (Message message : messages) {
					org.n52.kubas.messenger.Message message2 = new org.n52.kubas.messenger.Message();
					try {
						String subject = message.getSubject();
						String fromEmailAddress = extractSenderEmailAdress(message.getFrom());
						message2.setEmail(fromEmailAddress);
						message2.setDetails(subject);
						log.info("Got new message with subject: " + subject);
					} catch (MessagingException e1) {
						log.error("Could not get message subject.", e1);
					}
					//forward to GEE
					try {
						forwardToGEE(message2);
					} catch (Exception e1) {
						log.error("Could not forward message to GEE.", e1);
					}

					try {
						message.setFlag(Flag.SEEN, true);
					} catch (MessagingException e1) {
						log.error("Could not set message to seen.", e1);
					}
				}

			}
		});

		while (true) {
			try {
				// folder.idle();
				int newMessageCount = folder.getNewMessageCount();
				log.info("New messages: " + newMessageCount);
				Thread.sleep(30000);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	private String extractSenderEmailAdress(Address[] addressArray){

		String result = "";

		if(addressArray == null){
			return result;
		}

		for (Address address : addressArray) {
			if(address instanceof InternetAddress){
				result = ((InternetAddress)address).getAddress();
				break;
			}
		}

		return result;
	}

	protected HttpClient createClient() throws Exception {
		DefaultHttpClient result = new DefaultHttpClient();
		SchemeRegistry sr = result.getConnectionManager().getSchemeRegistry();

		SSLSocketFactory sslsf = new SSLSocketFactory(new TrustStrategy() {

			@Override
			public boolean isTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				return true;
			}
		}, new AllowTrustedHostNamesVerifier());

		Scheme httpsScheme2 = new Scheme("https", 443, sslsf);
		sr.register(httpsScheme2);

		return result;
    }

	public class AllowTrustedHostNamesVerifier implements X509HostnameVerifier {
		private StrictHostnameVerifier delegate;

		public AllowTrustedHostNamesVerifier() {
			this.delegate = new StrictHostnameVerifier();
		}

		public boolean verify(String hostname, SSLSession session) {
			boolean result = this.delegate.verify(hostname, session);
			if (!result) {
				return true;
			}

			return result;
		}

		public void verify(String host, SSLSocket ssl) throws IOException {
			try {
				this.delegate.verify(host, ssl);
			} catch (IOException e) {
				log.info(e.getMessage());
			}
		}

		public void verify(String host, X509Certificate cert)
				throws SSLException {
			try {
				this.delegate.verify(host, cert);
			} catch (SSLException e) {
				log.info(e.getMessage());
			}
		}

		public void verify(String host, String[] cns, String[] subjectAlts)
				throws SSLException {
			try {
				this.delegate.verify(host, cns, subjectAlts);
			} catch (SSLException e) {
				log.info(e.getMessage());
			}
		}
}

	private void forwardToGEE(org.n52.kubas.messenger.Message message2) throws HttpException, IOException {

		String requestJSON = createJSONFromMessage(message2);

		StringEntity requestEntity = new StringEntity(requestJSON, "UTF-8");

		requestEntity.setContentType("application/json");

		HttpPost uriRequest = new HttpPost(getGeeEndpointURL());

		uriRequest.setEntity(requestEntity);

		HttpResponse response = client.execute(uriRequest);

		EntityUtils.consume(response.getEntity());

	}

	private String createJSONFromMessage(org.n52.kubas.messenger.Message message) throws IOException{

        StringWriter stringWriter = new StringWriter();

        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createGenerator(stringWriter);

        g.writeStartObject();

        String email = message.getEmail();

        if(email != null && !email.isEmpty()){
            g.writeStringField(this.email, message.getEmail());
        }
        String phone = message.getPhone();

        if(phone != null && !phone.isEmpty()){
            g.writeStringField(this.phone, message.getPhone());
        }

//        String userId = message.getUserId();
//
//        if(userId != null && !userId.isEmpty()){
//            g.writeStringField(this.userId, message.getUserId());
//        }

        g.writeStringField(details, message.getDetails());

        g.writeEndObject();
        g.close();

        return stringWriter.toString();
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setInbox(String inbox) {
		this.inbox = inbox;
	}

	private String getInbox() {
		return inbox;
	}

	private String getPassword() {
		return password;
	}

	private String getUsername() {
		return username;
	}

	private String getHost() {
		return host;
	}

	public String getGeeEndpointURL() {
		return geeEndpointURL;
	}

	public void setGeeEndpointURL(String geeEndpointURL) {
		this.geeEndpointURL = geeEndpointURL;
	}
}
