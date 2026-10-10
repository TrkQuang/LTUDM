package com.ltudm.dto;

/**
 * Lớp DTO Sinh viên phục vụ Bài tập mở rộng #2 (Trả danh sách sinh viên dạng JSON).
 */
public class Student {
    private String id;
    private String name;
    private String major;
    private double gpa;
    private String email;

    public Student() {}

    public Student(String id, String name, String major, double gpa, String email) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.gpa = gpa;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
