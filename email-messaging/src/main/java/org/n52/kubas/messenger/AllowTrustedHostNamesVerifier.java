
package org.n52.kubas.messenger;

import java.io.IOException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllowTrustedHostNamesVerifier implements X509HostnameVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(AllowTrustedHostNamesVerifier.class);

    private StrictHostnameVerifier delegate;

    public AllowTrustedHostNamesVerifier() {
        this.delegate = new StrictHostnameVerifier();
    }

    @Override
    public boolean verify(String hostname, SSLSession session) {
        boolean result = this.delegate.verify(hostname, session);
        if (!result) {
            return true;
        }

        return result;
    }

    @Override
    public void verify(String host, SSLSocket ssl) throws IOException {
        try {
            this.delegate.verify(host, ssl);
        } catch (IOException e) {
            LOG.info(e.getMessage());
        }
    }

    @Override
    public void verify(String host, X509Certificate cert)
            throws SSLException {
        try {
            this.delegate.verify(host, cert);
        } catch (SSLException e) {
            LOG.info(e.getMessage());
        }
    }

    @Override
    public void verify(String host, String[] cns, String[] subjectAlts)
            throws SSLException {
        try {
            this.delegate.verify(host, cns, subjectAlts);
        } catch (SSLException e) {
            LOG.info(e.getMessage());
        }
    }
}
