CREATE OR REPLACE PACKAGE            "SVKTEDI" as

 procedure p_process_svrtreq
 (p_bgn_no                        in  varchar2,
  p_error_message                 in  out varchar2);

 procedure p_get_spriden_pidm
 (p_spriden_id                    in  varchar2,
  p_spriden_pidm                  out number);

 procedure p_match_student_info
 (p_svrtreq_bgn_no                in  varchar2,
  p_svrtreq_birth_date            in  date,
  p_svrtreq_sex                   in  varchar2,
  p_svrtreq_ssn                   in  varchar2,
  p_svrtreq_last_name             in  varchar2,
  p_svrtreq_first_name            in  varchar2,
  p_svrtreq_state_ind             out varchar2,
  p_svrtreq_match_ind             out varchar2,
  p_spriden_pidm                  out number,
  p_svrtreq_id                    out varchar2);

 procedure p_get_spriden_id
 (p_spriden_pidm                  in  number,
  p_spriden_id                    out varchar2);

 procedure p_insert_svrtdup_record
 (p_svrtdup_bgn_no                in  varchar2,
  p_svrtdup_pidm                  in  number);

 procedure p_check_student_deceased
 (p_spbpers_pidm                  in  number,
  p_spbpers_dead_ind              out varchar2);

 procedure p_check_for_holds
 (p_spriden_pidm                  in  number,
  p_svrtreq_state_ind             out varchar2,
  p_svrtreq_hold_ind              out varchar2);

 procedure p_determine_send_date
 (p_svrtreq_action_cde            in  varchar2,
  p_svrtreq_bgn_no                in  varchar2,
  p_svrtreq_send_date             out date,
  p_svrtreq_state_ind             out varchar2,
  p_svrtreq_date_ind              out varchar2,
  p_error_message                 out varchar2);

 procedure p_get_svrtnte_note
 (p_svrtnte_bgn_no                in  varchar2,
  p_record_type                   in  varchar2,
  p_svrtnte_note                  out varchar2);

 procedure p_get_stvterm_end_date
 (p_stvterm_code                  in  varchar2,
  p_stvterm_end_date              out date);

 procedure p_insert_shttran_record
 (p_spriden_pidm                  in  number,
  p_spriden_id                    in  varchar2,
  p_svrtreq_bgn_no                in  varchar2,
  p_shttran_seq_no                out number);

 procedure p_get_shttran_seq_no
 (p_shttran_pidm                  in  number,
  p_shttran_seq_no                out number);

 procedure p_get_sgbstdn_term_code_eff
 (p_sgbstdn_pidm                  in  number,
  p_sgbstdn_term_code_eff         out varchar2);

 procedure p_get_requestor_address
 (p_sobsbgi_sbgi_code             in  varchar2,
  p_sobsbgi_street_line1          out varchar2,
  p_sobsbgi_street_line2          out varchar2,
  p_sobsbgi_street_line3          out varchar2,
  p_sobsbgi_city                  out varchar2,
  p_sobsbgi_stat_code             out varchar2,
  p_sobsbgi_zip                   out varchar2,
  p_sobsbgi_natn_code             out varchar2);

 procedure p_get_gtvsdax_translation_code
 (p_gtvsdax_external_code         in  varchar2,
  p_gtvsdax_external_code_group   in  varchar2,
  p_gtvsdax_translation_code      out varchar2);

 procedure p_get_sfbetrm_term_code
 (p_sfbetrm_pidm                  in  number,
  p_sfbetrm_term_code             out varchar2);

 procedure p_update_svrtreq_record
 (p_svrtreq_bgn_no                in  varchar2,
  p_svrtreq_send_date             in  date,
  p_svrtreq_state_ind             in  varchar2,
  p_svrtreq_match_ind             in  varchar2,
  p_svrtreq_hold_ind              in  varchar2,
  p_svrtreq_date_ind              in  varchar2,
  p_svrtreq_completion_ind        in  varchar2,
  p_svrtreq_student_id            in  varchar2,
  p_svrtreq_seq_no                in  number,
  p_svrtreq_reason_cde            in  varchar2);

 procedure p_delete_svrtreq_records
 (p_svrtreq_bgn_no                in  varchar2);

 procedure p_delete_svrtnte_records
 (p_svrtnte_bgn_no                in  varchar2);

 procedure p_delete_svrtdup_records
 (p_svrtdup_bgn_no                in  varchar2);

end svktedi;

 
 
/


CREATE OR REPLACE PACKAGE BODY            "SVKTEDI" as

/*----------------------------------------------------------------------------*/
/* Program mainline.                                                          */
/*----------------------------------------------------------------------------*/

 procedure p_process_svrtreq
 (p_bgn_no                        in  varchar2,
  p_error_message                 in  out varchar2) is

 v_svrtreq_action_cde             svrtreq.svrtreq_action_cde%type;
 v_svrtreq_birth_date             svrtreq.svrtreq_birth_date%type;
 v_svrtreq_gender                 svrtreq.svrtreq_gender%type;
 v_svrtreq_sin                    svrtreq.svrtreq_sin%type;
 v_svrtreq_surname                svrtreq.svrtreq_surname%type;
 v_svrtreq_firstname              svrtreq.svrtreq_firstname%type;
 v_svrtreq_send_date              svrtreq.svrtreq_send_date%type;
 v_svrtreq_state_ind              svrtreq.svrtreq_state_ind%type;
 v_svrtreq_match_ind              svrtreq.svrtreq_match_ind%type;
 v_svrtreq_hold_ind               svrtreq.svrtreq_hold_ind%type;
 v_svrtreq_date_ind               svrtreq.svrtreq_date_ind%type;
 v_svrtreq_completion_ind         svrtreq.svrtreq_completion_ind%type;
 v_svrtreq_id                     svrtreq.svrtreq_id%type;
 v_spbpers_dead_ind               spbpers.spbpers_dead_ind%type;
 v_svrtreq_reason_cde             svrtreq.svrtreq_reason_cde%type;
 v_spriden_pidm                   spriden.spriden_id%type;
 v_shttran_seq_no                 shttran.shttran_seq_no%type;

 cursor c1 is
  select svrtreq_action_cde,
         svrtreq_birth_date,
         svrtreq_gender,
         svrtreq_sin,
         svrtreq_surname,
         svrtreq_firstname,
         svrtreq_send_date,
         svrtreq_state_ind,
         svrtreq_match_ind,
         svrtreq_hold_ind,
         svrtreq_date_ind,
         svrtreq_completion_ind,
         svrtreq_id,
		 svrtreq_reason_cde
    from svrtreq
   where svrtreq_bgn02 = p_bgn_no;

 begin

  open c1;
  fetch c1
   into v_svrtreq_action_cde,
        v_svrtreq_birth_date,
        v_svrtreq_gender,
        v_svrtreq_sin,
        v_svrtreq_surname,
        v_svrtreq_firstname,
        v_svrtreq_send_date,
        v_svrtreq_state_ind,
        v_svrtreq_match_ind,
        v_svrtreq_hold_ind,
        v_svrtreq_date_ind,
        v_svrtreq_completion_ind,
        v_svrtreq_id,
		v_svrtreq_reason_cde;
  close c1;

  p_error_message := '~';
  v_spriden_pidm  := null;

  if  v_svrtreq_state_ind in ('P','M')
  then
      p_delete_svrtdup_records
      (p_bgn_no);

      if  v_svrtreq_id is null
      then
          p_match_student_info
          (p_bgn_no,
           v_svrtreq_birth_date,
           v_svrtreq_gender,
           v_svrtreq_sin,
           v_svrtreq_surname,
           v_svrtreq_firstname,
           v_svrtreq_state_ind,
           v_svrtreq_match_ind,
           v_spriden_pidm,
           v_svrtreq_id);
      else
          v_svrtreq_state_ind := 'H';
          v_svrtreq_match_ind := 'X';
      end if;

      if  v_svrtreq_state_ind = 'H'
      then
          if  v_spriden_pidm is null
          then
              p_get_spriden_pidm
              (v_svrtreq_id,
               v_spriden_pidm);
          end if;
--
          p_check_student_deceased
          (v_spriden_pidm,
           v_spbpers_dead_ind);

          if  nvl(v_spbpers_dead_ind,'n') = 'Y'
          then
              v_svrtreq_send_date  :=  trunc(sysdate);
              v_svrtreq_state_ind  := 'C';
              v_svrtreq_hold_ind   := 'N';
              v_svrtreq_reason_cde := '12';
          end if;
      end if;
  end if;

  if  v_svrtreq_state_ind = 'H'
  then
      if  nvl(v_svrtreq_hold_ind,'~') = 'O'
      then
          v_svrtreq_state_ind := 'D';
      else
          if  v_spriden_pidm is null
          then
              p_get_spriden_pidm
              (v_svrtreq_id,
               v_spriden_pidm);
          end if;

          p_check_for_holds
          (v_spriden_pidm,
           v_svrtreq_state_ind,
           v_svrtreq_hold_ind);
      end if;
  end if;

  if  v_svrtreq_state_ind = 'D'
  then
      p_determine_send_date
      (v_svrtreq_action_cde,
       p_bgn_no,
       v_svrtreq_send_date,
       v_svrtreq_state_ind,
       v_svrtreq_date_ind,
       p_error_message);

      if  p_error_message     = '~'
      and v_svrtreq_state_ind = 'C'
      and v_svrtreq_send_date is not null
      and v_svrtreq_send_date > trunc(sysdate)
      then
          v_svrtreq_reason_cde := '48';
      end if;
  end if;

  if  p_error_message = '~'
  then
      v_shttran_seq_no := null;

      if  v_svrtreq_state_ind   = 'C'
      and v_svrtreq_send_date  is not null
      and v_svrtreq_send_date  <= trunc(sysdate)
      and v_svrtreq_reason_cde is null
      then
          if  v_spriden_pidm is null
          then
              p_get_spriden_pidm
              (v_svrtreq_id,
               v_spriden_pidm);
          end if;

          p_insert_shttran_record
          (v_spriden_pidm,
           v_svrtreq_id,
           p_bgn_no,
           v_shttran_seq_no);
          v_svrtreq_completion_ind := '130';
      end if;

      p_update_svrtreq_record
      (p_bgn_no,
       v_svrtreq_send_date,
       v_svrtreq_state_ind,
       v_svrtreq_match_ind,
       v_svrtreq_hold_ind,
       v_svrtreq_date_ind,
       v_svrtreq_completion_ind,
       v_svrtreq_id,
       v_shttran_seq_no,
       v_svrtreq_reason_cde);
  else
      p_delete_svrtreq_records
      (p_bgn_no);
      p_delete_svrtnte_records
      (p_bgn_no);
      p_delete_svrtdup_records
      (p_bgn_no);
  end if;

 end p_process_svrtreq;

/*----------------------------------------------------------------------------*/
/* Get the student's PIDM from the SPRIDEN table.                             */
/*----------------------------------------------------------------------------*/

 procedure p_get_spriden_pidm
 (p_spriden_id                    in  varchar2,
  p_spriden_pidm                  out number) is

 cursor c2 is
  select max(spriden_pidm)
    from spriden
   where spriden_id = p_spriden_id
     and spriden_change_ind is null;

 begin

  p_spriden_pidm := null;

  open c2;
  fetch c2
   into p_spriden_pidm;
  close c2;

 end p_get_spriden_pidm;

/*----------------------------------------------------------------------------*/
/* Try to determine which student the transcript request is for.              */
/*----------------------------------------------------------------------------*/

 procedure p_match_student_info
 (p_svrtreq_bgn_no                in  varchar2,
  p_svrtreq_birth_date            in  date,
  p_svrtreq_sex                   in  varchar2,
  p_svrtreq_ssn                   in  varchar2,
  p_svrtreq_last_name             in  varchar2,
  p_svrtreq_first_name            in  varchar2,
  p_svrtreq_state_ind             out varchar2,
  p_svrtreq_match_ind             out varchar2,
  p_spriden_pidm                  out number,
  p_svrtreq_id            out varchar2) is

 v_pidm                           number;

 type t1 is table of spriden.spriden_pidm%type
  index by binary_integer;
 t_pidm                           t1;
 t_pidm_count                     binary_integer;
 t_temp_index                     binary_integer;

 cursor c3 is
  select distinct b.spriden_pidm
    from spriden b,
         spbpers a
   where a.spbpers_ssn               = p_svrtreq_ssn
     and trunc(a.spbpers_birth_date) = trunc(p_svrtreq_birth_date)
     and a.spbpers_pidm              = b.spriden_pidm
     and b.spriden_change_ind is null
     and gukcmpr.f_compress_name(b.spriden_first_name)
       = gukcmpr.f_compress_name(p_svrtreq_first_name)
     and gukcmpr.f_compress_name(b.spriden_last_name)
       = gukcmpr.f_compress_name(p_svrtreq_last_name);

 cursor c4 is
  select distinct b.spriden_pidm
    from spriden b,
         spbpers a
   where a.spbpers_ssn  = p_svrtreq_ssn
     and a.spbpers_pidm = b.spriden_pidm
     and b.spriden_change_ind is null
     and gukcmpr.f_compress_name(b.spriden_first_name)
       = gukcmpr.f_compress_name(p_svrtreq_first_name)
     and gukcmpr.f_compress_name(b.spriden_last_name)
       = gukcmpr.f_compress_name(p_svrtreq_last_name);

 cursor c5 is
  select distinct b.spriden_pidm
    from spriden b,
         spbpers a
   where trunc(a.spbpers_birth_date) = trunc(p_svrtreq_birth_date)
     and a.spbpers_sex               = p_svrtreq_sex
     and a.spbpers_pidm              = b.spriden_pidm
     and b.spriden_change_ind is null
     and gukcmpr.f_compress_name(b.spriden_first_name)
       = gukcmpr.f_compress_name(p_svrtreq_first_name)
     and gukcmpr.f_compress_name(b.spriden_last_name)
       = gukcmpr.f_compress_name(p_svrtreq_last_name);

 begin

  p_svrtreq_state_ind  := 'M';
  p_svrtreq_match_ind  := 'N';
  p_spriden_pidm       :=  null;
  p_svrtreq_id :=  null;
  t_pidm_count         :=  0;

  if  p_svrtreq_birth_date is not null
  and p_svrtreq_ssn        is not null
  and p_svrtreq_last_name  is not null
  and p_svrtreq_first_name is not null
  then
      open c3;
      loop

       fetch c3
        into v_pidm;
       exit when c3%notfound;

       t_pidm_count         := t_pidm_count + 1;
       t_pidm(t_pidm_count) := v_pidm;

      end loop;
      close c3;
  end if;

  if  t_pidm_count < 1
  and p_svrtreq_ssn        is not null
  and p_svrtreq_last_name  is not null
  and p_svrtreq_first_name is not null
  then
      open c4;
      loop

       fetch c4
        into v_pidm;
       exit when c4%notfound;

       t_pidm_count         := t_pidm_count + 1;
       t_pidm(t_pidm_count) := v_pidm;

      end loop;
      close c4;
  end if;

  if  t_pidm_count < 1
  and p_svrtreq_birth_date is not null
  and p_svrtreq_sex        is not null
  and p_svrtreq_last_name  is not null
  and p_svrtreq_first_name is not null
  then
      open c5;
      loop

       fetch c5
        into v_pidm;
       exit when c5%notfound;

       t_pidm_count         := t_pidm_count + 1;
       t_pidm(t_pidm_count) := v_pidm;

      end loop;
      close c5;
  end if;

  if  t_pidm_count = 1
  then
      p_svrtreq_state_ind := 'H';
      p_svrtreq_match_ind := 'X';
      p_spriden_pidm      :=  v_pidm;
      p_get_spriden_id
      (v_pidm,
       p_svrtreq_id);
  end if;

  if  t_pidm_count > 1
  then
      p_svrtreq_match_ind  := 'D';
      t_temp_index :=  1;

      while (t_temp_index <= t_pidm_count)
       loop

        v_pidm       := t_pidm(t_temp_index);
        p_insert_svrtdup_record
        (p_svrtreq_bgn_no,
         v_pidm);
        t_temp_index := t_temp_index + 1;

       end loop;
  end if;

 end p_match_student_info;

/*----------------------------------------------------------------------------*/
/* Get the student's ID from the SPRIDEN table.                               */
/*----------------------------------------------------------------------------*/

 procedure p_get_spriden_id
 (p_spriden_pidm                  in  number,
  p_spriden_id                    out varchar2) is

 cursor c7 is
  select max(spriden_id)
    from spriden
   where spriden_pidm = p_spriden_pidm
     and spriden_change_ind is null;

 begin

  p_spriden_id := null;

  open c7;
  fetch c7
   into p_spriden_id;
  close c7;

 end p_get_spriden_id;

/*----------------------------------------------------------------------------*/
/* Insert a record into the SVRTDUP table.                                    */
/*----------------------------------------------------------------------------*/

 procedure p_insert_svrtdup_record
 (p_svrtdup_bgn_no                in  varchar2,
  p_svrtdup_pidm                  in  number) is

 begin

  insert into svrtdup
       ( svrtdup_bgn02,
         svrtdup_pidm,
         svrtdup_data_origin,
         svrtdup_user_id,
         svrtdup_activity_date )
  values
       ( p_svrtdup_bgn_no,
         p_svrtdup_pidm,
        'EDI',
         user,
         trunc(sysdate) );

 end p_insert_svrtdup_record;

/*----------------------------------------------------------------------------*/
/* Check to see if the student has been flagged as deceased.                  */
/*----------------------------------------------------------------------------*/

 procedure p_check_student_deceased
 (p_spbpers_pidm                  in  number,
  p_spbpers_dead_ind              out varchar2) is

 cursor c8 is
  select upper(nvl(spbpers_dead_ind,'n'))
    from spbpers
   where spbpers_pidm = p_spbpers_pidm
     and nvl(spbpers_dead_ind,'n') = 'Y';

 begin

  p_spbpers_dead_ind := null;

  open c8;
  fetch c8
   into p_spbpers_dead_ind;
  close c8;

 end p_check_student_deceased;

/*----------------------------------------------------------------------------*/
/* Check to see if a hold has been placed on the student's transcript.        */
/*----------------------------------------------------------------------------*/

 procedure p_check_for_holds
 (p_spriden_pidm                  in  number,
  p_svrtreq_state_ind             out varchar2,
  p_svrtreq_hold_ind              out varchar2) is

 v_tbraccd_balance number;

 cursor c9 is
  select 'H'
    from stvhldd b,
         sprhold a
   where a.sprhold_pidm = p_spriden_pidm
     and trunc(sysdate)
 between trunc(a.sprhold_from_date)
     and trunc(a.sprhold_to_date)
     and nvl(a.sprhold_release_ind,'n')   != 'Y'
     and a.sprhold_hldd_code               =  b.stvhldd_code
     and nvl(b.stvhldd_trans_hold_ind,'n') = 'Y';

 cursor c10 is
  select nvl(sum(tbraccd_balance),0)
    from tbraccd
   where tbraccd_pidm = p_spriden_pidm;

 begin

  p_svrtreq_state_ind := 'H';
  p_svrtreq_hold_ind  := 'N';

  open c9;
  fetch c9
   into p_svrtreq_hold_ind;
  close c9;

  if  p_svrtreq_hold_ind = 'N'
  then
      v_tbraccd_balance := 0;
      open c10;
      fetch c10
       into v_tbraccd_balance;
      close c10;

      if  v_tbraccd_balance > 0
      then
          p_svrtreq_hold_ind  := '$';
      else
          p_svrtreq_state_ind := 'D';
      end if;
  end if;

 end p_check_for_holds;

/*----------------------------------------------------------------------------*/
/* Determine what the value of the "senddate" field should be.                */
/*----------------------------------------------------------------------------*/

 procedure p_determine_send_date
 (p_svrtreq_action_cde            in  varchar2,
  p_svrtreq_bgn_no                in  varchar2,
  p_svrtreq_send_date             out date,
  p_svrtreq_state_ind             out varchar2,
  p_svrtreq_date_ind              out varchar2,
  p_error_message                 out varchar2) is

 v_svrtnte_data_value             svrtnte.svrtnte_note%type;
 v_gtvsdax_term_code              gtvsdax.gtvsdax_translation_code%type;
 v_gtvsdax_term_date_offset       gtvsdax.gtvsdax_translation_code%type;
 v_term_date_offset               number;

 begin

  p_svrtreq_send_date :=  null;
  p_svrtreq_state_ind := 'D';
  p_svrtreq_date_ind  :=  null;
  p_error_message     := '~';

  if  p_svrtreq_action_cde = 'R4'
  then
      p_svrtreq_date_ind  := 'R4';
      p_svrtreq_state_ind := 'C';
  end if;

  if  p_svrtreq_action_cde = 'R2'
  then
      p_svrtreq_send_date :=  trunc(sysdate);
      p_svrtreq_state_ind := 'C';
  end if;

  if  p_svrtreq_action_cde = 'OT'
  then
      p_get_svrtnte_note
      (p_svrtreq_bgn_no,
      'SENDDATE=',
       v_svrtnte_data_value);

      if  v_svrtnte_data_value is null
      then
          p_error_message := 'TNTE - SENDDATE= record not found';
      else
          p_svrtreq_send_date :=  to_date(v_svrtnte_data_value,'yyyymmdd');
          p_svrtreq_state_ind := 'C';
      end if;
  end if;

  if  p_svrtreq_action_cde = 'R3'
  then
      p_get_svrtnte_note
      (p_svrtreq_bgn_no,
      'TERM=',
       v_svrtnte_data_value);

      if  v_svrtnte_data_value is null
      then
          p_error_message := 'TNTE - TERM= record not found';
      else
          if  nvl(substr(p_svrtreq_bgn_no,1,1),'~') = 'C'
          then
              p_get_gtvsdax_translation_code
              (v_svrtnte_data_value,
              'TERM',
               v_gtvsdax_term_code);
          else
              p_get_gtvsdax_translation_code
              (v_svrtnte_data_value,
              'TERMU',
               v_gtvsdax_term_code);
          end if;

          if  v_gtvsdax_term_code is null
          then
              p_svrtreq_date_ind := 'IT';
          else
              p_get_stvterm_end_date
              (v_gtvsdax_term_code,
               p_svrtreq_send_date);

              if  p_svrtreq_send_date is null
              then
                  p_svrtreq_date_ind  := 'IT';
              else
                  p_svrtreq_state_ind := 'C';

                  p_get_gtvsdax_translation_code
                  ('',
                   'TERM DATE OFFSET',
                   v_gtvsdax_term_date_offset);

                  if  v_gtvsdax_term_date_offset is not null
                  then
                      if  substr(v_gtvsdax_term_date_offset,1,1) in ('+','-')
                      then
                          v_term_date_offset := to_number(substr(v_gtvsdax_term_date_offset,2,10));

                          if  substr(v_gtvsdax_term_date_offset,1,1) = '-'
                          then
                              v_term_date_offset := v_term_date_offset * -1;
                          end if;
                      else
                          v_term_date_offset := to_number(v_gtvsdax_term_date_offset);
                      end if;

                      p_svrtreq_send_date := p_svrtreq_send_date + v_term_date_offset;
                  end if;
              end if;
          end if;
      end if;
  end if;

 end p_determine_send_date;

/*----------------------------------------------------------------------------*/
/* Get the note field from the SVRTNTE table.                                 */
/*----------------------------------------------------------------------------*/

 procedure p_get_svrtnte_note
 (p_svrtnte_bgn_no                in  varchar2,
  p_record_type                   in  varchar2,
  p_svrtnte_note                  out varchar2) is

 cursor c11 is
  select substr(svrtnte_note,instr(svrtnte_note,'=')+1,8)
    from svrtnte
   where svrtnte_bgn02 = p_svrtnte_bgn_no
     and svrtnte_note like p_record_type||'%';

 begin

  p_svrtnte_note := null;

  open c11;
  fetch c11
   into p_svrtnte_note;
  close c11;

 end p_get_svrtnte_note;

/*----------------------------------------------------------------------------*/
/* Get the term end date from the STVTERM table.                              */
/*----------------------------------------------------------------------------*/

 procedure p_get_stvterm_end_date
 (p_stvterm_code                  in  varchar2,
  p_stvterm_end_date              out date) is

 cursor c12 is
  select trunc(stvterm_end_date)
    from stvterm
   where stvterm_code = p_stvterm_code;

 begin

  p_stvterm_end_date := null;

  open c12;
  fetch c12
   into p_stvterm_end_date;
  close c12;

 end p_get_stvterm_end_date;

/*----------------------------------------------------------------------------*/
/* Insert a record into the SHTTRAN table.                                    */
/*----------------------------------------------------------------------------*/

 procedure p_insert_shttran_record
 (p_spriden_pidm                  in  number,
  p_spriden_id                    in  varchar2,
  p_svrtreq_bgn_no                in  varchar2,
  p_shttran_seq_no                out number) is

  v_sgbstdn_term_code_eff         sgbstdn.sgbstdn_term_code_eff%type;
  v_shttran_sbgi_code             shttran.shttran_sbgi_code%type;
  v_sobsbgi_street_line1          sobsbgi.sobsbgi_street_line1%type;
  v_sobsbgi_street_line2          sobsbgi.sobsbgi_street_line2%type;
  v_sobsbgi_street_line3          sobsbgi.sobsbgi_street_line3%type;
  v_sobsbgi_city                  sobsbgi.sobsbgi_city%type;
  v_sobsbgi_stat_code             sobsbgi.sobsbgi_stat_code%type;
  v_sobsbgi_zip                   sobsbgi.sobsbgi_zip%type;
  v_sobsbgi_natn_code             sobsbgi.sobsbgi_natn_code%type;
  v_sfbetrm_term_code             sfbetrm.sfbetrm_term_code%type;
  v_gtvsdax_tprt_code             shttran.shttran_tprt_code%type;

 begin

  p_get_shttran_seq_no
  (p_spriden_pidm,
   p_shttran_seq_no);
  p_get_sgbstdn_term_code_eff
  (p_spriden_pidm,
   v_sgbstdn_term_code_eff);

  if  substr(p_svrtreq_bgn_no,1,1) = 'C'
  then
      v_shttran_sbgi_code := 'OCAS';
  else
      v_shttran_sbgi_code := 'OUAC';
  end if;

  p_get_requestor_address
  (v_shttran_sbgi_code,
   v_sobsbgi_street_line1,
   v_sobsbgi_street_line2,
   v_sobsbgi_street_line3,
   v_sobsbgi_city,
   v_sobsbgi_stat_code,
   v_sobsbgi_zip,
   v_sobsbgi_natn_code);
  p_get_gtvsdax_translation_code
  ('',
   'TPRT',
   v_gtvsdax_tprt_code);
  p_get_sfbetrm_term_code
  (p_spriden_pidm,
   v_sfbetrm_term_code);

  insert into shttran
       ( shttran_user,
         shttran_pidm,
         shttran_seq_no,
         shttran_id,
         shttran_type,
         shttran_term,
         shttran_levl_code,
         shttran_addr_name,
         shttran_street1,
         shttran_street2,
         shttran_street3,
         shttran_city,
         shttran_stat_code,
         shttran_zip,
         shttran_request_date,
         shttran_activity_date,
         shttran_tprt_code,
         shttran_no_copies,
         shttran_official_ind,
         shttran_term_code_in_prg,
         shttran_natn_code,
         shttran_sbgi_code,
         shttran_hold_grde_ind,
         shttran_hold_degr_ind )
  values
       ( user,
         p_spriden_pidm,
         p_shttran_seq_no,
         p_spriden_id,
        'E',
         v_sgbstdn_term_code_eff,
        'AL',
         v_shttran_sbgi_code,
         v_sobsbgi_street_line1,
         v_sobsbgi_street_line2,
         v_sobsbgi_street_line3,
         v_sobsbgi_city,
         v_sobsbgi_stat_code,
         v_sobsbgi_zip,
         trunc(sysdate),
         trunc(sysdate),
         v_gtvsdax_tprt_code,
         1,
        'Y',
         v_sfbetrm_term_code,
         v_sobsbgi_natn_code,
         v_shttran_sbgi_code,
        'N',
        'N' );

 end p_insert_shttran_record;

/*----------------------------------------------------------------------------*/
/* Get the next sequence number for the student from the SHTTRAN table.       */
/*----------------------------------------------------------------------------*/

 procedure p_get_shttran_seq_no
 (p_shttran_pidm                  in  number,
  p_shttran_seq_no                out number) is

 cursor c13 is
  select nvl(max(shttran_seq_no),0) + 1
    from shttran
   where shttran_pidm = p_shttran_pidm;

 begin

  p_shttran_seq_no := 1;

  open c13;
  fetch c13
   into p_shttran_seq_no;
  close c13;

 end p_get_shttran_seq_no;

/*----------------------------------------------------------------------------*/
/* Get the most record term code for the student from the SGBSTDN table.      */
/*----------------------------------------------------------------------------*/

 procedure p_get_sgbstdn_term_code_eff
 (p_sgbstdn_pidm                  in  number,
  p_sgbstdn_term_code_eff         out varchar2) is

 cursor c14 is
  select max(sgbstdn_term_code_eff)
    from saturn.sgbstdn
   where sgbstdn_pidm = p_sgbstdn_pidm;

 begin

  p_sgbstdn_term_code_eff := null;

  open c14;
  fetch c14
   into p_sgbstdn_term_code_eff;
  close c14;

 end p_get_sgbstdn_term_code_eff;

/*----------------------------------------------------------------------------*/
/* Get the requestor's address information from the SOBSBGI table.            */
/*----------------------------------------------------------------------------*/

 procedure p_get_requestor_address
 (p_sobsbgi_sbgi_code             in  varchar2,
  p_sobsbgi_street_line1          out varchar2,
  p_sobsbgi_street_line2          out varchar2,
  p_sobsbgi_street_line3          out varchar2,
  p_sobsbgi_city                  out varchar2,
  p_sobsbgi_stat_code             out varchar2,
  p_sobsbgi_zip                   out varchar2,
  p_sobsbgi_natn_code             out varchar2) is

 cursor c15 is
  select a.sobsbgi_street_line1,
         a.sobsbgi_street_line2,
         a.sobsbgi_street_line3,
         a.sobsbgi_city,
         a.sobsbgi_stat_code,
         a.sobsbgi_zip,
         a.sobsbgi_natn_code
    from sobsbgi a
   where a.sobsbgi_sbgi_code = p_sobsbgi_sbgi_code;

 begin

  p_sobsbgi_street_line1 := null;
  p_sobsbgi_street_line2 := null;
  p_sobsbgi_street_line3 := null;
  p_sobsbgi_city         := null;
  p_sobsbgi_stat_code    := null;
  p_sobsbgi_zip          := null;
  p_sobsbgi_natn_code    := null;

  open c15;
  fetch c15
   into p_sobsbgi_street_line1,
        p_sobsbgi_street_line2,
        p_sobsbgi_street_line3,
        p_sobsbgi_city,
        p_sobsbgi_stat_code,
        p_sobsbgi_zip,
        p_sobsbgi_natn_code;
  close c15;

 end p_get_requestor_address;

/*----------------------------------------------------------------------------*/
/* Get the term code from the GTVSDAX table.                                  */
/*----------------------------------------------------------------------------*/

 procedure p_get_gtvsdax_translation_code
 (p_gtvsdax_external_code         in  varchar2,
  p_gtvsdax_external_code_group   in  varchar2,
  p_gtvsdax_translation_code      out varchar2) is

 cursor c16 is
  select nvl(gtvsdax_translation_code,gtvsdax_external_code)
    from gtvsdax
   where upper(gtvsdax_external_code)       =  nvl(p_gtvsdax_external_code,upper(gtvsdax_external_code))
     and upper(gtvsdax_internal_code)       = 'EDIONT'
     and upper(gtvsdax_internal_code_group) =  p_gtvsdax_external_code_group;

 begin

  p_gtvsdax_translation_code := null;

  open c16;
  fetch c16
   into p_gtvsdax_translation_code;
  close c16;

 end p_get_gtvsdax_translation_code;

/*----------------------------------------------------------------------------*/
/* Get the student's most recent term code from the SFBETRM table.            */
/*----------------------------------------------------------------------------*/

 procedure p_get_sfbetrm_term_code
 (p_sfbetrm_pidm                  in  number,
  p_sfbetrm_term_code             out varchar2) is

 cursor c17 is
  select max(sfbetrm_term_code)
    from sfbetrm
   where sfbetrm_pidm = p_sfbetrm_pidm;

 begin

  p_sfbetrm_term_code := null;

  open c17;
  fetch c17
   into p_sfbetrm_term_code;
  close c17;

 end p_get_sfbetrm_term_code;

/*----------------------------------------------------------------------------*/
/* Update a record in the SVRTREQ table.                                      */
/*----------------------------------------------------------------------------*/

 procedure p_update_svrtreq_record
 (p_svrtreq_bgn_no                in  varchar2,
  p_svrtreq_send_date             in  date,
  p_svrtreq_state_ind             in  varchar2,
  p_svrtreq_match_ind             in  varchar2,
  p_svrtreq_hold_ind              in  varchar2,
  p_svrtreq_date_ind              in  varchar2,
  p_svrtreq_completion_ind        in  varchar2,
  p_svrtreq_student_id            in  varchar2,
  p_svrtreq_seq_no                in  number,
  p_svrtreq_reason_cde            in  varchar2) is

 begin

  update svrtreq
     set svrtreq_send_date      = p_svrtreq_send_date,
         svrtreq_state_ind      = p_svrtreq_state_ind,
         svrtreq_match_ind      = p_svrtreq_match_ind,
         svrtreq_hold_ind       = p_svrtreq_hold_ind,
         svrtreq_date_ind       = p_svrtreq_date_ind,
         svrtreq_completion_ind = p_svrtreq_completion_ind,
         svrtreq_id             = p_svrtreq_student_id,
         svrtreq_seq_no         = p_svrtreq_seq_no,
         svrtreq_reason_cde     = p_svrtreq_reason_cde,
         svrtreq_activity_date  = trunc(sysdate)
   where svrtreq_bgn02 = p_svrtreq_bgn_no;

 end p_update_svrtreq_record;

/*----------------------------------------------------------------------------*/
/* Delete all records in the SVRTREQ table for the BGN NO in question.        */
/*----------------------------------------------------------------------------*/

 procedure p_delete_svrtreq_records
 (p_svrtreq_bgn_no                in  varchar2) is

 begin

  delete from svrtreq
   where svrtreq_bgn02 = p_svrtreq_bgn_no;

 end p_delete_svrtreq_records;

/*----------------------------------------------------------------------------*/
/* Delete all records in the SVRTNTE table for the BGN NO in question.        */
/*----------------------------------------------------------------------------*/

 procedure p_delete_svrtnte_records
 (p_svrtnte_bgn_no                in  varchar2) is

 begin

  delete from svrtnte
   where svrtnte_bgn02 = p_svrtnte_bgn_no;

 end p_delete_svrtnte_records;

/*----------------------------------------------------------------------------*/
/* Delete all records in the SVRTDUP table for the BGN NO in question.        */
/*----------------------------------------------------------------------------*/

 procedure p_delete_svrtdup_records
 (p_svrtdup_bgn_no                in  varchar2) is

 begin

  delete from svrtdup
   where svrtdup_bgn02 = p_svrtdup_bgn_no;

 end p_delete_svrtdup_records;

end svktedi;

/
