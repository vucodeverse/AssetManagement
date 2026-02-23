package e2e.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelUtils {

    public static List<Object[]> readExcelData(String filePath, String sheetName) throws IOException {
        List<Object[]> data = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            
            // Skip header
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int lastCellNum = row.getLastCellNum();
                Object[] rowData = new Object[lastCellNum];

                for (int i = 0; i < lastCellNum; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData[i] = getCellValue(cell);
                }
                data.add(rowData);
            }
        }
        return data;
    }

    private static final Object lock = new Object();

    public static void updateResultColumn(String filePath, String sheetName, String testId, String status) throws IOException {
        synchronized (lock) {
            try (FileInputStream fis = new FileInputStream(filePath);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) sheet = workbook.getSheetAt(0);

                Row header = sheet.getRow(0);
                int resultCol = -1;
                for (int i = 0; i < header.getLastCellNum(); i++) {
                    if ("Result".equalsIgnoreCase(header.getCell(i).getStringCellValue())) {
                        resultCol = i;
                        break;
                    }
                }

                if (resultCol == -1) {
                    resultCol = header.getLastCellNum();
                    header.createCell(resultCol).setCellValue("Result");
                }

                for (Row row : sheet) {
                    Cell idCell = row.getCell(0);
                    if (idCell != null && idCell.getCellType() == CellType.STRING && testId.equals(idCell.getStringCellValue())) {
                        Cell resCell = row.createCell(resultCol);
                        resCell.setCellValue(status);
                        break;
                    }
                }

                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {
                    workbook.write(fos);
                }
            } catch (Exception e) {
                System.err.println("Excel update error for " + testId + ": " + e.getMessage());
            }
        }
    }

    public static void writeExcelData(String filePath, String sheetName, List<Object[]> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet(sheetName);
            int rowNum = 0;
            for (Object[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;
                for (Object field : rowData) {
                    Cell cell = row.createCell(colNum++);
                    if (field instanceof String) {
                        cell.setCellValue((String) field);
                    } else if (field instanceof Integer) {
                        cell.setCellValue((Integer) field);
                    } else if (field instanceof Double) {
                        cell.setCellValue((Double) field);
                    } else if (field instanceof Boolean) {
                        cell.setCellValue((Boolean) field);
                    }
                }
            }
            workbook.write(fos);
        }
    }

    private static Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue().toString();
                return String.valueOf((long)cell.getNumericCellValue());
            case BOOLEAN: return cell.getBooleanCellValue();
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }
}
