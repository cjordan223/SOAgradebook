package com.cst438.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="enrollment_id")
    int enrollmentId;

    @Column(name="grade")
	private String grade;

    @ManyToOne
    @JoinColumn(name="studentId", referencedColumnName = "id")
    User student;

    @ManyToOne
    @JoinColumn(name = "sectionId", referencedColumnName = "section_no")
    Section section;

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }


    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }


    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }


    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}
