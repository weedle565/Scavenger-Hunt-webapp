package com.ollie.mcsoc_hunt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/*
TODO:
- Documentation!
- More robust tests
- Real testing

 */

@SpringBootApplication(
        scanBasePackages = "com.ollie.mcsoc_hunt"
)

@EnableJpaRepositories(basePackages = "com.ollie.mcsoc_hunt.repositories")
@EntityScan(basePackages = "com.ollie.mcsoc_hunt.entities")
public class McsocHuntApplication {

    public static void main(String[] args) {
        SpringApplication.run(McsocHuntApplication.class, args);
    }

}
