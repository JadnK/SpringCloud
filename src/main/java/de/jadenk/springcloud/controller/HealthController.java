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
    private JdbcTemplate jdbcTemplate; // Für direkte DB-Abfragen

    @Autowired
    private ObjectMapper objectMapper; // Zum Umwandeln von Daten in JSON für das Frontend

    /*
     * GET /db-health
     * Zeigt eine Health-Seite für die Datenbank an:
     * - Name der aktuellen DB
     * - Tabellenstatistiken (Größe, Anzahl der Rows)
     * - Global Status-Variablen (z.B. Threads_connected, Uptime)
     * - Konfigurationsvariablen (z.B. max_connections)
     * Die Daten werden als JSON an das Thymeleaf-Template übergeben.
     */
    @GetMapping("/db-health")
    public String showHealthPage(Model model) throws Exception {
        // Aktuell verwendete Datenbank abrufen
        String dbName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);

        // Tabellenstatistiken (Größe in MB, Anzahl Rows) für die aktuelle DB
        String tableStatsSql = "SELECT table_name, " +
                "ROUND((data_length + index_length) / 1024 / 1024, 2) AS size_mb, " +
                "table_rows " +
                "FROM information_schema.tables " +
                "WHERE table_schema = ?";

        List<Map<String, Object>> tableStats = jdbcTemplate.queryForList(tableStatsSql, dbName);

        // Wichtige globale Statusvariablen abrufen
        String statusSql = "SHOW GLOBAL STATUS WHERE Variable_name IN (" +
                "'Threads_connected', 'Threads_running', 'Connections', 'Uptime', 'Max_used_connections'" +
                ")";
        List<Map<String, Object>> statusList = jdbcTemplate.queryForList(statusSql);

        // Wichtige Konfigurationsvariablen abrufen
        String variablesSql = "SHOW VARIABLES WHERE Variable_name IN (" +
                "'max_connections', 'wait_timeout', 'interactive_timeout'" +
                ")";
        List<Map<String, Object>> variablesList = jdbcTemplate.queryForList(variablesSql);

        // Alle gesammelten Daten in eine Map zusammenfassen
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

        return "db-health"; // Thymeleaf Template db-health.html
    }
}
