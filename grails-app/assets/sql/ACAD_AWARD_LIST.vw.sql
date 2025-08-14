-- ACAD_AWARD_LIST: view of academic awards per student

CREATE OR REPLACE FORCE VIEW GG_OCAS_SYNC.ACAD_AWARD_LIST
(
    PIDM,
    TERM_CODE,
    AWARD,
    ACTIVITY_DATE
)
AS
    SELECT SHRDGIH_PIDM,
           shrdgmr_term_code_sturec,
           STVHONR_DESC,
           TO_CHAR (SHRDGMR_ACTIVITY_DATE, 'YYYY-MM-DD')
      FROM SATURN.SHRDGIH
           INNER JOIN SATURN.SHRDGMR
               ON SHRDGIH_PIDM = SHRDGMR_PIDM AND SHRDGIH_DGMR_SEQ_NO = SHRDGMR_SEQ_NO
           INNER JOIN SATURN.STVHONR ON SHRDGIH_HONR_CODE = STVHONR_CODE
     WHERE SHRDGIH_TRANSC_PRT_IND = 'Y';

--SHRDGIH	1-Student, H-Grades/Academic History, R-Rule Table Repeating Table/Process	DGIH
--STVHONR	1-Student, T-Validation form/table, R-V Validation Form/Table View		HONR
--SHRDGMR	1-Student, H-Grades/Academic History, R-Rule Table Repeating Table/Process	DGMR
--SWVLHON	
