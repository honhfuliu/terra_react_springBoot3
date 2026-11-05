package com.ziheng.system.sysmonitor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@RestController
public class TestController1 {
    private final DataSource dataSource;
    public TestController1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/test/active-connection")
    public String testActiveConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT SLEEP(20)");
        }
        return "测试完成";
    }
}
