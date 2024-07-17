package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class SectionControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GradeRepository gradeRepository;
    @Test
    public void addSection() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new section.
        // the primary key, secNo, is set to 0. it will be
        // set by the database when the section is inserted.
        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst499",
                "",
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert section to String data and set as request content
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                .andReturn()
                .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        SectionDTO result = fromJsonString(response.getContentAsString(), SectionDTO.class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result.secNo());
        // check other fields of the DTO for expected values
        assertEquals("cst499", result.courseId());

        // check the database
        Section s = sectionRepository.findById(result.secNo()).orElse(null);
        assertNotNull(s);
        assertEquals("cst499", s.getCourse().getCourseId());

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/sections/"+result.secNo()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        s = sectionRepository.findById(result.secNo()).orElse(null);
        assertNull(s);  // section should not be found after delete
    }

    @Test
    public void addSectionFailsBadCourse() throws Exception {

        MockHttpServletResponse response;

        // course id cst599 does not exist.
        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst599",
                "",
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                .andReturn()
                .getResponse();

        // response should be 400, BAD_REQUEST
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("course not found cst599", message);

    }

    @Test
    public void studentEnrollsIntoSection() throws Exception {

        MockHttpServletResponse response;

        // Enrollment data
        int studentId = 5;  // existing student
        int sectionNo = 11;  // existing section

        // Ensure the section exists in the database
        Section section = sectionRepository.findById(sectionNo).orElse(null);
        assertNotNull(section);

        // Ensure the current date is within the add period for the section
        java.util.Date now = new Date();
        if (now.before(section.getTerm().getAddDate()) || now.after(section.getTerm().getAddDeadline())) {
            throw new IllegalStateException("Current date is not within the add period for the section.");
        }

        // Issue the POST request to enroll the student
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + sectionNo)
                                .param("studentId", String.valueOf(studentId))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // Check the response content
        EnrollmentDTO enrollment = fromJsonString(response.getContentAsString(), EnrollmentDTO.class);

        // Ensure the enrollment ID is returned and is not zero
        assertNotEquals(0, enrollment.enrollmentId());

        // Ensure the section and student details match
        assertEquals(sectionNo, enrollment.sectionNo());
        assertEquals(studentId, enrollment.studentId());

        // Check the database for the enrollment
        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        assertNotNull(e);
        assertEquals(studentId, e.getStudent().getId());
        assertEquals(sectionNo, e.getSection().getSectionNo());
    }
    @Test
    public void studentFailsToEnrollInSectionAlreadyEnrolled() throws Exception {
        MockHttpServletResponse response;

        // Enrollment data
        int studentId = 5;  // existing student
        int sectionNo = 11;  // existing section

        // Ensure the section exists in the database
        Section section = sectionRepository.findById(sectionNo).orElse(null);
        assertNotNull(section);

        // Ensure the student exists in the database
        User student = userRepository.findById(studentId).orElse(null);
        assertNotNull(student);

        // Clear any existing enrollments for this student and section
        Enrollment existingEnrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);

        if (existingEnrollment != null) {
            enrollmentRepository.delete(existingEnrollment);
        }

        // Ensure the student is already enrolled in the section
        existingEnrollment = new Enrollment();
        existingEnrollment.setStudent(student);
        existingEnrollment.setSection(section);
        enrollmentRepository.save(existingEnrollment);

        // Issue the POST request to enroll the student again
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + sectionNo)
                                .param("studentId", String.valueOf(studentId))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Check the response code for 400 meaning Bad Request
        assertEquals(404, response.getStatus());

        // Check the error message
        String errorMessage = response.getErrorMessage();
        assertEquals("student has already enrolled", errorMessage);
    }




    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
