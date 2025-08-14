<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.AcademicSession" %>
<g:set var="academicSession" value="${academicSession as AcademicSession}"/>

<tfoot>
<tr>
    <th colspan="2">${g.message(code: 'label.session_statistics')}</th>
    <th>${g.message(code: 'label.gpa.creditUnit')}</th>
    <th>${g.message(code: 'label.gpa.gradePointAverage')}</th>
    <th>${g.message(code: 'label.gpa.creditHours')}</th>
    <th>${g.message(code: 'label.gpa.creditAttempted')}</th>
    <th>${g.message(code: 'label.gpa.creditEarned')}</th>
    <th>${g.message(code: 'label.gpa.qualityPointsEarned')}</th>
</tr>
<tr>
    <td colspan="2">
        <g:if test="${academicSession.creditLevel}">
            <g:message code="label.session.creditLevel" args="${[academicSession.creditLevel]}"/>
        </g:if>
        <g:if test="${academicSession.classRank}">
            <g:message code="label.session.classRank" args="${[academicSession.classRank]}"/>
        </g:if>
        <g:if test="${academicSession.classSize}">
            <g:message code="label.session.classSize" args="${[academicSession.classSize]}"/>
        </g:if>
    </td>
    <td>${academicSession.creditUnit}</td>
    <td>${academicSession.gradePointAverage}</td>
    <td>${academicSession.creditHoursForGPA}</td>
    <td>${academicSession.creditHoursAttempted}</td>
    <td>${academicSession.creditHoursEarned}</td>
    <td>${academicSession.qualityPointsEarned}</td>
</tr>
</tfoot>