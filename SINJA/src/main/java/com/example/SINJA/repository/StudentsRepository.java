package com.example.SINJA.repository;

import com.example.SINJA.model.CampusUdea;
import com.example.SINJA.model.Student;
import java.util.List;

public interface StudentsRepository {
    Student save(Student student);
    boolean delete(Long id);
    Student findById(Long id);
    List<Student> findByCampus(CampusUdea campusUdea);
}
