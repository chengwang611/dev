package com.apress.demo.config;

/*
 * Created by hakdogan on 21.05.2018
 */

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("app.elastic")

public class ElasticContainer {
    private String imageUrl;
    private String version;
    private String credentialUsername;
    private String credentialPassword;
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getCredentialUsername() {
		return credentialUsername;
	}
	public void setCredentialUsername(String credentialUsername) {
		this.credentialUsername = credentialUsername;
	}
	public String getCredentialPassword() {
		return credentialPassword;
	}
	public void setCredentialPassword(String credentialPassword) {
		this.credentialPassword = credentialPassword;
	}
    
    
    
}
