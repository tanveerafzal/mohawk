<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.AcademicSession" %>
<g:set var="academicSession" value="${academicSession as AcademicSession}"/>

%{-- Begin session block --}%
<div class="academic-session">

%{-- Overall session info --}%
    <g:render template="/pdf/session_header" model="${academicSession}" var="academicSession"/>

%{-- Course listings --}%
    <g:if test="${academicSession.courses}">
        <table class="courses">
            <thead>
            <tr>
                <th><g:message code="label.course.number"/></th>
                <th><g:message code="label.course.title"/></th>
                <th><g:message code="label.course.creditLevel"/></th>
                <th><g:message code="label.course.gradeScale"/></th>
                <th><g:message code="label.course.grade"/></th>
                <th><g:message code="label.gpa.creditAttempted"/></th>
                <th><g:message code="label.gpa.creditEarned"/></th>
                <th><g:message code="label.gpa.qualityPointsEarned"/></th>
            </tr>
            </thead>
            <tbody>
            <g:render template="/pdf/course" collection="${academicSession.courses}" var="course"/>
            </tbody>

            <g:render template="/pdf/session_summary" model="${academicSession}" var="academicSession"/>
        </table>
    </g:if>

%{-- Overall session info --}%
    <g:render template="/pdf/session_footer" model="${academicSession}" var="academicSession"/>

</div><!-- end academic-session -->
%{-- End session block --}%


