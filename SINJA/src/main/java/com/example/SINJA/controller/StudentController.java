package com.example.SINJA.controller;

import com.example.SINJA.model.Student;
import com.example.SINJA.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*") 
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    private final StudentService studentService;

    public StudentController(StudentService studentService){
        this.studentService = studentService;
    }

    @PostMapping("/save")
    public ResponseEntity<Student> save(@RequestBody Student student){
        try {
            log.info("Entrando al método save con los datos {}", student);
            Student savedStudent = studentService.save(student);
            return ResponseEntity.ok(savedStudent); 
        } catch (Exception e) {
            log.error("Error al guardar estudiante", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Student> searchById(@RequestParam Long id){
        try {
            Student student = studentService.findById(id);
            if (student != null) {
                return ResponseEntity.ok(student); 
            } else {
                return ResponseEntity.notFound().build(); // 
            }
        } catch (Exception e) {
            log.error("Error al buscar estudiante", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteById(@RequestParam Long id){
        try {
            boolean eliminado = studentService.deleteById(id);
            if (eliminado) {
                return ResponseEntity.noContent().build(); 
            } else {
                return ResponseEntity.notFound().build(); 
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); 
        }
    }
}
