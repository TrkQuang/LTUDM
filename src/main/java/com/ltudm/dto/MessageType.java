package com.ltudm.dto;

/**
 * Các loại gói tin trao đổi trong hệ thống TCP Chat.
 */
public enum MessageType {
    CONNECT,        // Client yêu cầu kết nối và gửi tên mong muốn
    CONNECT_OK,     // Server chấp nhận kết nối và trả về tên chính thức
    CONNECT_FAIL,   // Server từ chối kết nối (trùng tên hoặc lỗi)
    PUBLIC,         // Tin nhắn công khai (Broadcast đến tất cả client)
    PRIVATE,        // Tin nhắn riêng tư giữa 2 người dùng (Whisper)
    USER_LIST,      // Gói tin cập nhật danh sách người dùng đang online
    SYSTEM,         // Thông báo hệ thống từ Server
    LEAVE           // Client chủ động ngắt kết nối / rời phòng
}
