/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apress.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.apress.demo.config.Clients;
import com.apress.demo.config.ElasticContainer;
import com.apress.demo.config.Index;
import com.apress.demo.config.YAMLConfig;

@SpringBootApplication
public class ElasticSearchStarter implements CommandLineRunner {

    @Autowired
    Clients client;
    @Autowired
    ElasticContainer elasticContainer;
    
    @Autowired
    Index index;
    

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ElasticSearchStarter.class);
        app.run();
    }

    public void run(String... args) throws Exception {
        System.out.println("using host:" + client.getHostname());
        System.out.println("http port:" + client.getHttpPort());
        System.out.println("elasticContainer ImageUrl :" + elasticContainer.getImageUrl());
        System.out.println("Index name :" + index.getName());
       
    }

}
