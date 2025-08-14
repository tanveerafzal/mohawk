<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.CollegeTranscript" %>
<g:set var="transcript" value="${ocasTranscript.collegeTranscript as CollegeTranscript}"/>

<div class="transcript">
    %{--  Transcript header  --}%
    <g:render template="/pdf/transcript_header" bean="${transcript}" var="transcript"/>

    %{--  Transcript header  --}%
    <g:render template="/pdf/academic_session" collection="${transcript.student.academicSessions}"
              var="academicSession"/>

    %{--  Transcript header  --}%
    <g:if test="${transcript.student.academicSessions}">
        <g:render template="/pdf/transcript_footer" bean="${transcript}" var="transcript"/>
    </g:if>
    <div>${g.message(code: 'label.end_of_transcript')}</div>
    <div><hr color="red" height="20"/></div>
</div>