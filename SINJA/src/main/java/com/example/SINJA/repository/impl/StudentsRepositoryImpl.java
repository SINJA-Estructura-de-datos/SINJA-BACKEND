package com.example.SINJA.repository.impl;

import com.example.SINJA.model.CampusUdea;
import com.example.SINJA.model.Student;
import com.example.SINJA.repository.StudentsRepository;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Repository
public class StudentsRepositoryImpl implements StudentsRepository {

    private static final Logger log = LoggerFactory.getLogger(StudentsRepositoryImpl.class);
    private final String studentsTxt = "SINJA/src/main/resources/Students";
    private final String indexTxt = "SINJA/src/main/resources/Index";
    private final String indexCampusTxt = "SINJA/src/main/resources/IndexCampus";

    private final Map<String, Long> index = new HashMap<>();
    private final Map<String, List<Long>> indexCampus = new HashMap<>();

    public StudentsRepositoryImpl() {
        loadIndex();
        loadIndexCampus();
        rebuildIndicesFromStudents();
    }


    @Override
    public Student save(Student student) {
        if (index.containsKey(String.valueOf(student.getId()))) {
            log.warn("El estudiante con ID " + student.getId() + " ya existe.");
            return null;
        }

        try (RandomAccessFile raf = new RandomAccessFile(studentsTxt, "rw")) {
            long pos = raf.length();
            raf.seek(pos);

            String register = student.getId() + "\t" +
                    student.getName() + "\t" +
                    student.getLastName() + "\t" +
                    student.getBornPlace() + "\t" +
                    student.getDegree() + "\t" +
                    student.getPlace().name() + "\t" +
                    student.getScoreAdmision() + "\n";
            raf.writeBytes(register);


            index.put(String.valueOf(student.getId()), pos);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(indexTxt, true))) {
                bw.write(student.getId() + "|" + pos);
                bw.newLine();
            }

            indexCampus.computeIfAbsent(student.getPlace().name(), k -> new ArrayList<>())
                    .add(pos);
            try (BufferedWriter bw1 = new BufferedWriter(new FileWriter(indexCampusTxt, true))) {
                bw1.write(student.getPlace().name() + "|" + pos);
                bw1.newLine();
            }

            writeAggregatedIndexCampus();

            log.info("Estudiante guardado correctamente en posición: " + pos);

        } catch (IOException e) {
            log.error("Error al guardar el estudiante: " + e.getMessage());
        }

        return student;
    }

    @Override
    public boolean delete(Long id) {
        File students = new File("SINJA/src/main/resources/Students");
        File temp = new File("SINJA/src/main/resources/temp.txt");

        boolean deleted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(students));
             BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\t");

                if (Long.parseLong(data[0]) == id) {
                    log.info("Estudiante con id={} fue encontrado y eliminado", id);
                    deleted = true;
                    // no se escribe esta línea
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            log.error("Error eliminando estudiante: " + e.getMessage());
            return false;
        }

        // reemplazar archivo original
        if (deleted) {
            if (!students.delete()) {
                log.error("No se pudo eliminar el archivo original");
                return false;
            }
            if (!temp.renameTo(students)) {
                log.error("No se pudo renombrar el archivo temporal");
                return false;
            }
        } else {
            // si no se eliminó, borrar el temporal
            temp.delete();
        }

        return deleted;

    }


    public Student findById(Long id) {
        String code = String.valueOf(id);
        Long pos = index.get(code);
        if (pos == null) {
            log.warn("No se encontró ningún estudiante con ID " + id);
            return null;
        }

        try (RandomAccessFile raf = new RandomAccessFile(studentsTxt, "r")) {
            raf.seek(pos);
            String linea = raf.readLine();
            if (linea != null) {
                String[] campos = linea.split("\t");
                return new Student(
                        Long.parseLong(campos[0]),
                        campos[1],
                        campos[2],
                        campos[3],
                        campos[4],
                        CampusUdea.valueOf(campos[5]),
                        Integer.parseInt(campos[6])
                );
            }
        } catch (IOException e) {
            log.error("Error al leer el estudiante: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Student> findByCampus(CampusUdea campusUdea) {
        List<Long> posiciones = indexCampus.get(campusUdea.name());
        List<Student> resultado = new ArrayList<>();

        if (posiciones == null) {
            log.warn("No se encontraron estudiantes para el campus " + campusUdea);
            return resultado;
        }

        try (RandomAccessFile raf = new RandomAccessFile(studentsTxt, "r")) {
            for (Long pos : posiciones) {
                raf.seek(pos);
                String linea = raf.readLine();
                if (linea != null) {
                    String[] campos = linea.split("\t");
                    resultado.add(new Student(
                            Long.parseLong(campos[0]),
                            campos[1],
                            campos[2],
                            campos[3],
                            campos[4],
                            CampusUdea.valueOf(campos[5]),
                            Integer.parseInt(campos[6])
                    ));
                }
            }
        } catch (IOException e) {
            log.error("Error al leer los estudiantes del campus: " + e.getMessage());
        }

        return resultado;
    }


    private void loadIndex() {
        try (BufferedReader br = new BufferedReader(new FileReader(indexTxt))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] parts = linea.split("\\|");
                if (parts.length == 2) {
                    String id = parts[0];
                    long pos = Long.parseLong(parts[1]);
                    index.put(id, pos);
                }
            }
            log.info("Archivo cargado en memoria con:" + index.size() + " registros");
        } catch (FileNotFoundException e) {
            log.warn("No se encontró el archivo de índice");
        } catch (IOException e) {
            log.error("Error al cargar el índice: " + e.getMessage());
        }
    }

    private void loadIndexCampus() {
        File f = new File(indexCampusTxt);
        if (!f.exists()) {
            log.warn("No se encontró el archivo de índice de campus");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] parts = linea.split("\\|", 2);
                if (parts.length != 2) continue;
                String campus = parts[0];
                String rest = parts[1].trim();

                if (rest.startsWith("[") && rest.endsWith("]")) {
                    String inner = rest.substring(1, rest.length() - 1).trim();
                    if (!inner.isEmpty()) {
                        String[] nums = inner.split("\\s*,\\s*");
                        for (String n : nums) {
                            long pos = Long.parseLong(n);
                            indexCampus.computeIfAbsent(campus, k -> new ArrayList<>()).add(pos);
                        }
                    }
                } else {
                    // formato legacy: una línea por entrada: CAUCASIA|60
                    long pos = Long.parseLong(rest);
                    indexCampus.computeIfAbsent(campus, k -> new ArrayList<>()).add(pos);
                }
            }
            log.info("Índice de campus cargado con: " + indexCampus.size() + " claves");
        } catch (IOException e) {
            log.error("Error al cargar índice de campus: " + e.getMessage());
        }
    }

    private void writeAggregatedIndexCampus() {
        // escribe archivo temporal y lo mueve (atomic-ish)
        File tmp = new File(indexCampusTxt + ".tmp");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
            // ordenar claves para reproducibilidad opcional
            List<String> claves;
            synchronized (indexCampus) {
                claves = new ArrayList<>(indexCampus.keySet());
            }
            Collections.sort(claves);
            for (String campus : claves) {
                List<Long> posiciones;
                synchronized (indexCampus) {
                    posiciones = new ArrayList<>(indexCampus.getOrDefault(campus, Collections.emptyList()));
                }
                if (posiciones.isEmpty()) continue;
                Collections.sort(posiciones);
                // ejemplo: CAUCASIA|[0,60,182]
                bw.write(campus + "|" + posiciones.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            log.error("Error al escribir indexCampus temporal: " + e.getMessage());
            return;
        }

        try {
            Files.move(tmp.toPath(), Paths.get(indexCampusTxt), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error al mover archivo temporal de índice de campus: " + e.getMessage());
        }
    }

    private void writeIndexFile() {
        File tmp = new File(indexTxt + ".tmp");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
            for (Map.Entry<String, Long> e : index.entrySet()) {
                bw.write(e.getKey() + "|" + e.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            log.error("Error al escribir index temporal: " + e.getMessage());
            return;
        }
        try {
            Files.move(tmp.toPath(), Paths.get(indexTxt), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error al mover archivo temporal de índice: " + e.getMessage());
        }
    }

    private void rebuildIndicesFromStudents() {
        index.clear();
        indexCampus.clear();

        File f = new File(studentsTxt);
        if (!f.exists()) {
            log.warn("Archivo students no existe cuando se intenta reconstruir índices");
            return;
        }



        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            long pos;
            String line;
            while ((pos = raf.getFilePointer()) < raf.length() && (line = raf.readLine()) != null) {
                String[] campos = line.split("\t");
                if (campos.length >= 7) {
                    String id = campos[0];
                    index.put(id, pos);
                    String campus = campos[5];
                    indexCampus.computeIfAbsent(campus, k -> new ArrayList<>()).add(pos);
                }
            }
        } catch (IOException e) {
            log.error("Error reconstruyendo índices desde Students: " + e.getMessage());
        }

        // persistir índices reconstruidos
        writeIndexFile();
        writeAggregatedIndexCampus();
        log.info("Índices reconstruidos desde archivo Students. registros=" + index.size());
    }

}
