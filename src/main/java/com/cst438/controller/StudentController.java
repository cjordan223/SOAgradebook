package com.cst438.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    // student gets transcript showing list of all enrollments
    // studentId will be temporary until Login security is implemented
    //example URL  /transcript?studentId=19803
    @GetMapping("/transcripts")
    public List<EnrollmentDTO> getTranscript(@RequestParam("studentId") int studentId) {

        User user = userRepository.findById(studentId).orElse(null);
        if (user==null) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "user id not found");
        }
        else if (!user.getType().equals("STUDENT")) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "user is not a student");
        }

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId);

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
            e.getSection().getTerm().getSemester()));
        }
        return dto_list;

        // list course_id, sec_id, title, credit, grade in chronological order
        // user must be a student
        // hint: use enrollment repository method findEnrollmentByStudentIdOrderByTermId
    }

    // student gets a list of their enrollments for the given year, semester
    // user must be student
    // studentId will be temporary until Login security is implemented
    @GetMapping("/enrollments")
    public List<EnrollmentDTO> getSchedule(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            @RequestParam("studentId") int studentId) {
       
        User user = userRepository.findById(studentId).orElse(null);
        if (user==null) {
                throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "user id not found");
        }
        else if (!user.getType().equals("STUDENT")) {
                throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "user is not a student");
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(studentId, year, semester);
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
            e.getSection().getTerm().getSemester()));
        }
        return dto_list;

        //  hint: use enrollment repository method findByYearAndSemesterOrderByCourseId
    }


    // student adds enrollment into a section
    // user must be student
    // return EnrollmentDTO with enrollmentId generated by database
    @PostMapping("/enrollments/sections/{sectionNo}")
    public EnrollmentDTO addCourse(
		    @PathVariable int sectionNo,
            @RequestParam("studentId") int studentId ) {

        User user = userRepository.findById(studentId).orElse(null);
        if (user==null) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "user id not found");
        }
        else if (!user.getType().equals("STUDENT")) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "user is not a student");
        }
        Section section = sectionRepository.findById(sectionNo).orElse(null);
        if (section==null) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "section is not found");
        }
        List<Enrollment> en = enrollmentRepository.findByStudentIdAndSectionNo(studentId, sectionNo);
        if (en.size() > 0) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "student has already enrolled");
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currDate = new Date();
        String currDateStr = dateFormat.format(currDate);
        Date enrollDeadDate = section.getTerm().getAddDeadline();
        String enrollDDateStr = enrollDeadDate.toString();
        Integer currYear = Integer.valueOf(currDateStr.substring(0, 4));
        Integer currMonth = Integer.valueOf(currDateStr.substring(5, 7));
        Integer currDay = Integer.valueOf(currDateStr.substring(8, 10));
        Integer dueYear = Integer.valueOf(enrollDDateStr.substring(0, 4));
        Integer dueMonth = Integer.valueOf(enrollDDateStr.substring(5, 7));
        Integer dueDay = Integer.valueOf(enrollDDateStr.substring(8, 10));
        boolean dateValid = true;
        System.out.println(dueYear+" "+dueMonth+" "+dueDay);
        if (currYear > dueYear) {
            dateValid = false;
        }
        else if ((currYear-dueYear == 0) && (currMonth > dueMonth)) {
            dateValid = false;
        }
        else if ((currYear-dueYear == 0) && (currMonth-dueMonth == 0) && (currDay > dueDay)) {
            dateValid = false;
        }
        if (!dateValid) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "the enrollment date is not valid");
        }

        Enrollment e = new Enrollment();
        e.setStudent(user);
        e.setSection(section);
        e.setGrade("");
        enrollmentRepository.save(e);

        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(
            e.getEnrollmentId(),
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
            );
        return enrollmentDTO;

        // check that the Section entity with primary key sectionNo exists
        // check that today is between addDate and addDeadline for the section
        // check that student is not already enrolled into this section
        // create a new enrollment entity and save.  The enrollment grade will
        // be NULL until instructor enters final grades for the course.
    }

    // student drops a course
    // user must be student
    @DeleteMapping("/enrollments/{enrollmentId}")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
        if (enrollment == null) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "enrollment was not found");
        }
        Date dropDeadDate = enrollment.getSection().getTerm().getDropDeadline();
        Date currDate = new Date();
        if (dropDeadDate.compareTo(currDate) < 0) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "drop dead line passed");
        }
        enrollmentRepository.delete(enrollment);

       // check that today is not after the dropDeadline for section
   }
}