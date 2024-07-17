package com.cst438.controller;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
public class EnrollmentControllerUnitTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    public void updateEnrollmentGrade() throws Exception {
        
        int sectionNum = 8;

        // Fetch enrollments for the given section number
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNum);

        assertNotEquals(0, enrollments.size(), "No enrollments found for sectionNum=" + sectionNum);

        // Update grade for each enrollment to "L"
        for (Enrollment enrollment : enrollments) {
            Enrollment en = enrollmentRepository.findById(enrollment.getEnrollmentId()).orElse(null);
            en.setGrade("L");
            enrollmentRepository.save(en);
        }

        // Convert Enrollment entities to DTOs
        List<EnrollmentDTO> dto_list = new ArrayList<>();
        for (Enrollment en : enrollments) {
            dto_list.add(new EnrollmentDTO(en.getEnrollmentId(),
                    "L", // Ensure grade is set to "L"
                    en.getStudent().getId(),
                    en.getStudent().getName(),
                    en.getStudent().getEmail(),
                    en.getSection().getCourse().getCourseId(),
                    en.getSection().getCourse().getTitle(),
                    en.getSection().getSecId(),
                    en.getSection().getSectionNo(),
                    en.getSection().getBuilding(),
                    en.getSection().getRoom(),
                    en.getSection().getTimes(),
                    en.getSection().getCourse().getCredits(),
                    en.getSection().getTerm().getYear(),
                    en.getSection().getTerm().getSemester()
            ));
        }

        // Perform PUT request to update enrollments
        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders
                        .put("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto_list)))
                .andReturn()
                .getResponse();

        // Assertions
        assertEquals(200, response.getStatus(), "Expected HTTP status 200");

        // Verify changes in database
        for (EnrollmentDTO enrollment : dto_list) {
            Enrollment en = enrollmentRepository.findById(enrollment.enrollmentId()).orElse(null);
            assertEquals("L", en.getGrade(), "Expected grade 'L' for enrollmentId=" + enrollment.enrollmentId());
        }
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJsonString(String str, TypeReference<T> valueTypeRef) {
        try {
            return new ObjectMapper().readValue(str, valueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}