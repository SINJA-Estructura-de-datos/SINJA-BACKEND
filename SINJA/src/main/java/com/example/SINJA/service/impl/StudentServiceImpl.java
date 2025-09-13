package com.example.SINJA.service.impl;

import com.example.SINJA.model.Student;
import com.example.SINJA.repository.StudentsRepository;
import com.example.SINJA.service.StudentService;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentsRepository studentsRepository;

    public StudentServiceImpl(StudentsRepository studentsRepository){
        this.studentsRepository = studentsRepository;
    }

    @Override
    public Student save(Student student) {
        return studentsRepository.save(student);
    }


    @Override
    public Student findById(Long id) {
        return studentsRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {

    }
}
