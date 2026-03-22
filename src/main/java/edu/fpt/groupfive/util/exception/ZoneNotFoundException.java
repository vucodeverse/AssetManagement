package edu.fpt.groupfive.util.exception;

public class ZoneNotFoundException extends RuntimeException {
    public ZoneNotFoundException(Integer id) {
        super("Không tìm thấy khu vực lưu trữ (Zone) với ID: " + id);
    }

    public ZoneNotFoundException(String message) {
        super(message);
    }
}
