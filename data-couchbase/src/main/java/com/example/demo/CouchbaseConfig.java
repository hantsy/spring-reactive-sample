/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.couchbase.config.AbstractReactiveCouchbaseConfiguration;
import org.springframework.data.couchbase.core.RxJavaCouchbaseTemplate;
import org.springframework.data.couchbase.core.WriteResultChecking;
import org.springframework.data.couchbase.core.query.Consistency;
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories;
import org.springframework.data.couchbase.repository.support.IndexManager;

/**
 *
 * @author hantsy
 */
@Configuration
@EnableReactiveCouchbaseRepositories(basePackageClasses = {CouchbaseConfig.class})
public class CouchbaseConfig extends AbstractReactiveCouchbaseConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public String couchbaseAdminUser() {
        return env.getProperty("couchbase.adminUser", "Administrator");
    }

    @Bean
    public String couchbaseAdminPassword() {
        return env.getProperty("couchbase.adminPassword", "password");
    }

    @Override
    protected List<String> getBootstrapHosts() {
        return Collections.singletonList(env.getProperty("couchbase.host", "127.0.0.1"));
    }

    @Override
    protected String getBucketName() {
        return env.getProperty("couchbase.bucket", "default");
    }

    @Override
    protected String getBucketPassword() {
        return env.getProperty("couchbase.password", "");
    }

    @Override
    protected CouchbaseEnvironment getEnvironment() {
        return DefaultCouchbaseEnvironment.builder()
            .connectTimeout(10000)
            .kvTimeout(10000)
            .queryTimeout(10000)
            .viewTimeout(10000)
            .build();
    }

    @Override
    public RxJavaCouchbaseTemplate reactiveCouchbaseTemplate() throws Exception {
        RxJavaCouchbaseTemplate template = super.reactiveCouchbaseTemplate();
        template.setWriteResultChecking(WriteResultChecking.LOG);
        return template;
    }

    //this is for dev so it is ok to auto-create indexes
    @Override
    public IndexManager indexManager() {
        return new IndexManager();
    }

    @Override
    protected Consistency getDefaultConsistency() {
        return Consistency.READ_YOUR_OWN_WRITES;
    }

}
