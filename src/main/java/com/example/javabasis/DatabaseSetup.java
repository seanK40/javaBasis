package com.example.javabasis; // Match your actual package name

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSetup {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/jwt_lesson";
        String user = "postgres";
        String password = "postgres";

        // Using 'users' (plural) because 'user' is a reserved keyword in PostgreSQL
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users (id BIGINT);";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("Table 'users' created successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}