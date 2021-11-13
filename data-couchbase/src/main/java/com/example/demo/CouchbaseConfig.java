/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories;

/**
 * @author hantsy
 */
@Configuration
@EnableReactiveCouchbaseRepositories(basePackageClasses = {CouchbaseConfig.class})
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @Value("${couchbase.user}")
    private String user;

    @Value("${couchbase.password}")
    private String password;

    @Value("${couchbase.connection-string}")
    private String connectionString;

    @Value("${couchbase.bucket-name}")
    private String bucket;

    @Override
    public String getConnectionString() {
        return this.connectionString;
    }

    @Override
    public String getUserName() {
        return this.user;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getBucketName() {
        return this.bucket;
    }


}
