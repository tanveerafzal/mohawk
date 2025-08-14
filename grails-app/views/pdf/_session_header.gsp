<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.AcademicSession" %>
<g:set var="academicSession" value="${academicSession as AcademicSession}"/>

<div class="session-header">
    <p>
        <g:message code="label.session.name" args="${[academicSession.name]}"/>
        <g:if test="${academicSession.type}">
            <g:message code="label.session.type" args="${[academicSession.type]}"/>
        </g:if>
    </p>

    <div>
        <g:if test="${academicSession.begin}">
            <div class="inline-block">
                <g:message code="label.from"/>:
                <g:formatDate formatName="date.format" date="${academicSession.begin}"/>
            </div>
        </g:if>
        <g:if test="${academicSession.end}">
            <div class="inline-block">
                <g:message code="label.to"/>:
                <g:formatDate formatName="date.format" date="${academicSession.end}"/>
            </div>
        </g:if>
    </div>

    <div>
        <g:if test="${academicSession.studentLevel}">
            <div class="inline-block">
                <g:message code="label.student_level"/>:
                ${academicSession.studentLevel}
            </div>
        </g:if>
        <g:if test="${academicSession.programName}">
            <div class="inline-block">
                <g:message code="label.program.name"/>:
                ${academicSession.programName}
            </div>
        </g:if>
    </div>
</div>
