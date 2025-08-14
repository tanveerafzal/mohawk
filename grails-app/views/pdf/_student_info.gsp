<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.Student" %>
<g:set var="student" value="${student as Student}"/>

<div class="student info-block">
    <div>${student.fullyQualifiedName}</div>

    <div>
        <g:message code="label.local_student_id"/>
        ${student.schoolAssignedPersonId}
    </div>

    <g:if test="${student.ocasApplicationNumber}">
        <div>
            <g:message code="label.student.ocasApplicationNumber"/>
            ${student.ocasApplicationNumber}
        </div>
    </g:if>

    <g:if test="${student.ontarioEducationNumber}">
        <div>
            <g:message code="label.state_level_reference_number"/>
            ${student.ontarioEducationNumber}
        </div>
    </g:if>

    <g:if test="${student.dateOfBirth}">
        <div>
            <g:message code="label.student.dateOfBirth"/>
            <g:formatDate formatName="date.format" date="${student.dateOfBirth}"/>
        </div>
    </g:if>
</div>