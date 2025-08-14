-- ACAD_GRAD_LIST: view of graduates, with graduation and degree information

CREATE OR REPLACE FORCE VIEW GG_OCAS_SYNC.ACAD_GRAD_LIST
(
    PIDM,
    TERM_CODE,
    MAJR_CODE,
    MAJR_DESC,
    DEGREE_CODE,
    DEGREE_DESC,
    GRAD_DATE
)
AS
    SELECT SHRDGMR_PIDM,
           SHRDGMR_TERM_CODE_GRAD,
           SHRDGMR_MAJR_CODE_1,
           STVMAJR_DESC,
           SHRDGMR_DEGC_CODE,
           STVDEGC_DESC,
           TO_CHAR (SHRDGMR_GRAD_DATE, 'YYYY-MM-DD')
      FROM SATURN.SHRDGMR
           INNER JOIN SATURN.STVDEGC ON SHRDGMR_DEGC_CODE = STVDEGC_CODE
           INNER JOIN SATURN.STVMAJR ON STVMAJR_CODE = SHRDGMR_MAJR_CODE_1
     WHERE SHRDGMR_DEGS_CODE = 'AW' AND SHRDGMR_DEGC_CODE != '00';

--	SHRDGMR	S-Student, H-Grades/Academic History,	R-Rule Table Repeating Table/Process	DGMR
-- 	STVDEGC	S-Student, T-Validation form/table, 	V-Validation Form/Table View			DEGC
