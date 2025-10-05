package com.example.SINJA.repository.impl;

import com.example.SINJA.model.CampusUdea;
import com.example.SINJA.model.Student;
import com.example.SINJA.repository.StudentsRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

@Repository
public class StudentsRepositoryImpl implements StudentsRepository {

    private static final Logger log = LoggerFactory.getLogger(StudentsRepositoryImpl.class);
    private static final String EXCEL_FILE_PATH = "SINJA/src/main/resources/Students.xlsx";

    private File getExcelFile() {
        try {
            File file = new File(EXCEL_FILE_PATH);
            if (!file.exists()) {
                log.error("El archivo Excel no existe en la ruta: {}", file.getAbsolutePath());
                return null;
            }
            return file;
        } catch (Exception e) {
            log.error("Error al acceder al archivo Excel: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Student save(Student student) {
        File excelFile = getExcelFile();
        if (excelFile == null) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet("Students");
            if (sheet == null) {
                log.error("No se encontró la hoja 'Students' en el archivo Excel");
                return null;
            }

            // Verificar ID único
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Saltar encabezados
                Cell idCell = row.getCell(0);
                if (idCell != null && (long)idCell.getNumericCellValue() == student.getId()) {
                    log.error("Ya existe un estudiante con el ID: {}", student.getId());
                    return null;
                }
            }

            // Crear nueva fila
            int lastRowNum = sheet.getLastRowNum();
            Row newRow = sheet.createRow(lastRowNum + 1);

            // Escribir datos del estudiante
            newRow.createCell(0).setCellValue(student.getId());
            newRow.createCell(1).setCellValue(student.getName());
            newRow.createCell(2).setCellValue(student.getLastName());
            newRow.createCell(3).setCellValue(student.getBornPlace());
            newRow.createCell(4).setCellValue(student.getDegree());
            newRow.createCell(5).setCellValue(student.getPlace().name());
            newRow.createCell(6).setCellValue(student.getScoreAdmision());

            // Guardar cambios
            try (FileOutputStream outputStream = new FileOutputStream(excelFile)) {
                workbook.write(outputStream);
                log.info("Estudiante guardado exitosamente con ID: {}", student.getId());
            }
            return student;

        } catch (IOException e) {
            log.error("Error al guardar estudiante en Excel: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean delete(Long id) {
        File excelFile = getExcelFile();
        if (excelFile == null) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet("Students");
            if (sheet == null) {
                log.error("No se encontró la hoja 'Students' en el archivo Excel");
                return false;
            }

            boolean deleted = false;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null && 
                    row.getCell(0).getNumericCellValue() == id) {
                    sheet.removeRow(row);
                    // Desplazar filas hacia arriba
                    if (i < sheet.getLastRowNum()) {
                        sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
                    }
                    deleted = true;
                    break;
                }
            }

            if (deleted) {
                try (FileOutputStream outputStream = new FileOutputStream(excelFile)) {
                    workbook.write(outputStream);
                    log.info("Estudiante con ID {} eliminado exitosamente", id);
                }
            } else {
                log.info("No se encontró estudiante con ID: {}", id);
            }
            return deleted;

        } catch (IOException e) {
            log.error("Error al eliminar estudiante del Excel: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Student findById(Long id) {
        File excelFile = getExcelFile();
        if (excelFile == null) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet("Students");
            if (sheet == null) {
                log.error("No se encontró la hoja 'Students' en el archivo Excel");
                return null;
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Saltar encabezados
                
                Cell idCell = row.getCell(0);
                if (idCell != null && (long)idCell.getNumericCellValue() == id) {
                    return new Student(
                        (long) row.getCell(0).getNumericCellValue(),
                        row.getCell(1).getStringCellValue(),
                        row.getCell(2).getStringCellValue(),
                        row.getCell(3).getStringCellValue(),
                        row.getCell(4).getStringCellValue(),
                        CampusUdea.valueOf(row.getCell(5).getStringCellValue()),
                        (int) row.getCell(6).getNumericCellValue()
                    );
                }
            }
            log.info("No se encontró estudiante con ID: {}", id);
            return null;

        } catch (IOException e) {
            log.error("Error al buscar estudiante en Excel: {}", e.getMessage());
            return null;
        }
    }
}
