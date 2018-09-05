package com.apress.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * Created by hakdogan on 28/11/2017
 */


@Component

public class ConfigProps {

 
	@Autowired
    private Clients clients ;

	@Autowired
    private ElasticContainer elastic ;

	@Autowired
    private Index index ;

	public Clients getClients() {
		return clients;
	}

	public void setClients(Clients clients) {
		this.clients = clients;
	}

	public ElasticContainer getElastic() {
		return elastic;
	}

	public void setElastic(ElasticContainer elastic) {
		this.elastic = elastic;
	}

	public Index getIndex() {
		return index;
	}

	public void setIndex(Index index) {
		this.index = index;
	}
    
    
    
    
}
