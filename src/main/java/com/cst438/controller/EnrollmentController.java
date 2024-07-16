package com.cst438.controller;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.dto.EnrollmentDTO;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {
    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    // instructor downloads student enrollments for a section, ordered by student name
    // user must be instructor for the section
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(
            @PathVariable("sectionNo") int sectionNo ) {

        Section theSec = sectionRepository.findById(sectionNo).orElse(null);
        if (theSec == null) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "Section not found "+sectionNo);
        }
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);
        if (enrollments.size() < 1) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "Nobody has enrolled in section "+sectionNo);
        } else {
            List<EnrollmentDTO> dto_list = new ArrayList<>();
            for (Enrollment e : enrollments) {
                dto_list.add(new EnrollmentDTO(e.getEnrollmentId(),
                        e.getGrade(),
                        e.getStudent().getId(),
                        e.getStudent().getName(),
                        e.getStudent().getEmail(),
                        e.getSection().getCourse().getCourseId(),
                        e.getSection().getCourse().getTitle(),
                        e.getSection().getSecId(),
                        e.getSection().getSectionNo(),
                        e.getSection().getBuilding(),
                        e.getSection().getRoom(),
                        e.getSection().getTimes(),
                        e.getSection().getCourse().getCredits(),
                        e.getSection().getTerm().getYear(),
                        e.getSection().getTerm().getSemester()
                ));
            }
            return dto_list;
        }
    }

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist) {
        if (dlist.size() < 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Please provide the enrollments");
        } else {
            for (EnrollmentDTO enrollment : dlist) {
                Enrollment e = enrollmentRepository.findById(enrollment.enrollmentId()).orElse(null);
                e.setGrade(enrollment.grade());
                enrollmentRepository.save(e);
            }
        }
    }
}
