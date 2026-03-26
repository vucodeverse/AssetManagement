package edu.fpt.groupfive.util.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.ui.Model;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

//    @ExceptionHandler(InvalidDataException.class)
//    public String handleInvalidData(InvalidDataException e, Model model) {
//        log.warn("Invalid data: {}", e.getMessage());
//        model.addAttribute("errorMessage", e.getMessage());
//        return "error/error";
//    }

//    @ExceptionHandler(NumberFormatException.class)
//    public String handleNumberFormat(NumberFormatException e, Model model) {
//        log.warn("Number format error: {}", e.getMessage());
//        model.addAttribute("errorMessage", "Đường dẫn không hợp lệ.");
//        return "error/error";
//    }
//
//    @ExceptionHandler(Exception.class)
//    public String handleGeneral(Exception e, Model model) {
//        log.error("Unexpected error", e);
//        model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
//        return "error/error";
//    }
}
