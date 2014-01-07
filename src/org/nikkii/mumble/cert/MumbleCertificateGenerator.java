/*
 * Copyright 2008-2010 Xebia and the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nikkii.mumble.cert;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;

/**
 * Demo of a generation of a X509 Self Signed Certificate using <a
 * href="http://www.bouncycastle.org/">Bouncy Castle</a> library.
 * 
 * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
 */
@SuppressWarnings("deprecation")
public class MumbleCertificateGenerator {

	static {
		// adds the Bouncy castle provider to java security
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * <p>
	 * Generate a self signed X509 certificate .
	 * </p>
	 * <p>
	 * TODO : do the same with
	 * {@link org.bouncycastle.cert.X509v1CertificateBuilder} instead of the
	 * deprecated {@link org.bouncycastle.x509.X509V1CertificateGenerator}.
	 * </p>
	 * @throws KeyStoreException 
	 */
	public static void generate(OutputStream out) throws NoSuchAlgorithmException, NoSuchProviderException, CertificateEncodingException, SignatureException, InvalidKeyException, IOException, KeyStoreException {

		// yesterday
		Date validityBeginDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
		// in 2 years
		Date validityEndDate = new Date(System.currentTimeMillis() + 2 * 365 * 24 * 60 * 60 * 1000);

		// GENERATE THE PUBLIC/PRIVATE RSA KEY PAIR
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
		keyPairGenerator.initialize(1024, new SecureRandom());

		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		// GENERATE THE X509 CERTIFICATE
		X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
		X500Principal dnName = new X500Principal("CN=Mumble-Java User");

		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setSubjectDN(dnName);
		certGen.setIssuerDN(dnName); // use the same
		certGen.setNotBefore(validityBeginDate);
		certGen.setNotAfter(validityEndDate);
		certGen.setPublicKey(keyPair.getPublic());
		certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

		X509Certificate cert = certGen.generate(keyPair.getPrivate(), "BC");
		
		String strPassword = "";
		
		KeyStore keyStore = KeyStore.getInstance("JKS");
		try {
			keyStore.load(null, strPassword.toCharArray());
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		
		Certificate[] certChain = new Certificate[] { cert };
		
		keyStore.setEntry("cert", new KeyStore.PrivateKeyEntry((PrivateKey) keyPair.getPrivate(), certChain),
			       new KeyStore.PasswordProtection(strPassword.toCharArray()));
		
		try {
			keyStore.store(out, strPassword.toCharArray());
		} catch (CertificateException e) {
			e.printStackTrace();
		}
	}
}