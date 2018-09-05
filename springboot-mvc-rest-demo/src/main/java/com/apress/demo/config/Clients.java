package com.apress.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
 * Created by cheng wang on 01/12/2017
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("app.clients")
public class Clients {
    private String hostname;
    private String scheme;
    private int httpPort;
    private int containerPort;
    private int transportPort;
    private String environment;
    private String environment2;
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getScheme() {
		return scheme;
	}
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	public int getHttpPort() {
		return httpPort;
	}
	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}
	public int getContainerPort() {
		return containerPort;
	}
	public void setContainerPort(int containerPort) {
		this.containerPort = containerPort;
	}
	public int getTransportPort() {
		return transportPort;
	}
	public void setTransportPort(int transportPort) {
		this.transportPort = transportPort;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getEnvironment2() {
		return environment2;
	}
	public void setEnvironment2(String environment2) {
		this.environment2 = environment2;
	}

    
    
    
    
}
