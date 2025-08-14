<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.TransmissionData" %>
<g:set var="transmissionData" value="${transmissionData as TransmissionData}"/>

<div class="document info-block">
    <div>
        <g:message code="label.transmission"/>
        ${transmissionData.transmissionType.toString()}
    </div>

    <div>
        <g:message code="label.transcript.dateCreated"/>
        <g:formatDate formatName="date.format" date="${transmissionData.createdDateTime}"/>
    </div>
</div>