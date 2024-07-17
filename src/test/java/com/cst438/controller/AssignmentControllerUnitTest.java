package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.cst438.domain.*;
import com.cst438.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
@AutoConfigureMockMvc
@SpringBootTest
public class AssignmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

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
        section = sectionRepository.findByLikeCourseIdAndYearAndSemester("cst438", 2024, "Fall").get(0);
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
        String dueDate = "3024-09-01";

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

    @Test
    public void itShouldGradeOneAssignment() throws Exception {
        Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(4, 4);

        Integer score = 90;

        List<GradeDTO> gradeDTOList = new ArrayList<>();

        GradeDTO gradeDTO = new GradeDTO(
                grade.getGradeId(),
                grade.getEnrollment().getStudent().getName(),
                grade.getEnrollment().getStudent().getEmail(),
                grade.getAssignment().getTitle(),
                grade.getAssignment().getSection().getCourse().getCourseId(),
                grade.getAssignment().getSection().getSecId(),
                score
        );

        gradeDTOList.add(gradeDTO);

        MockHttpServletResponse gradeResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(gradeDTOList)))
                .andReturn()
                .getResponse();

        assertEquals(200, gradeResponse.getStatus(), "Status should be OK");

        String message = gradeResponse.getErrorMessage();
        assertEquals(message, null, "Message should match");

        MockHttpServletResponse getResult = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/" + grade.getAssignment().getAssignmentId() + "/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertEquals(200, getResult.getStatus(), "Status should be OK");

        message = getResult.getErrorMessage();
        assertEquals(message, null, "Message should be null");

        String jsonResponse = getResult.getContentAsString();
        // Convert JSON to List<GradeDTO>
        List<GradeDTO> resultList = new ObjectMapper().readValue(jsonResponse, new TypeReference<List<GradeDTO>>() {});

        // primary key should have a non-zero value from the database
        assertNotEquals(0, resultList.get(0).gradeId());

        // Check if score was updated
        assertEquals(score, resultList.get(0).score(), "Score should be 90");

        // Revert score back to null
        grade.setScore(null);
        gradeRepository.save(grade);

        Grade g = gradeRepository.findById(grade.getGradeId()).orElse(null);

        // Check grade was set back to null
        assertNull(g.getScore());
    }

    @Test
    public void gradeAssignmentInvalidId() throws Exception {

        MockHttpServletResponse response;

        // invalid assignment ID
        String gradeData = "[{\"gradeId\": 99999, \"score\": 90}]";

        // PUT request to update grades
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gradeData))
                .andReturn()
                .getResponse();


        assertEquals(400, response.getStatus());

        // checker error msg
        String errorMessage = response.getErrorMessage();
        assertEquals("Grade not found 99999", errorMessage);
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
