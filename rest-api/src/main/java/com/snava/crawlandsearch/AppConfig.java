package com.snava.crawlandsearch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Bean
  public Searcher searcher() {
    return new Searcher();
  }

  @Bean
  public Crawler crawler() {
    return new CrawlerController("local-data/crawler4j");
  }
}
