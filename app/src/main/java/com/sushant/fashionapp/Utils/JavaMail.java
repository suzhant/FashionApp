package com.sushant.fashionapp.Utils;

import android.content.Context;

import com.sushant.fashionapp.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class JavaMail {

    private Context context;

    public JavaMail(Context context) {
        this.context = context;
    }

    public void sendMail(String receiverMail, String name, String orderId, File file) {

        try {
            String senderEmail = "sushantshrestha62@gmail.com";

            String host = "smtp.gmail.com";

            Properties properties = System.getProperties();

            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, context.getString(R.string.mail_secret));
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress("noreply@support.fashionApp.com.np", "Fashion App"));
            mimeMessage.setHeader("Disposition-Notification-To", "noreply@support.fashionApp.com.np");
            mimeMessage.setReplyTo(InternetAddress.parse("noreply@support.fashionApp.com.np", false));
            mimeMessage.setSentDate(new Date());

            //single recipient
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverMail));

            //multiple recipients
//            javax.mail.Address[] recipient = new javax.mail.Address[]{
//                    new InternetAddress("xresthasushant61@gmail.com"),
//                    new InternetAddress("sushantshrestha62@gmail.com")
//            };
//            mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.toString(recipient));
            mimeMessage.setSubject("Order Confirmation");


            // Create the message part
            MimeBodyPart messageBodyPart1 = new MimeBodyPart();
            //body
            messageBodyPart1.setText(MessageFormat.format("Dear {0},\n\nYour order for #{1} has been confirmed.\n\n\n\n\n\nSincerely,\nFashion development Team", name, orderId));

            //if you want to inline image in email
            //inlineImage(messageBodyPart);

            //getting pic from internet and attaching it in mail
//            MimeBodyPart imageBodyPart = new MimeBodyPart();
//            String filepath="";
//            URLDataSource bds = new URLDataSource(new URL(filepath));
//            imageBodyPart.setDataHandler(new DataHandler(bds));
//            imageBodyPart.setFileName("photo.jpg");

            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(file);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName("invoice.pdf");
            //       attachmentBodyPart.attachFile(file, "application/pdf", null);


            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart1);
            //    multipart.addBodyPart(imageBodyPart);
            multipart.addBodyPart(attachmentBodyPart);

            // Send the complete message parts
            mimeMessage.setContent(multipart);


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
