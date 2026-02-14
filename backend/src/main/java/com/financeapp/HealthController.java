package com.financeapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class HealthController {
    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/db-test")
    public String textConnection() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            return "Connect to DB as: " + connection.getMetaData().getUserName();
        }
    }
}