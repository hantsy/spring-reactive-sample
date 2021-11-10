/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories;

/**
 *
 * @author hantsy
 */
@Configuration
@EnableReactiveCouchbaseRepositories(basePackageClasses = {CouchbaseConfig.class})
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @Autowired
    private Environment env;

    @Override
    public String getConnectionString() {
        return env.getProperty("couchbase.host", "couchbase://127.0.0.1");
    }

    @Override
    public String getUserName() {
        return env.getProperty("couchbase.adminUser", "Administrator");
    }

    @Override
    public String getPassword() {
        return env.getProperty("couchbase.adminPassword", "password");
    }

    @Override
    public String getBucketName() {
        return env.getProperty("couchbase.bucket", "default");
    }

}
