package com.cst438.controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.cst438.domain.*;
import com.cst438.dto.SectionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.GradeDTO;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {


    @Autowired
    AssignmentRepository assignmentRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(@PathVariable("secNo") int secNo) {
        List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);
        if (assignments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found or no assignments for section");
        }
        List<AssignmentDTO> assignmentDTOs = new ArrayList<>();
        for (Assignment a : assignments) {
            AssignmentDTO dto = new AssignmentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDate().toString(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    a.getSection().getSectionNo()
            );
            assignmentDTOs.add(dto);
        }
        return assignmentDTOs;
    }


    // add assignment
    // user must be instructor of the section
    // return AssignmentDTO with assignmentID generated by database
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(@RequestBody AssignmentDTO dto) {
        // check if section exists
        Section section = sectionRepository.findById(dto.secNo()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section not found"));

        // check if due date is within course start and end dates
        Date dueDate = Date.valueOf(dto.dueDate());
        if (dueDate.before(section.getTerm().getStartDate()) || dueDate.after(section.getTerm().getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date is outside the course dates");
        }

        // create new assignment
        Assignment assignment = new Assignment();
        assignment.setTitle(dto.title());
        assignment.setDueDate(dueDate);
        assignment.setSection(section);

        // save assignment
        assignment = assignmentRepository.save(assignment);

        return new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getTitle(),
                assignment.getDueDate().toString(),
                assignment.getSection().getCourse().getCourseId(),
                assignment.getSection().getSecId(),
                assignment.getSection().getSectionNo()
        );
    }
    // update assignment for a section.  Only title and dueDate may be changed.
    // user must be instructor of the section
    // return updated AssignmentDTO
    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO dto) {
        // find assignment by ID
        Assignment assignment = assignmentRepository.findById(dto.id()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment not found"));

        // check if the due date is within the course start and end dates
        Date dueDate = Date.valueOf(dto.dueDate());
        if (dueDate.before(assignment.getSection().getTerm().getStartDate()) || dueDate.after(assignment.getSection().getTerm().getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date is outside the course dates");
        }

        // update assignment title and due date
        assignment.setTitle(dto.title());
        assignment.setDueDate(dueDate);

        // save updated assignment
        assignment = assignmentRepository.save(assignment);

        return new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getTitle(),
                assignment.getDueDate().toString(),
                assignment.getSection().getCourse().getCourseId(),
                assignment.getSection().getSecId(),
                assignment.getSection().getSectionNo()
        );
    }

    // delete assignment for a section
    // logged in user must be instructor of the section
    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        assignmentRepository.delete(assignment);
    }

    // instructor gets grades for assignment ordered by student name
    // user must be instructor for the section
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment not found"));

        List<GradeDTO> gradeDTOs = new ArrayList<>();
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(assignment.getSection().getSectionNo());
        for (Enrollment e : enrollments) {
            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(e.getEnrollmentId(), assignment.getAssignmentId());
            if (grade == null) {
                grade = new Grade();
                grade.setAssignment(assignment);
                grade.setEnrollment(e);

                grade.setScore(0);
                //grade.setScore(null);
                grade = gradeRepository.save(grade);
            }
            gradeDTOs.add(new GradeDTO(grade.getGradeId(), grade.getEnrollment().getStudent().getName(),
                    grade.getEnrollment().getStudent().getEmail(), grade.getAssignment().getTitle(),
                    grade.getAssignment().getSection().getCourse().getCourseId(), grade.getAssignment().getSection().getSecId(), grade.getScore()));
        }

        return gradeDTOs;
    }

    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {
        for (GradeDTO dto : dlist) {
            Grade grade = gradeRepository.findById(dto.gradeId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grade not found " + dto.gradeId()));
            grade.setScore(dto.score());
            gradeRepository.save(grade);
        }
    }

    @GetMapping("/assignments")
    public List<AssignmentStudentDTO> getStudentAssignments(@RequestParam("studentId") int studentId,
                                                            @RequestParam("year") int year,
                                                            @RequestParam("semester") String semester) {
        List<Assignment> assignments = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(studentId, year, semester);
        if (assignments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No assignments found for the given criteria");
        }
        List<AssignmentStudentDTO> assignmentStudentDTOs = new ArrayList<>();
        for (Assignment a : assignments) {
            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(a.getSection().getEnrollments().stream()
                    .filter(e -> e.getStudent().getId() == studentId)
                    .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found for student")).getEnrollmentId(), a.getAssignmentId());
            Integer score = (grade != null) ? grade.getScore() : null;
            assignmentStudentDTOs.add(new AssignmentStudentDTO(a.getAssignmentId(), a.getTitle(), a.getDueDate(),
                    a.getSection().getCourse().getCourseId(), a.getSection().getSecId(), score));
        }
        return assignmentStudentDTOs;
    }

    // get Sections for an instructor
    @GetMapping("/sections")
    public List<SectionDTO> getSectionsForInstructor(
            @RequestParam("email") String instructorEmail,
            @RequestParam("year") int year ,
            @RequestParam("semester") String semester )  {


        List<Section> sections = sectionRepository.findByInstructorEmailAndYearAndSemester(instructorEmail, year, semester);

        List<SectionDTO> dto_list = new ArrayList<>();
        for (Section s : sections) {
            User instructor = null;
            if (s.getInstructorEmail()!=null) {
                instructor = userRepository.findByEmail(s.getInstructorEmail());
            }
            dto_list.add(new SectionDTO(
                    s.getSectionNo(),
                    s.getTerm().getYear(),
                    s.getTerm().getSemester(),
                    s.getCourse().getCourseId(),
                    s.getCourse().getTitle(),
                    s.getSecId(),
                    s.getBuilding(),
                    s.getRoom(),
                    s.getTimes(),
                    (instructor!=null) ? instructor.getName() : "",
                    (instructor!=null) ? instructor.getEmail() : ""
            ));
        }
        return dto_list;
    }
}
