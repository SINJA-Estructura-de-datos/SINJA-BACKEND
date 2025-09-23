package com.example.SINJA.repository;

import com.example.SINJA.model.Student;

public interface StudentsRepository {
    Student save(Student student);
    boolean delete(Long id);
    Student findById(Long id);
}
