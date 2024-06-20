package com.adobe.raven;

import com.adobe.raven.db.queries.*;
import com.adobe.raven.dto.geoReviewer.GeoReviewer;
import com.adobe.raven.dto.job.JobStep;
import com.adobe.raven.dto.job.MasterJob;
import com.adobe.raven.dto.message.MessageRepository;
import com.adobe.raven.dto.proof.MailChains;
import com.adobe.raven.dto.proof.ProofItem;
import com.adobe.raven.dto.proof.ProofRepository;
import com.adobe.raven.dto.proof.ProofRequestReviewers;
import com.adobe.raven.dto.qa.QaItem;
import com.adobe.raven.dto.qa.QaRepository;
import com.adobe.raven.dto.qa.ReshareQaRepository;
import com.adobe.raven.dto.user.UserInfo;
import com.adobe.raven.dto.workfront.WorkfrontRepository;
import com.adobe.raven.request.ProofRequest;
import com.adobe.raven.response.ProofResponse;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class MailUtils {


    public static Session outlookAuthenticate() {

        final Properties properties = new Properties();

        properties.put("mail.smtp.host", "smtp.office365.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.timeout", "100000000");
        properties.put("mail.smtp.connectiontimeout", "100000000");
        properties.put("mail.smtp.auth", "true"); // enable authentication
        properties.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS

        // create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            // override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("proofs@adobe.com", "GlobalMarketingTech@2022");//"RavenforGMO@789123");// "!QAZ1qaz@WSX2wsx");
            }
        };

        Session session = Session.getInstance(properties, auth);
        // Session session = Session.getDefaultInstance(properties, auth);

        return session;
    }

    public static ProofResponse sendMails(String jobId, ProofRequest proofRequest,
                                          MasterJobQueries masterJobQueries,
                                          ProofRepositoryQueries proofRepositoryQueries,
                                          UserInfoQueries userInfoQueries,
                                          WorkfrontQueries workfrontQueries,
                                          GeoReviewerQueries geoReviewerQueries,
                                          MessageQueries messageQueries,
                                          LanguageRepositoryQueries languageRepositoryQueries) {

        ProofResponse proofResponse = new ProofResponse();
        MasterJob masterJob = masterJobQueries.get(jobId);
        UserInfo createdUserInfo = userInfoQueries.getByGuidID(proofRequest.getUserId());

        ArrayList<JobStep> steps = masterJob.getSteps();
        JobStep proofStep = steps.stream().filter(step -> step
                        .getType().equalsIgnoreCase(Constants.Proofing_Type))
                .findAny().orElse(null);

        if (proofStep != null) {

            ProofRepository proofRepository = proofRepositoryQueries.get(proofStep.getId());
            ArrayList<ProofItem> items = proofRepository.getItems();

            ArrayList<ProofRequestReviewers> selectedGeoReviewers = proofRequest.getSelectedGeoReviewers();

            Session session = outlookAuthenticate();

            //UserInfo userInfo  = userInfoQueries.get(proofRequest.getUserId());
            WorkfrontRepository workfrontRepository = workfrontQueries.get(masterJob.getWorkfrontId());


            for (ProofRequestReviewers reviewer : selectedGeoReviewers) {

                String language = reviewer.getLanguage();
                String languageLabel = languageRepositoryQueries.get(language).getLanguageName();
                List<ProofItem> languageItems = items.stream().filter(item ->
                        item.getLanguage().equalsIgnoreCase(language)).collect(Collectors.toList());

                if (language.equals("en")) { // add ie proofs

                    List<ProofItem> ieLanguageItems = items.stream().filter(item ->
                            item.getLanguage().equalsIgnoreCase("ie")).collect(Collectors.toList());
                    languageItems.addAll(ieLanguageItems);
                }
                ArrayList<MessageRepository> messages = new ArrayList<>();

                for (ProofItem proofItem : languageItems) {

                    MessageRepository messageRepository = messageQueries.get(proofItem.getId());
                    messages.add(messageRepository);
                }

                String subject;
                String body;
                String toList, ccList;
                Boolean isUrgent = reviewer.getUrgent();
                ArrayList<String> ccLists = new ArrayList<>();

                ccLists.add("proofs@adobe.com");
                if (createdUserInfo != null) {
                    ccLists.add(createdUserInfo.getEmailId());
                }
                MailChains mailChains = new MailChains();
                mailChains.setAttachments(messages);

                toList = changeListToString(reviewer.getSendList());
                mailChains.setToList(toList);

                ccList = changeListToString(ccLists);
                mailChains.setCcList(ccList);

                if (isUrgent != null && isUrgent) {
                    subject = "Urgent: " + languageLabel + " Proofs for " + masterJob.getName();
                } else {
                    subject = languageLabel + " Proofs for " + masterJob.getName();
                }
                mailChains.setSubject(subject);
                GeoReviewer geoReviewer = geoReviewerQueries.getByEmail(reviewer.getSendList().get(0));


                body = Constants.MailBody
                        .replaceAll("\\{\\{ReviewerName\\}\\}", geoReviewer.getName())
                        .replaceAll("\\{\\{createdUser\\}\\}", createdUserInfo.getName())
                        .replaceAll("\\{\\{campaignName\\}\\}", masterJob.getName())
                        .replaceAll("\\{\\{deploymentDate\\}\\}", workfrontRepository.getDeploymentDate())
                        .replaceAll("\\{\\{languageName\\}\\}", languageLabel);

                mailChains.setBody(body);

                boolean isSent = sendLanguageMail(mailChains, session);
                // proofResponse.set
                for (ProofItem proofItem : languageItems) {

                    proofItem.setStatus("Sent");
                }
                proofResponse.setProofSent(true);
            }

            boolean allMessagesNotSent = items.stream()
                    .anyMatch(item -> !item.getStatus()
                            .equalsIgnoreCase("Sent"));

            if (allMessagesNotSent) {
                proofRepository.setStatus("Few Sent");
                masterJob.setState("Few Proofs Sent");
                proofStep.setState("Few Sent");
            } else {
                proofRepository.setStatus("Sent");
                masterJob.setState("Proofs Sent");
                proofStep.setState("Sent");
            }
            proofRepositoryQueries.update(proofRepository);
            masterJobQueries.update(masterJob);
        }
        return proofResponse;
    }

    public static Boolean sendQaMail(MasterJob masterJob, QaRepositoryQueries qaRepositoryQueries,
                                           UserInfoQueries userInfoQueries, WorkfrontQueries workfrontQueries,
                                     JobStep nextStep, JobStep currentJobStep) {

        QaRepository qaRepository = qaRepositoryQueries.get(nextStep.getId());

        UserInfo userInfo = userInfoQueries.getByGuidID(qaRepository.getAssignee());
        WorkfrontRepository workfrontRepository = workfrontQueries.get(masterJob.getWorkfrontId());

        MailChains mailChains = new MailChains();
        String body = Constants.NextQaMailBody.replaceAll("\\{\\{QaName\\}\\}", userInfo.getName())
                                               .replaceAll("\\{\\{QaCurrentLevel\\}\\}", currentJobStep.getName())
                                               .replaceAll("\\{\\{QaNextLevel\\}\\}", nextStep.getName())
                                                .replaceAll("\\{\\{campaignName\\}\\}", masterJob.getName())
                                                .replaceAll("\\{\\{deploymentDate\\}\\}",workfrontRepository.getDeploymentDate());

        mailChains.setBody(body);
        mailChains.setSubject("Raven Update : " + currentJobStep.getName() + " Completed");
        mailChains.setToList(userInfo.getEmailId());

        Session session = outlookAuthenticate();
        boolean isSent = sendLanguageMail(mailChains,session);

        return isSent;
    }


    public static Boolean sendProofMail(MasterJob masterJob, ProofRepositoryQueries proofRepositoryQueries,
                                     UserInfoQueries userInfoQueries, WorkfrontQueries workfrontQueries,
                                     JobStep nextStep, JobStep currentJobStep) {

        //ProofRepository proofRepository = proofRepositoryQueries.get(nextStep.getId());


        UserInfo userInfo = userInfoQueries.getByGuidID(masterJob.getMasterJobMetadata().getCreatedById());
        WorkfrontRepository workfrontRepository = workfrontQueries.get(masterJob.getWorkfrontId());

        MailChains mailChains = new MailChains();
        String body = Constants.NextProofMailBody.replaceAll("\\{\\{userName\\}\\}", userInfo.getName())
                .replaceAll("\\{\\{QaCurrentLevel\\}\\}", currentJobStep.getName())
                .replaceAll("\\{\\{campaignName\\}\\}", masterJob.getName())
                .replaceAll("\\{\\{deploymentDate\\}\\}",workfrontRepository.getDeploymentDate());

        mailChains.setBody(body);
        mailChains.setSubject("Raven Update : " + currentJobStep.getName() + " Completed");
        mailChains.setToList(userInfo.getEmailId());

        Session session = outlookAuthenticate();
        boolean isSent = sendLanguageMail(mailChains,session);

        return isSent;
    }

    public static boolean sendLanguageMail(MailChains mailChains, Session session) {

        try {

            final MimeMessage msg = new MimeMessage(session);

            // set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress("proofs@adobe.com", "NoReply-JD"));

            // set subject
            msg.setSubject(mailChains.getSubject(), "UTF-8");

            // set current date
            msg.setSentDate(new Date());

            MimeMultipart mimeMultipart = new MimeMultipart();

            // set body
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(mailChains.getBody());

            mimeMultipart.addBodyPart(bodyPart);

            // set attachments byte[] converted to base64
            ArrayList<MessageRepository> attachments = mailChains.getAttachments();
            if(attachments != null) {
                for (MessageRepository attachment : attachments) {

                    MimeBodyPart part = addAttachment(attachment.getActivityId() + ".msg",
                            attachment.getContent().getBase64());
                    mimeMultipart.addBodyPart(part);
                }
            }
            // added body and attachments to message
            msg.setContent(mimeMultipart);

            // added to emails
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailChains.getToList(), false));

            // added cc emails
            if(mailChains.getCcList() != null) {
                msg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(mailChains.getCcList(), false));
            }

            Transport.send(msg);
            System.out.println("Message Id : " + msg.getMessageID());

        } catch (Exception e) {
            System.out.println();
            e.printStackTrace();
            waitForSeconds(10);
            sendLanguageMail(mailChains, session);
            // sendEmail(session, toEmail, subject, body, count);
            System.out.println(" Error in " + "with Attachment");
            System.out.println();
            // e.printStackTrace();
        }

        return true;
    }

    public static MimeBodyPart addAttachment(final String fileName, final String fileContent)
            throws MessagingException {
        if (fileName == null || fileContent == null) {
            return null;
        }

        System.out.println("addAttachment()");
        MimeBodyPart filePart = new PreencodedMimeBodyPart("base64");
        filePart.setContent(fileContent, "text/html");//"image/*");
        filePart.setFileName(fileName);
        System.out.println("addAttachment success !");
        return filePart;
    }

    private static void waitForSeconds(int secondsInt) {

        try {
            long initialCurrentTimeInMillis = System.currentTimeMillis();
            System.out.println("Wait for some seconds ");
            // wait(10000);
            TimeUnit.SECONDS.sleep(secondsInt);
            float finalSecs = (System.currentTimeMillis() - initialCurrentTimeInMillis) / 1000;
            System.out.println("Thread now running : " + finalSecs);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }



    public static String changeListToString(List<String> list) {

        return list.stream().collect(Collectors.joining(", "));
    }

}
