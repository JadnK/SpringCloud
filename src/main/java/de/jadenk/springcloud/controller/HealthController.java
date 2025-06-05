package de.jadenk.springcloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/db-health")
    public String showHealthPage(Model model) throws Exception {
        String dbName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);

        String tableStatsSql = "SELECT table_name, " +
                "ROUND((data_length + index_length) / 1024 / 1024, 2) AS size_mb, " +
                "table_rows " +
                "FROM information_schema.tables " +
                "WHERE table_schema = ?";

        List<Map<String, Object>> tableStats = jdbcTemplate.queryForList(tableStatsSql, dbName);

        String statusSql = "SHOW GLOBAL STATUS WHERE Variable_name IN (" +
                "'Threads_connected', 'Threads_running', 'Connections', 'Uptime', 'Max_used_connections'" +
                ")";
        List<Map<String, Object>> statusList = jdbcTemplate.queryForList(statusSql);

        String variablesSql = "SHOW VARIABLES WHERE Variable_name IN (" +
                "'max_connections', 'wait_timeout', 'interactive_timeout'" +
                ")";
        List<Map<String, Object>> variablesList = jdbcTemplate.queryForList(variablesSql);

        Map<String, Object> data = Map.of(
                "dbName", dbName,
                "tableStats", tableStats,
                "status", statusList.stream()
                        .collect(Collectors.toMap(
                                m -> (String) m.get("Variable_name"),
                                m -> m.get("Value")
                        )),
                "variables", variablesList.stream()
                        .collect(Collectors.toMap(
                                m -> (String) m.get("Variable_name"),
                                m -> m.get("Value")
                        ))
        );

        // Daten als JSON für JS im Template verfügbar machen
        String jsonData = objectMapper.writeValueAsString(data);

        model.addAttribute("dbHealthJson", jsonData);

        return "db-health"; // Thymeleaf template db-health.html
    }
}
