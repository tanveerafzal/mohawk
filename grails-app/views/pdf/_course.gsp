<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.Course" %>

%{--<g:each in="${academicSession.all_Course}" var="course">--}%
<g:set var="course" value="${course as Course}"/>
%{--<g:set var="courseCreditLevel" value="${course.creditLevel}"/>--}%
%{--<g:set var="courseCreditUnits" value="${course.creditUnits}"/>--}%
%{--    <g:set var="ahrs_total"--}%
%{--           value="${ahrs_total + Double.parseDouble(course?.CourseCreditValue?.toString() ?: "0")}"/>--}%
%{--    <g:set var="ehrs_total"--}%
%{--           value="${ehrs_total + Double.parseDouble(course?.CourseCreditEarned?.toString() ?: "0")}"/>--}%
%{--    <g:set var="qpts_total"--}%
%{--           value="${qpts_total + Double.parseDouble(course.all_CourseQualityPointsEarned?.size() > 0 ? course.CourseQualityPointsEarned?.toString() : "0")}"/>--}%

<tr>
    <td>${course.courseAbbreviation} ${course.number}</td>
    <td>
        <div>${course.title}</div>
        <g:each in="${course.noteMessages}" var="noteMessage">
            <div>${noteMessage}</div>
        </g:each>
        <g:if test="${course.beginDate}">
            <div><g:formatDate formatName="date.format" date="${course.beginDate}"/></div>
        </g:if>
        <g:if test="${course.endDate}">
            <div><g:formatDate formatName="date.format" date="${course.endDate}"/></div>
        </g:if>
    %{--        ${course.all_CourseBeginDate?.size() > 0 ? "<br/>${g.message(code: 'label.course_begin_date')}: " + course.CourseBeginDate : ""}--}%
    %{--        ${course.all_CourseEndDate?.size() > 0 ? "<br/>${g.message(code: 'label.course_end_date')}: " + course.CourseEndDate : ""}--}%
    %{--        ${course.all_CourseRepeatCode?.size() > 0 ? "<br/>${g.message(code: 'label.course_repeat_code')}: " + course.CourseRepeatCode : ""}--}%
    </td>
    <td>${course.creditLevelCode}</td>
    <td>${course.gradeScale}</td>
    <td>${course.grade}</td>
    <td>${course.creditValue}</td>
    <td>${course.creditEarned}</td>
    <td>${course.qualityPointsEarned}</td>
</tr>
%{--</g:each>--}%
