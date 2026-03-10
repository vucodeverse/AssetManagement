package edu.fpt.groupfive.util.exception;

public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException(Integer id) {
        super("Không tìm thấy kho hàng với ID: " + id);
    }

    public WarehouseNotFoundException(String message) {
        super(message);
    }
}
