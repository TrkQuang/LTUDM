package com.ltudm.dao;

import com.ltudm.dto.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tầng DAO quản lý dữ liệu người dùng TCP Server đang kết nối.
 */
public class UserDAO {
    private final Map<String, User> activeUsers = new ConcurrentHashMap<>();

    public void addUser(User user) {
        if (user != null && user.getUsername() != null) {
            activeUsers.put(user.getUsername().toLowerCase(), user);
        }
    }

    public void removeUser(String username) {
        if (username != null) {
            activeUsers.remove(username.toLowerCase());
        }
    }

    public boolean exists(String username) {
        if (username == null) return false;
        return activeUsers.containsKey(username.toLowerCase());
    }

    public User getUser(String username) {
        if (username == null) return null;
        return activeUsers.get(username.toLowerCase());
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(activeUsers.values());
    }

    public int count() {
        return activeUsers.size();
    }

    public void clear() {
        activeUsers.clear();
    }
}
