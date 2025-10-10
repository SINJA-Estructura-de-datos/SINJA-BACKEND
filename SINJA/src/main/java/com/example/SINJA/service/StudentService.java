package com.example.SINJA.service;

import com.example.SINJA.model.CampusUdea;
import com.example.SINJA.model.Student;

import java.util.List;

public interface StudentService {
    Student save(Student student);
    Student findById(Long id);
    boolean deleteById(Long id);
    List<Student> findByCampus(CampusUdea campusUdea);
}
