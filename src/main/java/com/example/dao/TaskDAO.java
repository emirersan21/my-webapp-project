package com.example.dao;

import com.example.model.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private static final String DATABASE_URL = "jdbc:sqlite:/Users/emirersan/my-webapp-project/todo.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public TaskDAO() {
        initializeDatabase();
    }

    /**
     * Initialize the database by creating the tasks table if it doesn't exist.
     */
    private void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "completed BOOLEAN NOT NULL DEFAULT 0" +
                ")";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all tasks from the database.
     */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT * FROM tasks";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getBoolean("completed")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    /**
     * Get a task by its ID.
     */
    public Task getTaskById(int id) {
        String query = "SELECT * FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Task(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getBoolean("completed")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Create a new task in the database.
     */
    public Task createTask(String title, boolean completed) {
        String insertSQL = "INSERT INTO tasks (title, completed) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, title);
            pstmt.setBoolean(2, completed);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new Task(id, title, completed);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update an existing task.
     */
    public boolean updateTask(int id, String title, boolean completed) {
        String updateSQL = "UPDATE tasks SET title = ?, completed = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setString(1, title);
            pstmt.setBoolean(2, completed);
            pstmt.setInt(3, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Delete a task by its ID.
     */
    public boolean deleteTask(int id) {
        String deleteSQL = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
