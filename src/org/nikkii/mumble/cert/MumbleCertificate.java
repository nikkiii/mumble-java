package org.nikkii.mumble.cert;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;

public class MumbleCertificate {
	
	private KeyStore keyStore;
	private X509KeyManager x509KeyManager;

	public MumbleCertificate(InputStream input) throws MumbleCertificateException {
		initialize(input);
	}
	
	public MumbleCertificate(File file) throws IOException, MumbleCertificateException {
		initialize(new FileInputStream(file));
	}
	
	public MumbleCertificate(byte[] bytes) throws MumbleCertificateException {
		initialize(new ByteArrayInputStream(bytes));
	}
	
	private void initialize(InputStream input) throws MumbleCertificateException {
		try {
			keyStore = KeyStore.getInstance("JKS");
			keyStore.load(input, null);
			
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
			keyManagerFactory.init(keyStore, "".toCharArray());

			for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
				if (keyManager instanceof X509KeyManager) {
					x509KeyManager = (X509KeyManager) keyManager;
					break;
				}
			}

			if (x509KeyManager == null) {
				throw new NullPointerException();
			}
		} catch(Exception e) {
			throw new MumbleCertificateException(e);
		}
	}
	
	public X509KeyManager getKeyManager() {
		return x509KeyManager;
	}
}
