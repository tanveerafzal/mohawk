<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.AcademicSession" %>
<g:set var="academicSession" value="${academicSession as AcademicSession}"/>

<g:if test="${academicSession.gpaRange}">
    <div class="session-footer"><g:message code="label.gpa.range"
                                           args="${[academicSession.gpaRangeMinimum, academicSession.gpaRangeMaximum]}"/></div>
</g:if>

<g:if test="${academicSession.noteMessages}">
    <div class="session-footer">
        <g:each in="${academicSession.noteMessages}" var="noteMessage">
            <div>${noteMessage}</div>
        </g:each>
    </div>
</g:if>