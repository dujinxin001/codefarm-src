package com.sxj.jsonrpc.core.exception;

public class JsonRpcConnectionException extends RuntimeException {

	public JsonRpcConnectionException() {
		super();
	}

	public JsonRpcConnectionException(String message) {
		super(message);
	}

	public JsonRpcConnectionException(String message, Throwable e) {
		super(message, e);
	}
}
