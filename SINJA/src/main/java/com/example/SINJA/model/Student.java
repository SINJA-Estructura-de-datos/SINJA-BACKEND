package com.example.SINJA.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Student extends Person{
    private String degree;
    private CampusUdea place;
    private Integer scoreAdmision;

    public Student(Long id, String name, String lastName, String bornPlace,
                   String degree, CampusUdea place, Integer scoreAdmision) {
        super(id, name, lastName, bornPlace);
        this.degree = degree;
        this.place = place;
        this.scoreAdmision = scoreAdmision;
    }

}
