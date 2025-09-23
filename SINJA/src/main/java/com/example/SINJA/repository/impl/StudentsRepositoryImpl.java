package com.example.SINJA.repository.impl;

import com.example.SINJA.model.CampusUdea;
import com.example.SINJA.model.Student;
import com.example.SINJA.repository.StudentsRepository;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

@Repository
public class StudentsRepositoryImpl implements StudentsRepository {

    private static final Logger log = LoggerFactory.getLogger(StudentsRepositoryImpl.class);

    @Override
    public Student save(Student student) {
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter("SINJA/src/main/resources/Students", true));
                    writer.write((student.getId())+"\t"+student.getName()+"\t"+student.getLastName()
                    +"\t"+student.getBornPlace()+"\t"+student.getDegree()+"\t"+student.getPlace().name()+"\t"+(student.getScoreAdmision()));
                    writer.newLine();
                    writer.close();
        }catch(IOException e){
            log.error("The student has not been save " + e.getMessage());
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


    @Override
    public Student findById(Long id) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader("SINJA/src/main/resources/Students"));
            String line;
            while ((line = reader.readLine()) != null){
                String[] data = line.split("\t");
                if(Long.parseLong(data[0]) == id){
                    return new Student(id, data[1], data[2], data[3], data[4], CampusUdea.valueOf(data[5]), Integer.parseInt(data[6]));
                }

            }
        }catch (IOException e){
            log.error("The student has not been found " + e.getMessage());
        }
        return null;
    }
}
