package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
public class StudentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Test
    public void addCourse() throws Exception {

        MockHttpServletResponse response;

        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Fall",
                "cst499",
                "Capstone",
                2,
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

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        SectionDTO secResult = fromJsonString(response.getContentAsString(), SectionDTO.class);

        // primary key should be 20
        assertNotEquals(0, secResult.secNo());
        // check other fields of the DTO for expected values
        assertEquals("Fall", secResult.semester());
        assertEquals(2024, secResult.year());

        // check the database
        Section sec = sectionRepository.findById(secResult.secNo()).orElse(null);
        assertNotNull(sec);
        assertEquals("cst499", sec.getCourse().getCourseId());

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/"+secResult.secNo()+"?studentId=3")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        EnrollmentDTO result = fromJsonString(response.getContentAsString(), EnrollmentDTO.class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result.enrollmentId());
        // check other fields of the DTO for expected values
        assertEquals(secResult.secNo(), result.sectionNo());
        // check other fields of the DTO for expected values
        assertEquals(3, result.studentId());

        // check the database
        Enrollment n = enrollmentRepository.findById(result.enrollmentId()).orElse(null);
        assertNotNull(n);
        assertNotEquals(0, n.getEnrollmentId());
        assertEquals(secResult.secNo(), n.getSection().getSectionNo());
        assertEquals(3, n.getStudent().getId());

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/enrollments/"+result.enrollmentId()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        n = enrollmentRepository.findById(result.enrollmentId()).orElse(null);
        assertNull(n);  // section should not be found after delete

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/sections/"+secResult.secNo()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        sec = sectionRepository.findById(secResult.secNo()).orElse(null);
        assertNull(sec);  // section should not be found after delete
    }

    @Test
    public void addCourseFailsAlreadyEnrolled() throws Exception {

        MockHttpServletResponse response;

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/10?studentId=3")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // response should be 404, BAD_REQUEST
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("student has already enrolled", message);

    }

    @Test
    public void addCourseFailsBadSection() throws Exception {
        MockHttpServletResponse response;

        // issue the POST request to a non-existent section (e.g., section ID 12)
        response = mvc.perform(
                        MockMvcRequestBuilders

                                .post("/enrollments/sections/20?studentId=3")

                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // response should be 404, BAD_REQUEST
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("section is not found", message);
    }


    @Test
    public void addCourseFailsBadDate() throws Exception {

        MockHttpServletResponse response;

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/9?studentId=3")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // response should be 404, BAD_REQUEST
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("the enrollment date is not valid", message);

    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
