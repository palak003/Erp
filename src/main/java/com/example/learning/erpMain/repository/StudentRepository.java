package com.example.learning.erpMain.repository;

import com.example.learning.erpMain.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student,Integer> {

    boolean existsByStudentNumber(String studentNumber);
}
