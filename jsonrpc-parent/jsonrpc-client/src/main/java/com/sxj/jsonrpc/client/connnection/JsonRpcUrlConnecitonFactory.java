package com.sxj.jsonrpc.client.connnection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonRpcUrlConnecitonFactory {

	private static int connectTimeout = -1;

	private static int readTimeout = -1;

	public static JsonRpcConnection open(URL url) throws IOException {

		HttpURLConnection conn;
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setChunkedStreamingMode(4 * 1024);
		conn.setDoInput(true);

		if (connectTimeout >= 0)
			conn.setConnectTimeout(connectTimeout);

		if (readTimeout > 0) {
			try {
				conn.setReadTimeout(readTimeout);
			} catch (Throwable e) {
			}
		}

		return new JsonRpcUrlConnection(url, conn);
	}

	public static void setConnectTimeout(int timeout) {
		connectTimeout = timeout;
	}

	public static void setReadTimeout(int timeout) {
		readTimeout = timeout;
	}

}
