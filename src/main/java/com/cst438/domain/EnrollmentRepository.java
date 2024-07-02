package com.cst438.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface EnrollmentRepository extends CrudRepository<Enrollment, Integer> {

    // TODO uncomment the following lines as needed

    @Query("select e from Enrollment e where e.section.sectionNo=:sectionNo order by e.student.name")
    List<Enrollment> findEnrollmentsBySectionNoOrderByStudentName(int sectionNo);

    @Query("select e from Enrollment e where e.student.id=:studentId order by e.section.term.termId")
    List<Enrollment> findEnrollmentsByStudentIdOrderByTermId(int studentId);

    @Query("select e from Enrollment e " +
    "where e.student.id=:studentId and e.section.term.year=:year and e.section.term.semester=:semester " +
    "order by e.section.course.courseId")
    List<Enrollment> findByYearAndSemesterOrderByCourseId(int studentId, int year, String semester);

    @Query("select e from Enrollment e where e.student.id=:studentId and e.section.sectionNo=:sectionNo")
    List<Enrollment> findByStudentIdAndSectionNo(int studentId, int sectionNo);

//    @Query("select e from Enrollment e where e.section.term.year=:year and e.section.term.semester=:semester and e.student.id=:studentId order by e.section.course.courseId")
//    List<Enrollment> findByYearAndSemesterOrderByCourseId(int year, String semester, int studentId);
//
//    @Query("select e from Enrollment e where e.section.sectionNo=:sectionNo and e.student.id=:studentId")
//    Enrollment findEnrollmentBySectionNoAndStudentId(int sectionNo, int studentId);
}
