DROP TYPE XML_TRAN_STUDENT_LIST;
DROP TYPE XML_TRAN_STUDENT_TYPE;
DROP FUNCTION LF_XML_FIND_MATCHING;

CREATE TYPE XML_TRAN_STUDENT_TYPE AS OBJECT (
    PIDM                    NUMBER(8),
    ID                      VARCHAR2(9),
    FIRSTNAME               VARCHAR2(60),
    LASTNAME                VARCHAR2(60),
    MIDDLENAME              VARCHAR2(60),
    BIRTHDATE               VARCHAR2(10),
    GENDER                  VARCHAR2(1),
    SSN                     VARCHAR2(15),
    MATCH_CODE              VARCHAR2(2),
    PRIORITY                NUMBER(2) );
    
     
CREATE TYPE XML_TRAN_STUDENT_LIST AS TABLE OF XML_TRAN_STUDENT_TYPE;


CREATE OR REPLACE FUNCTION LF_XML_FIND_MATCHING ( 
    FIRST_NAME      IN VARCHAR2,
    LAST_NAME       IN VARCHAR2,
    ALT_FIRST_NAME  IN VARCHAR2,
    ALT_LAST_NAME   IN VARCHAR2,
    BIRTHDATE       IN VARCHAR2,
    SSN             IN VARCHAR2 )
RETURN XML_TRAN_STUDENT_LIST PIPELINED IS

    lv_return       XML_TRAN_STUDENT_TYPE := XML_TRAN_STUDENT_TYPE(NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

    lv_priority     NUMBER(2) := 1 ;
    lv_birthdate    DATE := NULL;
BEGIN

    IF( BIRTHDATE IS NOT NULL)
    THEN
        lv_birthdate := TO_DATE( BIRTHDATE, 'YYYY-MM-DD');
    END IF;
    
    
    -- Is there a sin number matching (good change this is the person)
    IF NVL(SSN,' ') != ' ' 
    THEN
        FOR SIN IN ( SELECT * FROM SPBPERS, SPRIDEN WHERE SPRIDEN_PIDM = SPBPERS_PIDM AND SPRIDEN_CHANGE_IND IS NULL AND SPBPERS_SSN = SSN )
        LOOP
            lv_return.PIDM := SIN.SPRIDEN_PIDM;
            lv_return.ID := SIN.SPRIDEN_ID;
            lv_return.FIRSTNAME := SIN.SPRIDEN_FIRST_NAME;
            lv_return.LASTNAME := SIN.SPRIDEN_LAST_NAME;
            lv_return.MIDDLENAME := SIN.SPRIDEN_MI;
            lv_return.BIRTHDATE := TO_CHAR(SIN.SPBPERS_BIRTH_DATE, 'YYYY-MM-DD');
            lv_return.GENDER := SIN.SPBPERS_SEX;
            lv_return.SSN := SIN.SPBPERS_SSN;
            lv_return.MATCH_CODE := 'S';
            lv_return.PRIORITY := lv_priority;
            PIPE ROW (lv_return);
        
        END LOOP;
        lv_priority := lv_priority + 1;
    
    END IF;
    
    dbms_output.put_line( 'Checking first name and last name ' || NVL(FIRST_NAME,'.') || '  ' || NVL(LAST_NAME,'.'));
    -- first name and last name match
    IF NVL(FIRST_NAME,' ') != ' ' AND NVL(LAST_NAME,' ') != ' '
    THEN
        dbms_output.put_line( 'IN Checking first name and last name' );
        FOR SIN2 IN ( 
            SELECT * 
            FROM SPBPERS, SPRIDEN 
            WHERE SPRIDEN_PIDM = SPBPERS_PIDM 
                AND SPRIDEN_CHANGE_IND IS NULL 
                AND UPPER(SPRIDEN_FIRST_NAME) = UPPER(FIRST_NAME)
                AND UPPER(SPRIDEN_LAST_NAME) = UPPER(LAST_NAME) )
        LOOP
            dbms_output.put_line( SIN2.SPRIDEN_PIDM ||' '||SIN2.SPRIDEN_FIRST_NAME||' '||SIN2.SPRIDEN_LAST_NAME );
            lv_return.PIDM := SIN2.SPRIDEN_PIDM;
            lv_return.ID := SIN2.SPRIDEN_ID;
            lv_return.FIRSTNAME := SIN2.SPRIDEN_FIRST_NAME;
            lv_return.LASTNAME := SIN2.SPRIDEN_LAST_NAME;
            lv_return.MIDDLENAME := SIN2.SPRIDEN_MI;
            lv_return.BIRTHDATE := TO_CHAR(SIN2.SPBPERS_BIRTH_DATE, 'YYYY-MM-DD');
            lv_return.GENDER := SIN2.SPBPERS_SEX;
            lv_return.SSN := SIN2.SPBPERS_SSN;
            lv_return.MATCH_CODE := 'FL';
            lv_return.PRIORITY := lv_priority;
            
            PIPE ROW (lv_return);
        
        END LOOP;
        lv_priority := lv_priority + 1;
    
    END IF;

    -- first name and alternate last name match
    IF NVL(FIRST_NAME,' ') != ' ' AND NVL(ALT_LAST_NAME,' ') != ' '
    THEN
        FOR SIN2 IN ( 
            SELECT * 
            FROM SPBPERS, SPRIDEN 
            WHERE SPRIDEN_PIDM = SPBPERS_PIDM 
                AND SPRIDEN_CHANGE_IND IS NULL 
                AND UPPER(SPRIDEN_FIRST_NAME) = UPPER(FIRST_NAME)
                AND UPPER(SPRIDEN_LAST_NAME) = UPPER(ALT_LAST_NAME) )
        LOOP
            lv_return.PIDM := SIN2.SPRIDEN_PIDM;
            lv_return.ID := SIN2.SPRIDEN_ID;
            lv_return.FIRSTNAME := SIN2.SPRIDEN_FIRST_NAME;
            lv_return.LASTNAME := SIN2.SPRIDEN_LAST_NAME;
            lv_return.MIDDLENAME := SIN2.SPRIDEN_MI;
            lv_return.BIRTHDATE := TO_CHAR(SIN2.SPBPERS_BIRTH_DATE, 'YYYY-MM-DD');
            lv_return.GENDER := SIN2.SPBPERS_SEX;
            lv_return.SSN := SIN2.SPBPERS_SSN;
            lv_return.MATCH_CODE := 'FA';
            lv_return.PRIORITY := lv_priority;
            
            PIPE ROW (lv_return);
        
        END LOOP;
        lv_priority := lv_priority + 1;
    
    END IF;

    -- last name and birthdate match
    IF NVL(LAST_NAME,' ') != ' ' AND lv_birthdate IS NOT NULL
    THEN
        FOR SIN2 IN ( 
            SELECT * 
            FROM SPBPERS, SPRIDEN 
            WHERE SPRIDEN_PIDM = SPBPERS_PIDM 
                AND SPRIDEN_CHANGE_IND IS NULL 
                AND UPPER(SPRIDEN_LAST_NAME) = UPPER(LAST_NAME)
                AND TO_CHAR(SPBPERS_BIRTH_DATE, 'YYYYMMDD') = TO_CHAR(lv_birthdate, 'YYYYMMDD') )
        LOOP
            lv_return.PIDM := SIN2.SPRIDEN_PIDM;
            lv_return.ID := SIN2.SPRIDEN_ID;
            lv_return.FIRSTNAME := SIN2.SPRIDEN_FIRST_NAME;
            lv_return.LASTNAME := SIN2.SPRIDEN_LAST_NAME;
            lv_return.MIDDLENAME := SIN2.SPRIDEN_MI;
            lv_return.BIRTHDATE := TO_CHAR(SIN2.SPBPERS_BIRTH_DATE, 'YYYY-MM-DD');
            lv_return.GENDER := SIN2.SPBPERS_SEX;
            lv_return.SSN := SIN2.SPBPERS_SSN;
            lv_return.MATCH_CODE := 'LB';
            lv_return.PRIORITY := lv_priority;
            
            PIPE ROW (lv_return);
        
        END LOOP;
        lv_priority := lv_priority + 1;
    END IF;

    -- alternate last name and birthdate match
    IF NVL(ALT_LAST_NAME,' ') != ' ' AND lv_birthdate IS NOT NULL
    THEN
        FOR SIN2 IN ( 
            SELECT * 
            FROM SPBPERS, SPRIDEN 
            WHERE SPRIDEN_PIDM = SPBPERS_PIDM 
                AND SPRIDEN_CHANGE_IND IS NULL 
                AND UPPER(SPRIDEN_LAST_NAME) = UPPER(ALT_LAST_NAME)
                AND TO_CHAR(SPBPERS_BIRTH_DATE, 'YYYYMMDD') = TO_CHAR(lv_birthdate, 'YYYYMMDD') )
        LOOP
            lv_return.PIDM := SIN2.SPRIDEN_PIDM;
            lv_return.ID := SIN2.SPRIDEN_ID;
            lv_return.FIRSTNAME := SIN2.SPRIDEN_FIRST_NAME;
            lv_return.LASTNAME := SIN2.SPRIDEN_LAST_NAME;
            lv_return.MIDDLENAME := SIN2.SPRIDEN_MI;
            lv_return.BIRTHDATE := TO_CHAR(SIN2.SPBPERS_BIRTH_DATE, 'YYYY-MM-DD');
            lv_return.GENDER := SIN2.SPBPERS_SEX;
            lv_return.SSN := SIN2.SPBPERS_SSN;
            lv_return.MATCH_CODE := 'AB';
            lv_return.PRIORITY := lv_priority;
            
            PIPE ROW (lv_return);
        
        END LOOP;
        lv_priority := lv_priority + 1;
    
    END IF;

    -- first name and birthdate match
    IF NVL(FIRST_NAME,' ') != ' ' AND lv_birthdate IS NOT NULL
    THEN
        FOR SIN2 IN ( 
            SELECT * 
            FROM SPBPERS, SPRIDEN 
            WHERE SPRIDEN_PIDM = SPBPERS_PIDM 
                AND SPRIDEN_CHANGE_IND IS NULL 
                AND UPPER(SPRIDEN_LAST_NAME) = UPPER(FIRST_NAME)
                AND TO_CHAR(SPBPERS_BIRTH_DATE, 'YYYYMMDD') = TO_CHAR(lv_birthdate, 'YYYYMMDD') )
        LOOP
            lv_return.PIDM := SIN2.SPRIDEN_PIDM;
            lv_return.ID := SIN2.SPRIDEN_ID;
            lv_return.FIRSTNAME := SIN2.SPRIDEN_FIRST_NAME;
            lv_return.LASTNAME := SIN2.SPRIDEN_LAST_NAME;
            lv_return.MIDDLENAME := SIN2.SPRIDEN_MI;
            lv_return.BIRTHDATE := TO_CHAR(SIN2.SPBPERS_BIRTH_DATE, 'YYYY-MM-DD');
            lv_return.GENDER := SIN2.SPBPERS_SEX;
            lv_return.SSN := SIN2.SPBPERS_SSN;
            lv_return.MATCH_CODE := 'FB';
            lv_return.PRIORITY := lv_priority;
            
            PIPE ROW (lv_return);
        
        END LOOP;
        lv_priority := lv_priority + 1;
    
    END IF;

    -- first name and birthdate match
    IF NVL(ALT_FIRST_NAME,' ') != ' ' AND lv_birthdate IS NOT NULL
    THEN
        FOR SIN2 IN ( 
            SELECT * 
            FROM SPBPERS, SPRIDEN 
            WHERE SPRIDEN_PIDM = SPBPERS_PIDM 
                AND SPRIDEN_CHANGE_IND IS NULL 
                AND UPPER(SPRIDEN_FIRST_NAME) = UPPER(ALT_FIRST_NAME)
                AND TO_CHAR(SPBPERS_BIRTH_DATE, 'YYYYMMDD') = TO_CHAR(lv_birthdate, 'YYYYMMDD') )
        LOOP
            lv_return.PIDM := SIN2.SPRIDEN_PIDM;
            lv_return.ID := SIN2.SPRIDEN_ID;
            lv_return.FIRSTNAME := SIN2.SPRIDEN_FIRST_NAME;
            lv_return.LASTNAME := SIN2.SPRIDEN_LAST_NAME;
            lv_return.MIDDLENAME := SIN2.SPRIDEN_MI;
            lv_return.BIRTHDATE := TO_CHAR(SIN2.SPBPERS_BIRTH_DATE, 'YYYY-MM-DD');
            lv_return.GENDER := SIN2.SPBPERS_SEX;
            lv_return.SSN := SIN2.SPBPERS_SSN;
            lv_return.MATCH_CODE := 'XB';
            lv_return.PRIORITY := lv_priority;
            
            PIPE ROW (lv_return);
        
        END LOOP;
        lv_priority := lv_priority + 1;
    
    END IF;

    -- last name
    IF NVL(LAST_NAME,' ') != ' ' 
    THEN
        FOR SIN2 IN ( 
            SELECT * 
            FROM SPBPERS, SPRIDEN 
            WHERE SPRIDEN_PIDM = SPBPERS_PIDM 
                AND SPRIDEN_CHANGE_IND IS NULL 
                AND UPPER(SPRIDEN_LAST_NAME) = UPPER(LAST_NAME) )
        LOOP
            lv_return.PIDM := SIN2.SPRIDEN_PIDM;
            lv_return.ID := SIN2.SPRIDEN_ID;
            lv_return.FIRSTNAME := SIN2.SPRIDEN_FIRST_NAME;
            lv_return.LASTNAME := SIN2.SPRIDEN_LAST_NAME;
            lv_return.MIDDLENAME := SIN2.SPRIDEN_MI;
            lv_return.BIRTHDATE := TO_CHAR(SIN2.SPBPERS_BIRTH_DATE, 'YYYY-MM-DD');
            lv_return.GENDER := SIN2.SPBPERS_SEX;
            lv_return.SSN := SIN2.SPBPERS_SSN;
            lv_return.MATCH_CODE := 'L';
            lv_return.PRIORITY := lv_priority;
            
            PIPE ROW (lv_return);
        
        END LOOP;
        lv_priority := lv_priority + 1;
    
    END IF;


    -- first name and birthdate match
    IF lv_birthdate IS NOT NULL
    THEN
        FOR SIN2 IN ( 
            SELECT * 
            FROM SPBPERS, SPRIDEN 
            WHERE SPRIDEN_PIDM = SPBPERS_PIDM 
                AND SPRIDEN_CHANGE_IND IS NULL 
                AND TO_CHAR(SPBPERS_BIRTH_DATE, 'YYYYMMDD') = TO_CHAR(lv_birthdate, 'YYYYMMDD') )
        LOOP
            lv_return.PIDM := SIN2.SPRIDEN_PIDM;
            lv_return.ID := SIN2.SPRIDEN_ID;
            lv_return.FIRSTNAME := SIN2.SPRIDEN_FIRST_NAME;
            lv_return.LASTNAME := SIN2.SPRIDEN_LAST_NAME;
            lv_return.MIDDLENAME := SIN2.SPRIDEN_MI;
            lv_return.BIRTHDATE := TO_CHAR(SIN2.SPBPERS_BIRTH_DATE, 'YYYY-MM-DD');
            lv_return.GENDER := SIN2.SPBPERS_SEX;
            lv_return.SSN := SIN2.SPBPERS_SSN;
            lv_return.MATCH_CODE := 'B';
            lv_return.PRIORITY := lv_priority;
            
            PIPE ROW (lv_return);
        
        END LOOP;
        lv_priority := lv_priority + 1;
    
    END IF;

END;

CREATE OR REPLACE PUBLIC SYNONYM LF_XML_FIND_MATCHING FOR BANINST1.LF_XML_FIND_MATCHING;

GRANT EXECUTE ON BANINST1.LF_XML_FIND_MATCHING TO BAN_SS_USER;

    
    