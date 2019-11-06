package com.chemyoo.spider.core;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/** 
 * @author Author : jianqing.liu
 * @description class description
 */
public class IgnoreX509TrustManager implements X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		// ignore
	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		// ignore
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		// ignore
		return new X509Certificate[]{};
	}

}
