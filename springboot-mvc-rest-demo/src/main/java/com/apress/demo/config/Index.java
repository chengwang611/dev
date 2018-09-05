package com.apress.demo.config;
/*
 * Created by hakdogan on 01/12/2017
 */

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;



@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("app.index")
public class Index {
    private String name;
    private String type;
    private int shard;
    private int replica;
    private int from;
    private int size;
    private int timeout;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getShard() {
		return shard;
	}
	public void setShard(int shard) {
		this.shard = shard;
	}
	public int getReplica() {
		return replica;
	}
	public void setReplica(int replica) {
		this.replica = replica;
	}
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
    
    
    
}
