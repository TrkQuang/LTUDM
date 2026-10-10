package com.ltudm.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ltudm.dto.Student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tầng DAO quản lý dữ liệu Sinh viên
 * (Trả danh sách sinh viên dạng JSON qua GET /api/students).
 */
public class StudentDAO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final List<Student> studentList = Collections.synchronizedList(new ArrayList<>());

    public StudentDAO() {
        initSampleData();
    }

    private void initSampleData() {
        studentList.add(new Student("SV001", "Nguyễn Văn An", "Công nghệ thông tin", 3.82, "annv@ptit.edu.vn"));
        studentList.add(new Student("SV002", "Trần Thị Bình", "Khoa học máy tính", 3.65, "binhtt@ptit.edu.vn"));
        studentList.add(new Student("SV003", "Lê Hoàng Cường", "An toàn thông tin", 3.50, "cuonglh@ptit.edu.vn"));
        studentList.add(new Student("SV004", "Phạm Thu Dung", "Hệ thống thông tin", 3.90, "dungpt@ptit.edu.vn"));
        studentList.add(new Student("SV005", "Vũ Minh Đức", "Kỹ thuật phần mềm", 3.75, "ducvm@ptit.edu.vn"));

        String[] firstNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý"};
        String[] middleNames = {"Văn", "Thị", "Hoàng", "Minh", "Thu", "Đức", "Ngọc", "Hữu", "Xuân", "Hải", "Tuấn", "Thanh"};
        String[] lastNames = {"An", "Bình", "Cường", "Dung", "Đức", "Giang", "Hải", "Hoa", "Hùng", "Hương", "Huy", "Khang", "Khánh", "Khoa", "Kiên", "Lâm", "Lan", "Linh", "Long", "Mai", "Minh", "Nga", "Ngọc", "Nhung", "Oanh", "Phong", "Phúc", "Phương", "Quân", "Quang", "Quyên", "Sơn", "Tài", "Tâm", "Thảo", "Thắng", "Thành", "Thủy", "Tiến", "Toàn", "Trang", "Trí", "Trinh", "Trung", "Tú", "Tuấn", "Uyên", "Vân", "Việt", "Vinh", "Vy", "Xuân", "Yến"};
        String[] majors = {"Công nghệ thông tin", "Khoa học máy tính", "Kỹ thuật phần mềm", "Hệ thống thông tin", "An toàn thông tin", "Mạng máy tính", "Trí tuệ nhân tạo", "Khoa học dữ liệu", "Kỹ thuật máy tính"};

        for (int i = 6; i <= 128; i++) {
            String id = String.format("SV%03d", i);
            String first = firstNames[(i * 7) % firstNames.length];
            String middle = middleNames[(i * 13) % middleNames.length];
            String last = lastNames[(i * 17) % lastNames.length];
            String fullName = first + " " + middle + " " + last;
            String major = majors[(i * 19) % majors.length];
            double gpa = 2.0 + ((i * 37) % 200) / 100.0; // Random GPA between 2.00 and 3.99
            
            // Generate email based on name parts
            String emailPrefix = last.toLowerCase() + first.substring(0, 1).toLowerCase() + middle.substring(0, 1).toLowerCase();
            // Remove vietnamese accents for email
            emailPrefix = emailPrefix.replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a")
                                     .replaceAll("[éèẻẽẹêếềểễệ]", "e")
                                     .replaceAll("[íìỉĩị]", "i")
                                     .replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o")
                                     .replaceAll("[úùủũụưứừửữự]", "u")
                                     .replaceAll("[ýỳỷỹỵ]", "y")
                                     .replaceAll("đ", "d");
            String email = emailPrefix + "@ptit.edu.vn";
            
            studentList.add(new Student(id, fullName, major, gpa, email));
        }
    }

    public List<Student> getAll() {
        return new ArrayList<>(studentList);
    }

    public synchronized boolean addStudent(Student student) {
        if (student == null || student.getId() == null) return false;
        // Kiểm tra xem đã có sinh viên với ID này chưa
        for (Student s : studentList) {
            if (s.getId().equalsIgnoreCase(student.getId())) {
                return false;
            }
        }
        studentList.add(student);
        return true;
    }

    public synchronized boolean updateStudent(Student student) {
        if (student == null || student.getId() == null) return false;
        for (int i = 0; i < studentList.size(); i++) {
            if (studentList.get(i).getId().equalsIgnoreCase(student.getId())) {
                studentList.set(i, student);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteStudent(String id) {
        if (id == null) return false;
        return studentList.removeIf(s -> s.getId().equalsIgnoreCase(id));
    }

    public Student findById(String id) {
        if (id == null) return null;
        for (Student s : studentList) {
            if (s.getId().equalsIgnoreCase(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Xuất danh sách sinh viên thành chuỗi JSON chuẩn.
     */
    public String toJson() {
        return GSON.toJson(getAll());
    }
}
