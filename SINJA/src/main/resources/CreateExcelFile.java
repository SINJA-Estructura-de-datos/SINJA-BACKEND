import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateExcelFile {
    public static void main(String[] args) {
        String[] headers = {"ID", "Name", "LastName", "BornPlace", "Degree", "Campus", "ScoreAdmision"};
        Object[][] data = {
            {10L, "Camilo", "Perez", "PR", "Ingeniería de Sistemas", "MEDELLIN", 70},
            {20L, "Pedro", "Malo", "Turbo", "Ingeniería Aeroespacial", "MEDELLIN", 50},
            {60L, "Milena", "Popu", "Bello", "Ingeniería Ambiental", "MEDELLIN", 90},
            {999L, "Test", "Usuario", "Test", "Test", "MEDELLIN", 85},
            {1001L, "TestUser", "Frontend", "Test", "Ingeniería de Sistemas", "MEDELLIN", 85},
            {3001L, "TestUser", "Prueba", "Medellín", "Ingeniería de Sistemas", "MEDELLIN", 85},
            {4001L, "TestMinimo", "Usuario", "Test", "Ingeniería de Sistemas", "MEDELLIN", 85},
            {777L, "Manu", "Oso", "Bolívar", "Ingeniería Química", "MEDELLIN", 90}
        };

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");
            
            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Añadir datos
            int rowNum = 1;
            for (Object[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int colNum = 0; colNum < rowData.length; colNum++) {
                    Cell cell = row.createCell(colNum);
                    Object value = rowData[colNum];
                    if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Long) {
                        cell.setCellValue((Long) value);
                    } else if (value instanceof Integer) {
                        cell.setCellValue((Integer) value);
                    }
                }
            }

            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            try (FileOutputStream outputStream = new FileOutputStream("src/main/resources/Students.xlsx")) {
                workbook.write(outputStream);
                System.out.println("Archivo Excel creado exitosamente");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}