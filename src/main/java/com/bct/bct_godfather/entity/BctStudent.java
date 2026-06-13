package com.bct.bct_godfather.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BctStudent {
    
    @Id
    private String id;

    private String name;
    private LocalDateTime dob;
    private String address;
    private String contact;

}
