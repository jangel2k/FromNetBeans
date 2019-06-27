package dellocks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import static java.lang.System.console;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import oracle.jdbc.OracleDriver;

public class EnrollmentCnt
{
  public static void main(String[] args)
    throws Exception
  {
    Connection c = null;
    String dbUrl = null;
    String envStr = null;
    if (args.length == 0)
    {
      System.out.println("Need to supply name of DB env(TEST,PROD or HCSC_ADMIN");
      System.exit(0);
    }
    else if (args[0].toLowerCase().equals("test"))
    {
      dbUrl = "jdbc:oracle:thin:@scan-638587.rsp.medecision.com:1521/OLTP1TEST";
      envStr = "TEST";
    }
    else if (args[0].toLowerCase().equals("prod"))
    {
      dbUrl = "jdbc:oracle:thin:@scan-645842.rsp.medecision.com:1521/OLTP1PROD";
      envStr = "PROD";
    }
    else if (args[0].toLowerCase().equals("hcsc"))
    {
      dbUrl = "jdbc:oracle:thin:@scan-804005.rsp.medecision.com:1521/HCSACMP";
      envStr = "HCSC";
    }
    else
    {
      System.out.println("PROD,TEST or HCSC are the only acceptable parameters");
    }
    System.out.println(dbUrl);
    System.out.println(LocalDate.now());
    System.out.println(LocalTime.now());
    
    
    String toMail = args[1];
    //Use Aerial.Batch@medecision.com
    String fromMail = args[2];
    String ccStr =args[3];
    String outDir =args[4];
    
    
    
    
    DriverManager.registerDriver(new OracleDriver());
    try
    {
      c = DriverManager.getConnection(dbUrl, "jangelucci", "shelby2k5");
      
      String plsql = "DECLARE  appl_config_query VARCHAR2(500);  appl_config_query_result VARCHAR2(10);  member_count_query VARCHAR2(500);  member_count_query_result NUMBER;  date_query VARCHAR(500);  date_query_result VARCHAR(10);  BEGIN  /*Query for first day of current month*/  date_query :=  'SELECT to_char(ADD_MONTHS((LAST_DAY(SYSDATE)+1),-1),''YYYY-MM-DD'') FROM DUAL';  EXECUTE IMMEDIATE date_query into date_query_result;  /*Query loop for all users with AERIALCM in the name*/  FOR item IN\t(  SELECT substr(owner,0,3) client_code,OWNER FROM all_tables WHERE TABLE_NAME='APPL_CONFIG' AND OWNER LIKE '%_AERIALCM' AND num_rows >0 )  LOOP   /*Query to get Remote Mmember source (Batch, OLI)*/  appl_config_query := 'select appl_config_val from ' ||  item.owner || '.appl_config where APPL_CONFIG_CODE=''RemoteMemberSource''';  EXECUTE IMMEDIATE appl_config_query into appl_config_query_result;  /*If BATCH, query remote tables for member counts:  members where primary enrollment starts on or before the beginning of the month, and term as nul or after endo of month*/  IF 'BATCH' = appl_config_query_result THEN  member_count_query := 'select count(distinct(me.pat_id))  from ' || item.owner || '.memb_enrollment me join ' || item.owner || '.remote_elig re on re.pat_id = me.pat_id  where me.is_primary=1 and me.eff_date <= to_date(''' || date_query_result || ' 00:00:00'',''YYYY-MM-DD HH24:MI:SS'')  and (me.term_date > to_date(''' || date_query_result || '00:00:00'',''YYYY-MM-DD HH24:MI:SS'') or me.term_date is null) and re.active_flag=''1''';  EXECUTE IMMEDIATE member_count_query into member_count_query_result;  DBMS_OUTPUT.put_line (item.client_code || ',' || appl_config_query_result || ',' || member_count_query_result);  ELSE  /*If not BATCH, still print Client code and type*/  DBMS_OUTPUT.put_line (item.client_code || ',' || appl_config_query_result || ',0');  END IF;  END LOOP;  EXCEPTION  WHEN OTHERS THEN  DBMS_OUTPUT.PUT_LINE('Error Message: '|| SQLERRM);  END; ";
      
      CallableStatement cs = c.prepareCall(plsql);Throwable localThrowable3 = null;
      try
      {
        c.setAutoCommit(false);
        DbmsOutput dbmsOutput = new DbmsOutput(c);
        dbmsOutput.enable(1000000);
        mail Mail = new mail();
        
        cs.execute();
        
        dbmsOutput.show();
        LocalDate now = LocalDate.now();
        LocalDate earlier = now.minusMonths(1L);
        
        earlier.getMonth();
        earlier.getYear();
        String nowStr = earlier.getMonth().toString() + " " + earlier.getYear();
        
        Mail.sendMail(toMail, fromMail, "172.21.50.104", "Member Enrollment Report for " + nowStr + "(" + envStr + ")", dbmsOutput.getMess(),ccStr);
        //Added 06/06/2019
        System.out.println(outDir + nowStr + "_out.txt");
        File file = new File(outDir + nowStr + "_out.txt");
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
		System.out.println(dbmsOutput.getMess());
        dbmsOutput.close();
        c.commit();
        c.close();
        
      }
      catch (SQLException localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (cs != null) {
          if (localThrowable3 != null) {
            try
            {
              cs.close();
            }
            catch (SQLException localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            cs.close();
          }
        }
      }
    }
    catch (SQLException e)
    {
      System.out.println("Error processing SQL statement:" + e);
      mail Mail1 = new mail();
      Mail1.sendMail(toMail, fromMail, "172.21.50.104", "Member Count Failed", "Member Count Process Failed",ccStr);
    }
  }
}
