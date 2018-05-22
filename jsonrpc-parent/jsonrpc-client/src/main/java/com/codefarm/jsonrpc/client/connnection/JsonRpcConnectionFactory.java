package com.codefarm.jsonrpc.client.connnection;

import java.io.IOException;
import java.net.URL;

public interface JsonRpcConnectionFactory {

	public void setConnectTimeout(int timeout);

	public void setReadTimeout(int timeout);

	public JsonRpcConnection open(URL url) throws IOException;
}
