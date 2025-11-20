package com.example.SINJA.repository.impl;

import com.example.SINJA.model.CampusUdea;
import com.example.SINJA.model.Student;
import com.example.SINJA.model.Tuple;
import com.example.SINJA.repository.StudentsRepository;
import com.example.SINJA.treeB.TreeBPlus;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Repository
public class StudentsRepositoryImpl implements StudentsRepository {

    private static final Logger log = LoggerFactory.getLogger(StudentsRepositoryImpl.class);
    private final String studentsTxt = "SINJA/src/main/resources/Students";

    private final TreeBPlus treeBPlusId = new TreeBPlus();
    private final TreeBPlus treBPlusCampus = new TreeBPlus();

    public StudentsRepositoryImpl() {
        rebuildTreeBPlus();
        treeBPlusId.printTree();
        treBPlusCampus.printTree();
    }


    @Override
    public Student save(Student student) {
        if (findById(student.getId()) != null) {
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

            byte[] data = register.getBytes(StandardCharsets.UTF_8);
            raf.write(data);

            Tuple key = new Tuple(student.getId(), pos);
            treeBPlusId.insert(key);

            Long campus = (long) student.getPlace().getCode();
            Tuple campusKey = new Tuple(campus, student.getId());
            treBPlusCampus.insert(campusKey);

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
        Tuple tuple = treeBPlusId.searchId(id);
        Long pos;
        if (tuple==null) {
            log.warn("No se encontró ningún estudiante con ID " + id);
            return null;
        }else{
            pos = tuple.getAddress();
        }

        try (RandomAccessFile raf = new RandomAccessFile(studentsTxt, "r")) {
            raf.seek(pos);
            String linea = raf.readLine();
            if (linea != null) {
                linea = new String(linea.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
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
        List<Tuple> keys = treBPlusCampus.searchCampus((long) campusUdea.getCode());
        List<Student> students = new ArrayList<>();
        List<Long> ids = new ArrayList<>();


        if (keys == null) {
            log.warn("No se encontraron estudiantes para el campus " + campusUdea);
            return students;
        }

        for(int i = 0; i < keys.size(); i++){
            ids.add(keys.get(i).getAddress());
        }

        for(int j = 0; j < ids.size(); j++){
            students.add(findById(ids.get(j)));
        }
        return students;

    }


    public void rebuildTreeBPlus(){
        try(RandomAccessFile reader = new RandomAccessFile(studentsTxt, "r")){
            String line;
            boolean flag = true;
            while(flag) {
                Long pos = reader.getFilePointer();
                if ((line = reader.readLine()) != null) {
                    String[] campos = line.split("\t");
                    Student student = new Student(
                            Long.parseLong(campos[0]),
                            campos[1],
                            campos[2],
                            campos[3],
                            campos[4],
                            CampusUdea.valueOf(campos[5]),
                            Integer.parseInt(campos[6]));
                    Tuple tupleId = new Tuple(student.getId(), pos);
                    treeBPlusId.insert(tupleId);

                    Tuple tupleCampus = new Tuple((long) student.getPlace().getCode(), student.getId());
                    treBPlusCampus.insert(tupleCampus);
                }else{
                    flag = false;
                }
            }

        }catch (IOException e){
            log.error("No se realizó la reconstrucción del árbol" + e.getMessage());
        }
    }

}
