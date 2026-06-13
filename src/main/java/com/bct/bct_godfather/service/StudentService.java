package com.bct.bct_godfather.service;

import org.springframework.stereotype.Service;

import com.bct.bct_godfather.entity.BctStudent;
import com.bct.bct_godfather.repo.StudentRepo;

@Service
public class StudentService {
    
    private final StudentRepo studentRepo;

    public StudentService(StudentRepo studentRepo){
        this.studentRepo = studentRepo;
    }

    public BctStudent getStudentDetailsById(String id){
        BctStudent student = studentRepo.findById(id).orElse(null);
        return student;
    }

}
