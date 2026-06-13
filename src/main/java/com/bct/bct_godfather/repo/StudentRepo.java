package com.bct.bct_godfather.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bct.bct_godfather.entity.BctStudent;

@Repository
public interface StudentRepo extends JpaRepository<BctStudent,String>{

}
