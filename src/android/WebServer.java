package com.rjfun.cordova.httpd;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer extends NanoHTTPD
{
	
	public WebServer(InetSocketAddress localAddr, AndroidFile wwwroot, CorHttpd s) throws IOException {
		super(localAddr, wwwroot, s);
	}

	public WebServer(int port, AndroidFile wwwroot, CorHttpd s ) throws IOException {
		super(port, wwwroot, s);
	}
}
