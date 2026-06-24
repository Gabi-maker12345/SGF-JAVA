package com.SI.Crud.funcionarios_SI.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AuditService {
    private static final List<String> logs = Collections.synchronizedList(new ArrayList<>());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void logAction(String user, String action, String details) {
        if (user == null || user.trim().isEmpty()) user = "UNKNOWN_USER";
        String timestamp = LocalDateTime.now().format(formatter);
        String formattedLog = String.format("[%s] OPERADOR: %s | AÇÃO: %s | DETALHES: %s", 
                timestamp, user.toUpperCase(), action.toUpperCase(), details);
        logs.add(formattedLog);
        System.out.println(formattedLog);
    }

    public static List<String> getAllLogs() {
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }
}
