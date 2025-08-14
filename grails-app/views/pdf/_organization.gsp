<%@ page import="ca.mohawkcollege.ocastranscript.xml.pesc.Organization" %>
<g:set var="organization" value="${organization as Organization}"/>

<div class="organization info-block">
    <div>
        ${organization.name}
        <g:message code="label.institution.identifier" args="${[organization.identifier]}"/>
    </div>

    <div><g:join in="${organization.addressLines}" delimiter="${raw("<br/>")}"/></div>
    <g:each in="${organization.contactNoteMessages}" var="message"><div>${message}</div></g:each>
    <g:each in="${organization.noteMessages}" var="message"><div>${message}</div></g:each>
    <g:if test="${organization.formattedPhone}">
        <div><g:message code="label.tel"/> ${organization.formattedPhone}</div>
    </g:if>
    <g:if test="${organization.formattedFax}">
        <div><g:message code="label.fax"/> ${organization.formattedFax}</div>
    </g:if>
    <g:if test="${organization.email}">
        <div><g:message code="label.email"/> ${organization.email}</div>
    </g:if>
</div>