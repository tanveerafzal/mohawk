<style>
/** {*/
    /*box-sizing: border-box;*/
/*}*/
/* Top-level digest elements */
body {
    font-family: Calibri, serif;
}

@page {
    size: letter portrait;
    @top-left {
        content: element(student-name);
    }
    @top-center {
        content: element(student-number);
    }
    @top-right {
        content: element(source-name);
    }
    @bottom-center {
        content: element(footer);
    }
}

h1, h2 {
    text-align: center;
}

.page-footer {
    position: running(footer);
    text-align: center;
}

/* Single student transcript within digest */
.transcript {
    page-break-before: always;
}

.transcript-header-block .info-block {
    display: inline-block;
}

.transcript-header-block .student.info-block {
    width: 30%;
}

.transcript-header-block .document.info-block {
    width: 30%;
}

.transcript-header-block .organization.info-block {
    width: auto;
}

.transcript-footer {
    border: 1px solid black;
    padding: 0.5em 1em;
}

.running.student-name {
    position: running(student-name);
    text-align: left;
}

.running.student-number {
    position: running(student-number);
    text-align: center;
}

.running.source-name {
    position: running(source-name);
    text-align: right;
}

.running.page-number {
    position: running(page-number);
    content: "Page " counter(page);
    counter-increment: page;
    text-align: center;
}

/* Single session within transcript */
.academic-session {
    page-break-inside: avoid;
    border: 1px solid black;
    padding: 0.5em 1em;
}

/* Table of courses in a session */
.academic-session .courses {
    width: 100%;
}

th, td {
    text-align: left;
    padding: 2px;
}

.inline-block {
    display: inline-block;
}

</style>
