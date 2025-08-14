<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.CollegeTranscript" %>
<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.Award" %>
<g:set var="transcript" value="${transcript as CollegeTranscript}"/>

%{-- Clear these values so they don't show up in the header of the first page of the transcript--}%

<span class="running student-name"><!-- this space intentionally left blank --></span>

<span class="running student-number"><!-- this space intentionally left blank --></span>

<span class="running source-name"><!-- this space intentionally left blank --></span>

<div class="transcript-header-block">

%{-- Student personal information --}%
    <g:render template="/pdf/student_info" bean="${transcript.student}" var="student"/>

%{-- Transcript document information --}%
    <g:render template="/pdf/transmission_info" bean="${transcript.transmissionData}" var="transmissionData"/>

%{-- Source Institution information --}%
    <g:render template="/pdf/organization" bean="${transcript.source}" var="organization"/>

    <g:if test="${transcript.noteMessages}">
        <div class="transcript-notes">
            <g:each in="${transcript.noteMessages}" var="note"><div>${note}</div></g:each>
        </div>
    </g:if>

%{-- Awards, if any --}%
    <g:set var="awardList" value="${transcript.student.getPDFAwards()}" />
    <g:if test="${!awardList.isEmpty()}">
        <g:each var="award" in="${awardList}">
            <g:each var="awardItem" in="${award}">
                <div class="award">
                    <p>${awardItem?.title}</p>
                    <g:if test="${awardItem?.date}">
                        <p>
                            <g:message code="label.award.date"/>:
                            <g:formatDate formatName="date.format" date="${awardItem?.date}"/>
                        </p>
                    </g:if>
                    <g:if test="${awardItem?.programType || awardItem?.programName}">
                        <p>${awardItem?.programType} ${awardItem?.programName}</p>
                    </g:if>
                    <g:each in="${awardItem?.noteMessages}" var="noteMessage">
                        <p>${noteMessage}</p>
                    </g:each>
                </div>
            </g:each>
        </g:each>

    </g:if>
</div>

%{-- Defining these after the main header means they will be defined for subsequent pages only --}%

<span class="running student-name">${transcript.student.fullyQualifiedName}</span>

%{--<span class="running student-number">${transcript.student.schoolAssignedPersonId}</span>--}%
<span class="running student-number"><g:message code="label.student.ocasApplicationNumber"/>: ${transcript.student.ocasApplicationNumber}</span>

<span class="running source-name">${transcript.source.name}</span>

%{--  TODO implement page numbering  --}%
<div class="running page-number"><g:message code="label.page"/> </div>