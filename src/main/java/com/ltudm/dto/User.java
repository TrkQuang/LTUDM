package com.ltudm.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lớp DTO đại diện cho thông tin người dùng kết nối TCP Server.
 */
public class User {
    private String id;
    private String username;
    private String ipAddress;
    private int port;
    private String connectedAt;

    public User(String id, String username, String ipAddress, int port) {
        this.id = id;
        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;
        this.connectedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(String connectedAt) {
        this.connectedAt = connectedAt;
    }
}
