<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.CollegeTranscript" %>
<g:set var="transcript" value="${transcript as CollegeTranscript}"/>
<g:set var="gpa" value="${transcript.gpa}"/>

<g:if test="${gpa}">
    <div class="transcript-footer">
        <table>
            <tr>
                <th>${g.message(code: 'label.overall_summary')}</th>
                <th>${g.message(code: 'label.gpa.creditUnit')}</th>
                <th>${g.message(code: 'label.gpa.gradePointAverage')}</th>
                <th>${g.message(code: 'label.gpa.creditHours')}</th>
                <th>${g.message(code: 'label.gpa.creditAttempted')}</th>
                <th>${g.message(code: 'label.gpa.creditEarned')}</th>
                <th>${g.message(code: 'label.gpa.qualityPointsEarned')}</th>
            </tr>
            <tr>
                <td>${transcript.creditLevel}</td>
                <td>${gpa.creditUnit}</td>
                <td>${gpa.gradePointAverage}</td>
                <td>${gpa.creditHoursForGpa}</td>
                <td>${gpa.creditHoursAttempted}</td>
                <td>${gpa.creditHoursEarned}</td>
                <td>${gpa.totalQualityPoints}</td>
            </tr>
        </table>
    </div>
</g:if>

<g:if test="${transcript.gpa.range}">
    <div class="transcript-footer">
        <g:message code="label.gpa.range" args="${[transcript.gpa.rangeMinimum, transcript.gpa.rangeMaximum]}"/>
    </div>
</g:if>

<g:if test="${transcript.noteMessages}">
    <div class="transcript-footer">
        <g:each in="${transcript.noteMessages}" var="noteMessage">
            <div>${noteMessage}</div>
        </g:each>
    </div>
</g:if>