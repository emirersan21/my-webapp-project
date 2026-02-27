package com.example.servlet;

import com.example.model.Task;
import com.example.dao.TaskDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/tasks/*")
public class TasksServlet extends HttpServlet {
    private TaskDAO taskDAO;

    @Override
    public void init() {
        taskDAO = new TaskDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        try {
            // GET /api/tasks - Get all tasks
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Task> tasks = taskDAO.getAllTasks();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(tasksToJson(tasks));
            }
            // GET /api/tasks/{id} - Get task by ID
            else {
                String idStr = pathInfo.substring(1); // Remove leading /
                try {
                    int id = Integer.parseInt(idStr);
                    Task task = taskDAO.getTaskById(id);
                    
                    if (task != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(taskToJson(task));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Task not found\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid task ID\"}");
                }
            }
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();

        try {
            String title = request.getParameter("title");
            String completedStr = request.getParameter("completed");
            
            if (title == null || title.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Title is required\"}");
                return;
            }

            boolean completed = completedStr != null && Boolean.parseBoolean(completedStr);
            Task task = taskDAO.createTask(title, completed);

            if (task != null) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(taskToJson(task));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Failed to create task\"}");
            }
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Task ID is required\"}");
                return;
            }

            String idStr = pathInfo.substring(1); // Remove leading /
            try {
                int id = Integer.parseInt(idStr);
                String title = request.getParameter("title");
                String completedStr = request.getParameter("completed");

                if (title == null || title.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Title is required\"}");
                    return;
                }

                boolean completed = completedStr != null && Boolean.parseBoolean(completedStr);
                boolean success = taskDAO.updateTask(id, title, completed);

                if (success) {
                    Task task = taskDAO.getTaskById(id);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(taskToJson(task));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Task not found\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid task ID\"}");
            }
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Task ID is required\"}");
                return;
            }

            String idStr = pathInfo.substring(1); // Remove leading /
            try {
                int id = Integer.parseInt(idStr);
                boolean success = taskDAO.deleteTask(id);

                if (success) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print("{\"message\":\"Task deleted successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Task not found\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid task ID\"}");
            }
        } finally {
            out.flush();
        }
    }

    /**
     * Convert a single task to JSON format.
     */
    private String taskToJson(Task task) {
        return String.format(
                "{\"id\":%d,\"title\":\"%s\",\"completed\":%s}",
                task.getId(),
                escapeJson(task.getTitle()),
                task.isCompleted()
        );
    }

    /**
     * Convert a list of tasks to JSON array format.
     */
    private String tasksToJson(List<Task> tasks) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append(taskToJson(tasks.get(i)));
            if (i < tasks.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Escape JSON special characters in strings.
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
