package com.example.SINJA.service;

import com.example.SINJA.model.Student;

public interface StudentService {
    Student save(Student student);
    Student findById(Long id);
    void deleteById(Long id);
}
