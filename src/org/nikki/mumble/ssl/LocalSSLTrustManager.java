package org.nikki.mumble.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class LocalSSLTrustManager implements X509TrustManager {
	@Override
	public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
	}

	@Override
	public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
	}

	@Override
	public final X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}