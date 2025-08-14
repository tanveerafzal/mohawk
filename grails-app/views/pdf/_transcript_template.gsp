<%@ page import="ca.mohawkcollege.ocastranscript.ssb.OcasTranscript" %>
<!DOCTYPE html>
<html>
<head>
    <title>Incoming transcripts</title>
    <g:render template="/pdf/transcript_css"/>
</head>

<body>

%{-- Explicit type for the variable we get from the model --}%
<g:set var="ocasTranscripts" value="${transcripts as List<OcasTranscript>}"/>
<g:set var="dateString" value="${g.formatDate(formatName: 'date.format', date: digestDate as Date)}"/>

%{-- Document header --}%
<div class="header-page">
    <h1>
        <g:if test="${ocasTranscripts?.size() > 0}">
            <g:message code="label.incoming_transcripts" args="${[dateString]}"/>
        </g:if>
        <g:else>
            <g:message code="label.no_transcripts_for" args="${[dateString]}"/>
        </g:else>
    </h1>
</div>

<g:render template="/pdf/single_transcript" collection="${ocasTranscripts}" var="ocasTranscript"/>

<div class="page-footer">${g.message(code: 'label.end_of_transcripts')}</div>
</body>
</html>