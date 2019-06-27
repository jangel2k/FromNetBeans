/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dellocks;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class mail {

    private String toMail;
    private String fromMail;
    private String smtpHost;
    public String subject;
    public String body;
    
    public mail(){
    
        //toMail = pToMail;
        //fromMail = pFromMail;
        //smtpHost = pSmtpHost;
       // subject = pSubject;
        //body = pBody;
    }

   // mail(String joeangelucci, String joeangeluccimedecisioncom, String string, String dellocks_process_faile, String the_Delock_Oricess_failed_please_review) {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   // }

    public void sendMail(String pToMail,String pFromMail,String pSmtpHost,String pSubject,String pBody,String ccString){
        Properties properties = System.getProperties();

      // Setup mail server
      properties.setProperty("mail.smtp.host", pSmtpHost);

      // Get the default Session object.
      Session session = Session.getDefaultInstance(properties);

      try {
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(pFromMail));

         // Set To: header field of the header.
                  message.addRecipient(Message.RecipientType.TO, new InternetAddress(pToMail));
                 message.addRecipients(Message.RecipientType.CC,InternetAddress.parse(ccString));

         // Set Subject: header field
         message.setSubject(pSubject);

         // Now set the actual message
         message.setText(pBody);

         // Send message
         Transport.send(message);
         System.out.println("Sent message successfully....");
      } catch (MessagingException e) {
           System.out.println("Problem sending message");
             
            // Prints what exception has been thrown
            System.out.println(e);
      }
   }
  }


    

