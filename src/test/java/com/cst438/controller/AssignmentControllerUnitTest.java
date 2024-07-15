package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.CourseDTO;
import net.bytebuddy.asm.Advice;
import org.aspectj.lang.annotation.Before;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Date;
import java.time.LocalDate;

import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
@AutoConfigureMockMvc
@SpringBootTest
public class AssignmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    AssignmentRepository assignmentRepository;

    static final LocalDate today = LocalDate.now();

    Term term;
    Course course;
    Section section;
    @Autowired
    TermRepository termRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    SectionRepository sectionRepository;


    // Create current course and section data to avoid date errors
    @BeforeEach
    public void setupData() throws Exception {
        term = termRepository.findByYearAndSemester(2024,"Fall");
        course = courseRepository.findById("cst438").get();
        section = sectionRepository.findBySectionNo(11);
    }


    @Test
    public void itShouldAddSuccessfulAssignment() throws Exception {

        String dueDate = "2024-09-01";

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                123,
                "Test Homework 1",
                dueDate,
                course.getCourseId(),
                section.getSecId(),
                section.getSectionNo()
        );

        MockHttpServletResponse assignmentResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignmentDTO)))
                .andReturn()
                .getResponse();

        assertEquals(200, assignmentResponse.getStatus());
        AssignmentDTO result = fromJsonString(assignmentResponse.getContentAsString(), AssignmentDTO.class);
        // Primary key should not have a non-zero value
        assertNotEquals(0,result.id(), "ID should not be zero");
        // Check database values
        Assignment a = assignmentRepository.findById(result.id()).orElse(null);
        assertNotNull(a);
        assertEquals("Test Homework 1", a.getTitle(), "Title should match");
        assertEquals(dueDate.toString(), a.getDueDate().toString(), "Due date should match");
        assertEquals(section.getSecId(), a.getSection().getSecId(), "Section should match");
        assertEquals(section.getSectionNo(), a.getSection().getSectionNo(), "Section Numbers should match");

        assignmentResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignments/"+result.id()))
                .andReturn()
                .getResponse();

        assertEquals(200, assignmentResponse.getStatus());

        //Check that assignment was deleted
        a = assignmentRepository.findById(result.id()).orElse(null);
        assertNull(a);
    }

    @Test
    public void itShouldNotInsertInvalidDueDate() throws Exception {
        String dueDate = "2025-09-01";

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                123,
                "Test Homework 1",
                dueDate,
                course.getCourseId(),
                section.getSecId(),
                section.getSectionNo()
        );

        MockHttpServletResponse assignmentResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignmentDTO)))
                .andReturn()
                .getResponse();
        assertEquals(400, assignmentResponse.getStatus(), "Status should be error");

        String message = assignmentResponse.getErrorMessage();
        assertEquals(message, "Due date is outside the course dates", "Error message should match");
    }

    @Test
    public void itShouldNotInsertInvalidSection() throws Exception {
        String dueDate = "2024-09-01";

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                123,
                "Test Homework 1",
                dueDate,
                course.getCourseId(),
                section.getSecId(),
                0
        );

        MockHttpServletResponse assignmentResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignmentDTO)))
                .andReturn()
                .getResponse();
        assertEquals(400, assignmentResponse.getStatus(), "Status should be error");

        String message = assignmentResponse.getErrorMessage();
        assertEquals(message, "Section not found", "Message should match");
    }

    public void itShouldGradeOneAssignment() throws Exception {

    }

    public void itShouldGradeMoreThanOneAssignment() throws Exception {

    }

    public void itShouldNotGradeInvalidAssignmentId() throws Exception {

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
