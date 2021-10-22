package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;

@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer implements ApplicationRunner {

  private final DatabaseClient databaseClient;

  private final TransactionalOperator transactionalOperator;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("start data initialization...");
    transactionalOperator
        .execute(
            status ->
                this.databaseClient
                    .sql(
                        () ->
                            "insert into posts (title, content) values ('First post title','Content of my first post') ")
                    .map((r, m) -> r.get("id", Integer.class))
                    .all()
                    .log()
                    .thenMany(
                        this.databaseClient
                            .sql("select * from posts order by id desc")
                            .fetch()
                            .all()
                            .log()))
        .subscribe(null, null, () -> log.info("initialization is done..."));
  }
}
