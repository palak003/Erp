package com.example.learning.erpMain.repository;

import com.example.learning.erpMain.models.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher,Integer> {

    boolean existsByTeacherNumber(String teacherNumber);
}
