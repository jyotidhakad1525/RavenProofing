package com.adobe.raven;

import com.adobe.raven.db.queries.*;
import com.adobe.raven.dto.HtmlInfo;
import com.adobe.raven.dto.LanguageCodeRepository;
import com.adobe.raven.dto.Metadata;
import com.adobe.raven.dto.ResponseError;
import com.adobe.raven.dto.cgen.CgenContent;
import com.adobe.raven.dto.cgen.CgenRepository;
import com.adobe.raven.dto.job.JobSegmentInfo;
import com.adobe.raven.dto.job.JobStep;
import com.adobe.raven.dto.job.MasterJob;
import com.adobe.raven.dto.message.JobMessageRequest;
import com.adobe.raven.dto.message.MessageContent;
import com.adobe.raven.dto.message.MessageRepository;
import com.adobe.raven.dto.message.ProofMessageInfo;
import com.adobe.raven.dto.proof.ProofInfo;
import com.adobe.raven.dto.proof.ProofItem;
import com.adobe.raven.dto.proof.ProofRepository;
import com.adobe.raven.dto.proof.ProofUpdateRequest;
import com.adobe.raven.dto.qa.QaItem;
import com.adobe.raven.dto.qa.QaRepository;
import com.adobe.raven.dto.user.UserInfo;
import com.adobe.raven.dto.workfront.*;
import com.adobe.raven.request.JobRequest;
import com.adobe.raven.request.ParseRequest;
import com.adobe.raven.response.JobResponse;
import com.adobe.raven.response.ParseResponse;
import com.adobe.raven.response.ProofResponse;
import com.auxilii.msgparser.Message;
import com.auxilii.msgparser.MsgParser;
import com.google.gson.Gson;
import okhttp3.*;
import org.apache.commons.io.serialization.ValidatingObjectInputStream;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.tomcat.util.buf.Utf8Encoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.io.*;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {

    public static JobResponse updateJob(String jobId
                                        ,JobRequest jobRequest, MasterJobQueries masterJobQueries,
                                        QaRepositoryQueries qaRepositoryQueries, ProofRepositoryQueries proofRepositoryQueries,
                                        WorkfrontQueries workfrontQueries,
                                        MessageQueries messageQueries, CgenRepositoryQueries cgenRepositoryQueries,
                                        UserInfoQueries userInfoQueries) {

        JobResponse jobResponse = new JobResponse();
        int type = jobRequest.getType();


        MasterJob masterJob = masterJobQueries.get(jobId);
        ArrayList<JobSegmentInfo> segmentInfos = jobRequest.getSegmentInfos();//masterJob.getSegmentInfos();
        JobStep selfQaStep = masterJob.getSteps().stream()
                .filter(step -> step.getName().equals(Constants.QALevel_Self))
                .findAny().orElse(null);

        QaRepository selfQaRepo = qaRepositoryQueries.get(selfQaStep.getId());

        CgenRepository insertedCgenRepository;
         insertedCgenRepository = cgenRepositoryQueries.get(selfQaRepo.getCgenId());

        ArrayList<ResponseError> errors = new ArrayList<>();
        ArrayList<ResponseError> warnings = new ArrayList<>();
        jobResponse.setErrors(errors);
        jobResponse.setWarnings(warnings);

        // update msg files for other qas
        if(type == 1) {

            String qaIds = jobRequest.getQaId();


            if(jobRequest.getQaId() == null
            || jobRequest.getMessages() == null) {
                ResponseError responseError = getInvalidParametersError();
                //jobResponse.setError(responseError);
                errors.add(responseError);
                return jobResponse;
            }

            // check for activities
            verifyHtmls(jobRequest, errors, warnings, selfQaRepo, messageQueries);

            if(errors.size() == 0) {
                QaRepository insertedQaRepository = qaRepositoryQueries.get(jobRequest.getQaId());
                Metadata metadata = insertedQaRepository.getMetadata();
                metadata.setLastModifiedById(jobRequest.getUserId());
                UserInfo userInfo = userInfoQueries.getByGuidID(jobRequest.getUserId());
                if (userInfo != null) {
                    metadata.setLastModifiedBy(userInfo.getName());
                }
                metadata.setLastModifiedAt(System.currentTimeMillis());

                insertedQaRepository.setMetadata(metadata);

                ArrayList<QaItem> qaItems = new ArrayList<>();
                insertedQaRepository.setItems(qaItems);

                ArrayList<JobMessageRequest> messages = jobRequest.getMessages();
                for (JobMessageRequest jobMessageRequest : messages) {

                    QaItem qaItem = new QaItem();
                    qaItem.setStatus(Constants.QAItemStatus);

                    String md5 = null;
                    try {
                        md5 = convertToMD5(jobMessageRequest.getBase64());
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    MessageRepository insertedMessageRepository = messageQueries.getByHash(md5);
                    String messageId = null;

                    if (insertedMessageRepository != null) {
                        messageId = insertedMessageRepository.getId();
                    } else {

                        MessageRepository messageRepository = createMessage(jobMessageRequest,
                                messageQueries, metadata);
                        insertedMessageRepository = messageRepository;
                        messageId = messageRepository.getId();
                    }

//                    if(insertedMessageRepository == null) {
//                        System.out.println("here :: "+ md5);
//                    }

                    String messageActivityId = insertedMessageRepository.getActivityId();
                    ArrayList<CgenContent> content = insertedCgenRepository.getContent();
                    CgenContent foundCgenContent = content.stream().filter(cgenContent ->
                            cgenContent.getActivityId().equals(messageActivityId))
                            .findAny().orElse(null);

                    if (foundCgenContent != null) {
                        String segmentName = foundCgenContent.getCreativeFileName();
                        qaItem.setSegment(segmentName);

                        if (segmentInfos != null) {
                            JobSegmentInfo foundSegmentInfo = segmentInfos.stream().filter(segmentInfo ->
                                    segmentInfo.getSegmentName().equals(segmentName))
                                    .findAny().orElse(null);

                            if (foundSegmentInfo != null) {
                                qaItem.setBu(foundSegmentInfo.getBu());
                            }
                        }
                    }

                    qaItem.setId(messageId);
                    qaItems.add(qaItem);

                }

                boolean isUpdated = qaRepositoryQueries.update(insertedQaRepository);
                jobResponse.setJobUpdated(isUpdated);
            } else {

                jobResponse.setJobUpdated(false);
            }
        }
        else if(type == 2) { // update cgen for self qa

            String qaId = jobRequest.getQaId();

            if(jobRequest.getQaId() == null
            && jobRequest.getSegmentInfos() != null) {
                ResponseError responseError = getInvalidParametersError();
               // jobResponse.setError(responseError);
                errors.add(responseError);
                return jobResponse;
            }

            QaRepository insertedQaRepository = qaRepositoryQueries.get(jobRequest.getQaId());
            Metadata metadata = insertedQaRepository.getMetadata();
            metadata.setLastModifiedById(jobRequest.getUserId());
            UserInfo userInfo = userInfoQueries.getByGuidID(jobRequest.getUserId());
            if(userInfo != null) {
                metadata.setLastModifiedBy(userInfo.getName());
            }
            metadata.setLastModifiedAt(System.currentTimeMillis());

            insertedQaRepository.setMetadata(metadata);

            ArrayList<QaItem> items = insertedQaRepository.getItems();

            for(QaItem item : items) {

                String insertedSegment = item.getSegment();
                JobSegmentInfo segmentInfo =  segmentInfos.stream().filter(segment -> segment.getSegmentName()
                        .equalsIgnoreCase(insertedSegment))
                        .findAny().orElse(null);

                if(segmentInfo != null) {
                    item.setBu(segmentInfo.getBu());
                }
            }

            boolean isUpdated = qaRepositoryQueries.update(insertedQaRepository);
            jobResponse.setJobUpdated(isUpdated);

        }
        else if(type == 3) { // update qa status

            String qaId = jobRequest.getQaId();
            String qaStatus = jobRequest.getQaStatus();

            if(qaId == null
            && qaStatus == null) {
                ResponseError error = invalidParameterError();
                errors.add(error);
                return jobResponse;
            }

            JobStep step = masterJob.getSteps().stream()
                    .filter(jobStep -> jobStep.getId().equalsIgnoreCase(qaId)).findFirst().orElse(null);

            if(qaStatus.equalsIgnoreCase("COMPLETED") ) {
                step.setState("COMPLETED");
                masterJob.setState(step.getName() + " " + step.getState());

                // for sending mail
                int nextSequence = step.getSequence()+1;
                JobStep nextStep = masterJob.getSteps().stream()
                        .filter(jobStep -> jobStep.getSequence() == nextSequence ).findFirst().orElse(null);

                if(nextStep != null) {

                    if(nextStep.getType().equalsIgnoreCase(Constants.QaType)) {
                        Thread thread =new Thread(new Runnable() {
                            @Override
                            public void run() {

                                MailUtils.sendQaMail(masterJob, qaRepositoryQueries, userInfoQueries, workfrontQueries,
                                        nextStep, step);
                            }
                        });

                        thread.start();
                    } else if(nextStep.getType().equalsIgnoreCase(Constants.Proofing_Type)) {

                        Thread thread =new Thread(new Runnable() {
                            @Override
                            public void run() {

                                MailUtils.sendProofMail(masterJob, proofRepositoryQueries, userInfoQueries, workfrontQueries,
                                        nextStep, step);
                            }
                        });

                        thread.start();


                    }
                }
            } else if(qaStatus.equalsIgnoreCase("FAILED")) {
                step.setState("FAILED");
                masterJob.setState(step.getName() + " " + step.getState());
            } else {
                ResponseError error = invalidParameterError();
                errors.add(error);
                return jobResponse;
            }


            boolean isUpdated = masterJobQueries.update(masterJob);
            jobResponse.setJobUpdated(isUpdated);
        }
        else {

            ResponseError error = invalidParameterError();
            errors.add(error);
            //jobResponse.setError(error);
        }

        return jobResponse;
    }


    public static void verifyHtmls(JobRequest jobRequest, ArrayList<ResponseError> errors,
                            ArrayList<ResponseError> warnings,
                            QaRepository selfQaRepo, MessageQueries messageQueries) {


        ArrayList<MessageRepository> insertedMessageRepositories = new ArrayList<>();
        HashMap<String, Boolean> insertedActivityIdMap = new HashMap<>();
        ArrayList<QaItem> items = selfQaRepo.getItems();
        for(QaItem item : items) {

            MessageRepository messageRepository = messageQueries.get(item.getId());
            insertedMessageRepositories.add(messageRepository);
            insertedActivityIdMap.put(messageRepository.getActivityId(), false);
        }

        ArrayList<JobMessageRequest> messages = jobRequest.getMessages();
        for(JobMessageRequest message : messages) {


            MessageRepository messageRepository = null;
            try {
                messageRepository = messageQueries.getByHash(convertToMD5(message.getBase64()));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            if(messageRepository == null) {
                messageRepository = parseMessageRepository(message);
            }

            boolean isPresent = insertedActivityIdMap.containsKey(messageRepository.getActivityId());
            if(!isPresent) {
                ResponseError error = new ResponseError();
                error.setMessage(Constants.ErrorActivityIdMissingInSelfQa
                        .replaceAll("\\{\\{activityId\\}\\}", messageRepository.getActivityId()));
                errors.add(error);
            } else {
                insertedActivityIdMap.put(messageRepository.getActivityId(), true);
            }
        }

        for(MessageRepository message : insertedMessageRepositories) {

            boolean isPresent = insertedActivityIdMap.get(message.getActivityId());
            if(!isPresent) {
                ResponseError error = new ResponseError();
                error.setMessage(Constants.ErrorActivityIdMissingInCurrentQa
                        .replaceAll("\\{\\{activityId\\}\\}", message.getActivityId()));
                errors.add(error);
            }
        }

    }

    public static JobResponse verifyContent(JobRequest jobRequest,
                                            MessageQueries messageQueries) {

        JobResponse jobResponse = new JobResponse();
        ArrayList<ResponseError> errors = new ArrayList<>();
        ArrayList<ResponseError> warnings = new ArrayList<>();
        jobResponse.setErrors(errors);
        jobResponse.setWarnings(warnings);
        boolean activityMissing = false;

        if(jobRequest.getCgen() == null || jobRequest.getMessages() == null) {

            ResponseError responseError = invalidParameterError();
            jobResponse.setError(responseError);
            return jobResponse;
        }

        ArrayList<CgenContent> cgenContents = jobRequest.getCgen();

        int rowCount = 1;

        // check for activity id and creativeFileName
        for(CgenContent content : cgenContents) {
            String activityId = content.getActivityId();
            String creativeFileName = content.getCreativeFileName();
            ResponseError responseError = null;

            rowCount++;
            if(activityId == null || activityId.isEmpty()) {
                responseError = new ResponseError();
                activityMissing = true;
                responseError.setMessage(Constants.ErrorActivityIdMissing
                        .replaceAll("\\{\\{rowNumber\\}\\}", String.valueOf(rowCount)));
                errors.add(responseError);
            }

            if(creativeFileName == null || creativeFileName.isEmpty()) {
                responseError = new ResponseError();
                responseError.setMessage(Constants.ErrorCreativeFileNameMissing
                        .replaceAll("\\{\\{rowNumber\\}\\}", String.valueOf(rowCount)));

                errors.add(responseError);
            }

        }

        if(!activityMissing) {
            // get unique activity ids from cgen
            Map<String, List<CgenContent>> activityIdMap = cgenContents.stream().
                    collect(Collectors.groupingBy(CgenContent::getActivityId, Collectors.toList()));

            Set<String> activityIds = activityIdMap.keySet();

            HashMap<String, Boolean> activityIdPresentmap = new HashMap<>();
            for (String id : activityIds) {
                activityIdPresentmap.put(id, false);
            }

            ArrayList<JobMessageRequest> messages = jobRequest.getMessages();

            for (JobMessageRequest message : messages) {

                MessageRepository messageRepository = getMessageRepository(message,
                        messageQueries);

                String activityId = messageRepository.getActivityId();

                if (activityIdPresentmap.containsKey(activityId)) {
                    activityIdPresentmap.put(activityId, true);
                } else {
                    // not present in cgen
                    ResponseError activityIdMissingInCgen = new ResponseError();
                    activityIdMissingInCgen.setMessage(Constants.ErrorActivityIdMissingInCgen
                            .replaceAll("\\{\\{activityId\\}\\}", activityId));
                    errors.add(activityIdMissingInCgen);
                }

            }

            for (String id : activityIds) {

                Boolean isPresent = activityIdPresentmap.get(id);

                if (!isPresent) {
                    ResponseError error = new ResponseError();
                    error.setMessage(Constants.WarningMissingActivityIdMsgFile
                            .replaceAll("\\{\\{activityId\\}\\}", id));
                    warnings.add(error);
                }
            }
        }
            if (errors.size() > 0) {
                jobResponse.setValidData(false);
            } else {
                jobResponse.setValidData(true);
            }

        return jobResponse;
    }

    public static MessageRepository getMessageRepository(JobMessageRequest message,
                                       MessageQueries messageQueries) {

        MessageRepository messageRepository = null;
        String activityId = null;
        String hashed = null;
        try {
            hashed = convertToMD5(message.getBase64());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        messageRepository = messageQueries.getByHash(hashed);

        if(messageRepository == null) {

            messageRepository = parseMessageRepository(message);

        }

        return messageRepository;
    }

    public static String rtfToHtml(Reader rtf) throws IOException {
        JEditorPane p = new JEditorPane();
        p.setContentType("text/rtf");
        EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
        try {
            kitRtf.read(rtf, p.getDocument(), 0);
            kitRtf = null;
            EditorKit kitHtml = p.getEditorKitForContentType("text/html");
            Writer writer = new StringWriter();
            kitHtml.write(writer, p.getDocument(), 0, p.getDocument().getLength());
            return writer.toString();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MessageRepository parseMessage(String base64String) {

//        byte[] msgBytes = Base64.getDecoder().decode(base64String);
//        MAPIMessage msg = null;
//
//        try {
//            msg = new MAPIMessage(new ByteArrayInputStream(msgBytes));
//
//            System.out.println("From:" + msg.getDisplayFrom());
//            System.out.println("To:" + msg.getDisplayTo());
//            System.out.println("CC:" + msg.getDisplayCC());
//            System.out.println("BCC:" + msg.getDisplayBCC());
//
//            String htmlText = rtfToHtml(new StringReader(msg.getRtfBody()));
//            System.out.println("Body:" + htmlText);
//            System.out.println("Subject:" + msg.getSubject());
//        } catch (ChunkNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //String str = handleSpecialCharacters(base64String);

//        byte[] msgBytes = handleSpecialCharacters(Base64.getDecoder().decode(base64String).toString()).
//                getBytes();



//        String value = new String(Base64.getDecoder().decode(base64String), StandardCharsets.UTF_8);
//        String encoded = handleSpecialCharacters(value);
//        //byte[] msgBytes = encoded.getBytes(StandardCharsets.UTF_8);
        byte[] msgBytes = Base64.getDecoder().decode(base64String);


        //String asString = new String(msgBytes, StandardCharsets.UTF_8);
        MsgParser msgp = new MsgParser();
        msgp.setRtf2htmlConverter(new JEditorPaneRTF2HTMLConverter());
        Message msg = null;
        try {
            msg = msgp.parseMsg(new ByteArrayInputStream(msgBytes));
           // msg = msgp.parseMsg(new ByteArrayInputStream(asString.getBytes()));
         //   msg = msgp.parseMsg("C:\\Users\\kmehra\\Downloads\\_A464782 pl_PL 3651302_20220324_EMEA_ACQ_EM2 Proof 1 multipart_alternative_ Nowość_ generowanie grafiki 3D_ Dołącz do nowej kreatywnej fali_ (1).msg");

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (msg != null) {
            String body = msg.getConvertedBodyHTML();
            String md5 = null;

            String subject = msg.getSubject();
            String senderName = msg.getFromName();
            String senderAddress = msg.getFromEmail();


            HtmlInfo htmlInfo = new HtmlInfo();
            htmlInfo.setBody(body);
            System.out.println("Body :: " + body);

            String[] subjectArray = subject.split("\\[")[1].split(" ");
            String activityId = subjectArray[0];
            String langLocal = subjectArray[1];
            String language = langLocal.split("_")[0];
        }

       // String input = "NadawcÂ¹ tej wiadomoÅci marketingowej jest Adobe Systems Software Ireland Limited, 4-6 Riverwalk";
        String input = "Nadawc\\xc3\\x82\\xc2\\xb9 tej wiadomo\\xc3\\x85\\xc2\\x93ci marketingowej jest Adobe Systems Software Ireland Limited";


        String result =  fixDoubleUTF8Encoding(input);//URLDecoder.decode(input, StandardCharsets.UTF_8);//
        System.out.println(result);

        return null;
    }


    private static String fixDoubleUTF8Encoding(String s) {
        // interpret the string as UTF_8
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        // now check if the bytes contain 0x83 0xC2, meaning double encoded garbage
       // if(isDoubleEncoded(bytes)) {
            // if so, lets fix the string by assuming it is ASCII extended and recode it once
            s = new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
       // }
        return s;
    }

    private static boolean isDoubleEncoded(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if(bytes[i] == -125 && i+1 < bytes.length && bytes[i+1] == -62) {
                return true;
            }
        }
        return false;
    }


    public static String handleSpecialCharacters(String str) {
        byte[] utf8bytes = new byte[0];
        try {
            utf8bytes = str.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Charset utf8charset = Charset.forName("UTF-8");
        Charset iso88591charset = Charset.forName("ISO-8859-1");

        String string = new String ( utf8bytes, utf8charset ); //Conversion to UTF-8

        System.out.println("UTF8"+string);

        byte[] iso88591bytes = string.getBytes(iso88591charset);

        String string2 = new String ( iso88591bytes, iso88591charset ); //Conversion to ISO-88591

        System.out.println("ISO"+string2);

        return string2;
    }

    public static MessageRepository parseMessageRepository(JobMessageRequest jobMessageRequest) {

        MessageRepository messageRepository = new MessageRepository();
        MessageContent content = new MessageContent();
        content.setType(Constants.MessageFileType);
        content.setBase64(jobMessageRequest.getBase64());
        //messageRepository.setMetadata(metadata);

        messageRepository.setContent(content);

             ParseRequest parseRequest = new ParseRequest();
             parseRequest.setMsgFiles(jobMessageRequest.getBase64());
             Gson gson = new Gson();
             String request = gson.toJson(parseRequest);

             String response = null;
            try {
                response = callPostUrl(Constants.MessageParserUrl, request, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(response != null) {

            ParseResponse parseResponse = gson.fromJson(response, ParseResponse.class);
            String body = parseResponse.getHtmlBody();
            String subject = parseResponse.getSubject();
            String senderName = parseResponse.getSenderName();
            String senderAddress = parseResponse.getSenderAddress();

                String md5 = null;
            try {
                md5 = convertToMD5(jobMessageRequest.getBase64());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            messageRepository.setMd5(md5);

            messageRepository.setSubject(subject);
            messageRepository.setSenderName(senderName);
            messageRepository.setSenderAddress(senderAddress);

            HtmlInfo htmlInfo = new HtmlInfo();
            htmlInfo.setBody(body);
            messageRepository.setHtml(htmlInfo);

            String[] subjectArray = subject.split("\\[")[1].split(" ");
            String activityId = subjectArray[0];
            String langLocal = subjectArray[1];
            String language = langLocal.split("_")[0];

            messageRepository.setActivityId(activityId);
            messageRepository.setLocale(langLocal);
            messageRepository.setLanguage(language);

        }
        return messageRepository;
    }

    public static JobResponse createJob(JobRequest jobRequest, MasterJobQueries masterJobQueries,
                                        WorkfrontQueries workfrontQueries,
                                        QaRepositoryQueries qaRepositoryQueries,
                                        ProofRepositoryQueries proofRepositoryQueries,
                                        MessageQueries messageQueries,
                                        CgenRepositoryQueries cgenRepositoryQueries,
                                        UserInfoQueries userInfoQueries) {

        JobResponse jobResponse = new JobResponse();

        if(jobRequest.getCgen() == null || jobRequest.getMessages() == null
                || jobRequest.getQas() == null || jobRequest.getWorkfrontInfo() == null
                || jobRequest.getUserId() == null) {

            ResponseError responseError = invalidParameterError();
            jobResponse.setError(responseError);
            return jobResponse;
        }

        MasterJob checkMasterJob = masterJobQueries.getByWorkfrontId(jobRequest.getWorkfrontInfo().getId());
        if(checkMasterJob != null) {
            ResponseError responseError = new ResponseError();
            responseError.setMessage("Job Already Present for this Workfront Id. For opening this job ");
            responseError.setLink("" + checkMasterJob.getId());
            jobResponse.setError(responseError);
            return jobResponse;
        }

        MasterJob masterJob = new MasterJob();
        masterJob.setState(Constants.JobState_InDraft);
        masterJob.setSegmentInfos(jobRequest.getSegmentInfos());

        // inserting workfront info
        WorkfrontRepository workfrontRepository = jobRequest.getWorkfrontInfo();
        WorkfrontRepository insertedWorkfrontRepository = workfrontQueries.get(workfrontRepository.getId());
        if(insertedWorkfrontRepository == null) {
            workfrontQueries.insert(workfrontRepository);
        } else {
            workfrontQueries.update(workfrontRepository);
        }

        masterJob.setWorkfrontId(workfrontRepository.getId());
        masterJob.setName(workfrontRepository.getName());

        // inserting metainfo
        Metadata metadata = new Metadata();
        long currentTime = System.currentTimeMillis();
        metadata.setCreatedAt(currentTime);
        metadata.setLastModifiedAt(currentTime);
        UserInfo userInfo = userInfoQueries.getByGuidID(jobRequest.getUserId());
        metadata.setCreatedById(userInfo.getGuidId());
        metadata.setLastModifiedById(userInfo.getGuidId());
        metadata.setCreatedBy(userInfo.getName());
        metadata.setLastModifiedBy(userInfo.getName());
        masterJob.setMasterJobMetadata(metadata);

        // insert qa and messages
        ArrayList<JobStep> steps = new ArrayList<>();
        masterJob.setSteps(steps);

        // for self qa
        String jobId = createStep(jobRequest.getUserId(), jobRequest.getSegmentInfos(),
                jobRequest.getMessages(), jobRequest.getCgen(), qaRepositoryQueries,
                cgenRepositoryQueries, messageQueries,metadata);

        JobStep jobStepSelf = new JobStep();
        jobStepSelf.setId(jobId);
        jobStepSelf.setSequence(1);
        jobStepSelf.setState("Created");
        jobStepSelf.setType(Constants.QaType);
        jobStepSelf.setRequired(1);
        jobStepSelf.setName(Constants.QALevel_Self);
        steps.add(jobStepSelf);

        String cgenId = qaRepositoryQueries.get(jobId).getCgenId();

        // for other qa's
        ArrayList<UserInfo> qas = jobRequest.getQas();
        int count = 1;
        for(UserInfo qa : qas) {

            JobStep jobStep = new JobStep();

            String assignedId = qa.getGuidId();

            String qaId = createOtherQAStep(assignedId, metadata, qaRepositoryQueries, cgenId);
            jobStep.setId(qaId);
            jobStep.setState("Created");
            jobStep.setRequired(1);
            count++;
            jobStep.setSequence(count);
            int qaNumber = count - 1;
            jobStep.setName(Constants.QALevel_Others + qaNumber);
            jobStep.setType(Constants.QaType);
            steps.add(jobStep);
        }

        // for proofing
        if(jobRequest.getJobType().equalsIgnoreCase("Both")
            || jobRequest.getJobType().equalsIgnoreCase("Proofing")) {

            JobStep proofStep = new JobStep();
            count++;
            proofStep.setSequence(count);
            proofStep.setRequired(1);
            proofStep.setState("Created");
            proofStep.setName(Constants.Proofing_Name);
            proofStep.setType(Constants.Proofing_Type);

            String proofingId = creatProofingStep(metadata, proofRepositoryQueries, cgenId);
            proofStep.setId(proofingId);
            steps.add(proofStep);
        }

        masterJob.setId(createUUID());
        MasterJob insertedMasterJob = masterJobQueries.insert(masterJob);
        jobResponse.setJobId(insertedMasterJob.getId());
        jobResponse.setJobCreated(true);

        return jobResponse;

    }

    public static String creatProofingStep(Metadata metadata,
                                           ProofRepositoryQueries proofRepositoryQueries
            , String cgenId) {

        ProofRepository proofRepository = new ProofRepository();
        String id = createUUID();
        proofRepository.setId(id);
        proofRepository.setStatus(Constants.ProofingItemStatus);
        proofRepository.setCgenId(cgenId);
        proofRepositoryQueries.insert(proofRepository);

        return proofRepository.getId();
    }

    public static String createOtherQAStep(String assignedId, Metadata metadata,
                                           QaRepositoryQueries qaRepositoryQueries, String cgenId) {

        QaRepository qaRepository = new QaRepository();
        qaRepository.setAssignee(assignedId);
        qaRepository.setMetadata(metadata);
        qaRepository.setCgenId(cgenId);
        qaRepository.setId(createUUID());
        QaRepository insertedQaRepository = qaRepositoryQueries.insert(qaRepository);
        return insertedQaRepository.getId();

    }

    public static String createStep(String userId,
                                    ArrayList<JobSegmentInfo> segmentInfo,
                                    ArrayList<JobMessageRequest> messages,
                                    ArrayList<CgenContent> cgen,
                                    QaRepositoryQueries qaRepositoryQueries,
                                    CgenRepositoryQueries cgenRepositoryQueries,
                                    MessageQueries messageQueries,
                                    Metadata metadata) {

        QaRepository qaRepository = new QaRepository();
        qaRepository.setAssignee(userId);
        qaRepository.setMetadata(metadata);
        // for md5
        Gson gson = new Gson();
        String strCgen = gson.toJson(cgen);
        String md5Cgen = null;
        try {
            md5Cgen = convertToMD5(strCgen);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        CgenRepository insertedCgenRepository = cgenRepositoryQueries.getByHash(md5Cgen);
        if(insertedCgenRepository == null) {
            // set cgen
            CgenRepository cgenRepository = new CgenRepository();
            cgenRepository.setContent(cgen);
            cgenRepository.setMd5(md5Cgen);
            cgenRepository.setType(Constants.CgenType);
            cgenRepository.setMetadata(metadata);

            cgenRepository.setId(createUUID());
            insertedCgenRepository = cgenRepositoryQueries.insert(cgenRepository);
        }

        // set cgen id
        qaRepository.setCgenId(insertedCgenRepository.getId());


        // set messages
        ArrayList<QaItem> qaItems = new ArrayList<>();
        qaRepository.setItems(qaItems);

        for(JobMessageRequest jobMessageRequest : messages) {

            QaItem qaItem = new QaItem();
            qaItem.setStatus(Constants.QAItemStatus);

            String md5 = null;
            try {
                md5 = convertToMD5(jobMessageRequest.getBase64());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            MessageRepository insertedMessageRepository = messageQueries.getByHash(md5);
            String messageId = null;

            if(insertedMessageRepository != null) {
                messageId = insertedMessageRepository.getId();
            } else {
                insertedMessageRepository = createMessage(jobMessageRequest, messageQueries, metadata);
                messageId = insertedMessageRepository.getId();
            }
            String messageActivityId = insertedMessageRepository.getActivityId();
            ArrayList<CgenContent> content = insertedCgenRepository.getContent();
            CgenContent foundCgenContent = content.stream().filter(cgenContent ->
                    cgenContent.getActivityId().equals(messageActivityId))
                    .findAny().orElse(null);

            if(foundCgenContent != null) {
                String segmentName = foundCgenContent.getCreativeFileName();
                qaItem.setSegment(segmentName);    //set segment

//                if(segmentInfos != null) {
//                    JobSegmentInfo foundSegmentInfo = segmentInfos.stream().filter(segmentInfo ->
//                            segmentInfo.getSegmentName().equals(segmentName))
//                            .findAny().orElse(null);
//
//                    if(foundSegmentInfo != null) {
//                        qaItem.setBu(foundSegmentInfo.getBu());
//                    }
//                }
            }

            qaItem.setId(messageId);
            qaItems.add(qaItem);
        }

        qaRepository.setId(createUUID());
        QaRepository insertedQaRepository = qaRepositoryQueries.insert(qaRepository);
        return insertedQaRepository.getId();

    }

    public static MessageRepository createMessage(JobMessageRequest jobMessageRequest,
                                       MessageQueries messageQueries,
                                       Metadata metadata) {

        MessageRepository messageRepository = new MessageRepository();
        MessageContent content = new MessageContent();
        content.setType(Constants.MessageFileType);
        content.setBase64(jobMessageRequest.getBase64());
        messageRepository.setMetadata(metadata);

        messageRepository.setContent(content);

//        byte[] msgBytes = Base64.getDecoder().decode(jobMessageRequest.getBase64());
//        MsgParser msgp = new MsgParser();
//        Message msg = null;
//        try {
//            msg = msgp.parseMsg(new ByteArrayInputStream(msgBytes));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if(msg != null) {
//            String body = msg.getConvertedBodyHTML();
//            String md5 = null;
//            try {
//                md5 = convertToMD5(jobMessageRequest.getBase64());
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            }
//            messageRepository.setMd5(md5);
//            String subject = msg.getSubject();
//            String senderName = msg.getFromName();
//            String senderAddress = msg.getFromEmail();

        ParseRequest parseRequest = new ParseRequest();
        parseRequest.setMsgFiles(jobMessageRequest.getBase64());
        Gson gson = new Gson();
        String request = gson.toJson(parseRequest);

        String response = null;
        try {
            response = callPostUrl(Constants.MessageParserUrl, request, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(response != null) {

            ParseResponse parseResponse = gson.fromJson(response, ParseResponse.class);
            String body = parseResponse.getHtmlBody();
            String subject = parseResponse.getSubject();
            String senderName = parseResponse.getSenderName();
            String senderAddress = parseResponse.getSenderAddress();

            String md5 = null;
            try {
                md5 = convertToMD5(jobMessageRequest.getBase64());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            messageRepository.setMd5(md5);

            messageRepository.setSubject(subject);
            messageRepository.setSenderName(senderName);
            messageRepository.setSenderAddress(senderAddress);

            HtmlInfo htmlInfo = new HtmlInfo();
            htmlInfo.setBody(body);
            messageRepository.setHtml(htmlInfo);

            String[] subjectArray = subject.split("\\[")[1].split(" ");
            String activityId = subjectArray[0];
            String langLocal = subjectArray[1];
            String language = langLocal.split("_")[0];

            messageRepository.setActivityId(activityId);
            messageRepository.setLocale(langLocal);
            messageRepository.setLanguage(language);

        }

        messageRepository.setId(createUUID());
        MessageRepository insertedMessageRepository = messageQueries.insert(messageRepository);
        return insertedMessageRepository;
    }

    public static String convertToMD5(String text) throws NoSuchAlgorithmException {
        //CommonFunctions.text = text;

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(text.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
        // Now we need to zero pad it if you actually want the full 32 chars.
        while(hashtext.length() < 32 ){
            hashtext = "0"+hashtext;
        }

        return hashtext;
    }

    public static ResponseError invalidParameterError() {

        ResponseError responseError = new ResponseError();
        responseError.setMessage("Invalid Parameter/s");
        return responseError;
    }


    public static ProofResponse updateProofs(String jobId, String stepId, ProofUpdateRequest proofUpdateRequest,
                                             MasterJobQueries masterJobQueries,
                                             QaRepositoryQueries qaRepositoryQueries,
                                             ProofRepositoryQueries proofRepositoryQueries,
                                             MessageQueries messageQueries,
                                             LanguageRepositoryQueries languageRepositoryQueries) {

        ProofResponse proofResponse = new ProofResponse();

        ProofRepository proofRepository = proofRepositoryQueries.get(stepId);
        ArrayList<ProofItem> items = proofRepository.getItems();
        String language = proofUpdateRequest.getLanguageId();
        List<ProofItem> langItems = items.stream()
                .filter(item -> item.getLanguage().equals(language))
                .collect(Collectors.toList());



        if(proofUpdateRequest.getType() == 1) { // geo reviewers approval
            langItems.stream().forEach(item -> item.
                    setGeoReviewerApproved(proofUpdateRequest.isGeoReviewerApproved()));
            proofResponse.setGeoReviewerApproved(proofUpdateRequest.isGeoReviewerApproved());
        } else if(proofUpdateRequest.getType() == 2) { // pm approval
            langItems.stream().forEach(item -> item.
                    setPmReviewerReviewed(proofUpdateRequest.isPmApproved()));
            proofResponse.setPmApproved(proofUpdateRequest.isPmApproved());
        } else {

            ResponseError error = invalidParameterError();
            proofResponse.setError(error);
        }

        proofRepositoryQueries.update(proofRepository);

        return proofResponse;
    }

    public static ProofResponse getProofDetails(String jobId,
                                                MasterJobQueries masterJobQueries,
                                                QaRepositoryQueries qaRepositoryQueries,
                                                ProofRepositoryQueries proofRepositoryQueries,
                                                MessageQueries messageQueries,
                                                LanguageRepositoryQueries languageRepositoryQueries) {

        ProofResponse proofResponse = new ProofResponse();
        ArrayList<ProofInfo> result = new ArrayList<>();
        proofResponse.setResult(result);

        MasterJob masterJob = masterJobQueries.get(jobId);
        if(masterJob != null) {

          ArrayList<JobStep> steps = masterJob.getSteps();

          JobStep insertedProofStep = steps.stream().filter(step ->
                  step.getType().equalsIgnoreCase(Constants.Proofing_Type))
                  .findFirst().orElse(null);
          if(insertedProofStep == null) {
              ResponseError error = invalidParameterError();
              proofResponse.setError(error);
              return proofResponse;
          }

          ProofRepository insertedProofRepository = proofRepositoryQueries
                  .get(insertedProofStep.getId());

          if(insertedProofRepository.getItems() != null
                && insertedProofRepository.getItems().size() > 0) {

              ArrayList<ProofItem> proofItems = insertedProofRepository.getItems();
              getProofInfo(result, proofItems, languageRepositoryQueries, messageQueries);

              return proofResponse;
          }

          int size = steps.size();
          JobStep insertedLatestQaStep = steps.get(size-2);

          QaRepository insertedLatestQa = qaRepositoryQueries.get(insertedLatestQaStep.getId());
          ArrayList<QaItem> items = insertedLatestQa.getItems();

          ArrayList<ProofItem> proofItems = new ArrayList<>();
          insertedProofRepository.setItems(proofItems);

          for(QaItem item : items) {

              ProofItem proofItem = new ProofItem();
              proofItem.setBu(item.getBu());
              proofItem.setStatus(Constants.ProofingItemStatus);
              proofItem.setId(item.getId());
              proofItem.setSegment(item.getSegment());
              MessageRepository message = messageQueries.get(item.getId());
              proofItem.setLanguage(message.getLanguage());
              proofItems.add(proofItem);
          }

            proofRepositoryQueries.update(insertedProofRepository);
            getProofInfo(result, proofItems, languageRepositoryQueries, messageQueries);


        } else {

            ResponseError error = invalidParameterError();
            proofResponse.setError(error);
        }


        return proofResponse;
    }

    public static void getProofInfo(ArrayList<ProofInfo> result,
                                    ArrayList<ProofItem> proofItems,
                                    LanguageRepositoryQueries languageRepositoryQueries,
                                    MessageQueries messageQueries) {

        Map<String, List<ProofItem>> groupByLanguages = proofItems.stream().
                collect(Collectors.groupingBy(ProofItem :: getLanguage, Collectors.toList()));

        List<ProofItem> ieList = groupByLanguages.containsKey("ie") ? groupByLanguages.get("ie") : null;
        if(ieList != null) {
            groupByLanguages.remove("ie");

            for(ProofItem item : ieList) {
                item.setLanguage("en");
            }

            if(groupByLanguages.containsKey("en")) {

                List<ProofItem> enList = groupByLanguages.get("en");
                enList.addAll(ieList);
            } else {
                groupByLanguages.put("en", ieList);
            }
        }


        Set<String> keys = groupByLanguages.keySet();


        for(String key : keys) {

            ProofInfo proofInfo = new ProofInfo();

            List<ProofItem> proofItem = groupByLanguages.get(key);
            int count = proofItem.size();
            proofInfo.setNumberOfProofs(count);
            proofInfo.setLanguage(key);
            LanguageCodeRepository languageCodeRepository = languageRepositoryQueries.get(key);
            if(languageCodeRepository != null) {
                proofInfo.setLanguageLabel(languageCodeRepository.getLanguageName());
            }

            ArrayList<ProofMessageInfo> messageDetails = new ArrayList<>();
            proofInfo.setMessageDetails(messageDetails);
            int size = 0;
            for(ProofItem item : proofItem) {


                ProofMessageInfo msgInfo = new ProofMessageInfo();
                messageDetails.add(msgInfo);
                MessageRepository messageRepository = messageQueries.get(item.getId());
                int length = messageRepository.getContent().getBase64().length();

                int fileSize = (int) ((4 * Math.ceil((length / 3))*0.5624896334383812)/1000) + 20;
                msgInfo.setSubjectLine(messageRepository.getSubject());
                msgInfo.setLocale(messageRepository.getLocale());
                msgInfo.setActivityId(messageRepository.getActivityId());

                proofInfo.setStatus(item.getStatus());
                proofInfo.setGeoReviewerApproved(item.isGeoReviewerApproved());
                proofInfo.setPmApproved(item.isPmReviewerReviewed());
                size = size + fileSize;
            }
            proofInfo.setSize(size);
            proofInfo.setType("Message");
            result.add(proofInfo);

        }


//        Map<String, Long> groupByLanguages = proofItems.stream().
//                collect(Collectors.groupingBy(ProofItem :: getLanguage, Collectors.counting()));
//        Set<String> keys = groupByLanguages.keySet();
//
//        for(String key : keys) {
//
//            ProofInfo proofInfo = new ProofInfo();
//            Long count = groupByLanguages.get(key);
//            proofInfo.setNumberOfProofs(count);
//            proofInfo.setSize((int) (21 * count));
//            proofInfo.setLanguage(key);
//            proofInfo.setType("Message");
//            result.add(proofInfo);
//        }


    }


    public static String callGetUrl(String url) {

        OkHttpClient client = new OkHttpClient();
        client.newBuilder().callTimeout(2, TimeUnit.MINUTES).readTimeout(2, TimeUnit.MINUTES);

        Request request = new Request.Builder().url(url).get().build();

        Response response;
        String workfrontResponseStr = null;

        try {
            response = client.newCall(request).execute();
            workfrontResponseStr = response.body().string();

            // System.out.print(response.body().string());
        } catch (IOException e) {
            // e.printStackTrace();
        }

        return workfrontResponseStr;

    }

    public static String getWorkFrontKey() {

        String workfrontId = "?apiKey=" + "8c1nj3coyzxr1wxl0rel9fj28a6mbt9f";
        return workfrontId;
    }

    public static ProjectInfo getProjectsFromWorkfront(String url, ArrayList<String> customFormList) {

        String workFrontResponseStr = callGetUrl(url);
        // wrong project key {"error":{"message":"APIModel V10_0 does not support
        // namedQuery 5bce60a30020fe313c0e337650b0b4e (PROJ)"}}
        Gson gson = new Gson();
        WorkfrontServerResponse workfrontResponse = null;
        ProjectInfo projectInfo = null;

        String filteredWorkFrontResponseStr = filterWorkfrontResponse(workFrontResponseStr, customFormList);
        workfrontResponse = gson.fromJson(filteredWorkFrontResponseStr, WorkfrontServerResponse.class);

        if ((workfrontResponse != null) && (workfrontResponse.getError() == null)) {

            projectInfo = getCampaignInfo(workfrontResponse);
            // campaignService.insertOrUpdateCampaign(campaignInfo);
        }

        return projectInfo;

    }

    public static ResponseError getInvalidParametersError() {

        ResponseError responseError = new ResponseError();
        responseError.setErrorCode(1);
        responseError.setMessage("Invalid Parameters");

        return responseError;
    }

    public static ProjectInfo getCampaignInfo(WorkfrontServerResponse workfrontResponse) {

        String workfrontKey = getWorkFrontKey();//CommonFunctions.getWorkfrontKey(integerationInfoService);
        ProjectInfo projectInfo = new ProjectInfo();
        WorkfrontResponseData workfrontResponseData = workfrontResponse.getData();

        ProjectInfo insertedCampaignInfo = null;//campaignService.get(workfrontResponseData.getID());

        projectInfo.setId(workfrontResponseData.getID());
        projectInfo.setName(workfrontResponseData.getName());
        // projectInfo.setWorkfrontOwner(workfrontResponseData.getOwner());
        projectInfo.setPlannedCompletionDate(workfrontResponseData.getPlannedCompletionDate());
        projectInfo.setEnteredByID(workfrontResponseData.getEnteredByID());

        if (!isEmpty(insertedCampaignInfo) && !isEmpty(insertedCampaignInfo.getEnteredBy())
                && insertedCampaignInfo.getEnteredByID().equals(workfrontResponseData.getEnteredByID())) {

            projectInfo.setEnteredBy(insertedCampaignInfo.getEnteredBy());
            projectInfo.setEnteredByEmail(insertedCampaignInfo.getEnteredByEmail());
        } else {

            // call for entered by user name
            String enteredByNameResp = callGetUrl(
                    Constants.workfrontUserUrl + workfrontResponseData.getEnteredByID() + workfrontKey);

            JSONObject object;
            String enetredByStr = null;
            String enteredByEmailStr = null;
            try {
                object = new JSONObject(enteredByNameResp);
                JSONObject dataObject = object.getJSONObject("data");
                enetredByStr = dataObject.getString("name");
                enteredByEmailStr = dataObject.getString("emailAddr");

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            projectInfo.setEnteredBy(enetredByStr);
            projectInfo.setEnteredByEmail(enteredByEmailStr);

            // enteredByNameJson.get("data").getAsJsonObject("");
        }

        String status = getStatus(workfrontResponseData.getStatus());
        projectInfo.setStatus(status);//workfrontResponseData.getStatus());
        projectInfo.setDescription(workfrontResponseData.getDescription());

        // if projectRequestCreated is missing workaround
        if(!isEmpty(workfrontResponseData.getParameterValues())
                && isEmpty(workfrontResponseData.getParameterValues().getProjectRequestCreated())) {

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");
            LocalDateTime now = LocalDateTime.now();
            workfrontResponseData.getParameterValues().setProjectRequestCreated(dtf.format(now));
        }

        projectInfo.setWorkfrontCustomForm(workfrontResponseData.getParameterValues());

        projectInfo.setTemplateID(workfrontResponseData.getTemplateID());
        if (!isEmpty(insertedCampaignInfo) && !isEmpty(insertedCampaignInfo.getTemplateValue())
                && insertedCampaignInfo.getTemplateID().equals(workfrontResponseData.getTemplateID())) {

            projectInfo.setTemplateValue(insertedCampaignInfo.getTemplateValue());
        } else {
            // get template name
            String enteredByNameResp = callGetUrl(
                    Constants.workfrontTemplateUrl + workfrontResponseData.getTemplateID() + workfrontKey);

            JSONObject object;
            String templateName = null;
            try {
                object = new JSONObject(enteredByNameResp);
                JSONObject dataObject = object.getJSONObject("data");
                templateName = dataObject.getString("name");

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            projectInfo.setTemplateValue(templateName);

        }

        // campaignInfo.setTemplateValue(workfrontResponseData.getT);
        // acsActivityOrDepOwner

        String programMangerId = workfrontResponseData.getOwner().getID();

        // get template name
        String ownerResponse = callGetUrl(Constants.workfrontUserUrl + programMangerId + workfrontKey);

        JSONObject objectJson;
        String ownerEmailId = null;
        try {
            objectJson = new JSONObject(ownerResponse);
            JSONObject dataObject = objectJson.getJSONObject("data");

            ownerEmailId = dataObject.getString("emailAddr");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        workfrontResponseData.getOwner().setEmailId(ownerEmailId);
        projectInfo.setWorkfrontOwner(workfrontResponseData.getOwner());

        projectInfo.setWorkfrontSponsor(workfrontResponseData.getSponsor().getName());


        String acsOwnerId = getAcsOwnerId(workfrontResponseData.getTasks());

        projectInfo.setAssignedUserId(acsOwnerId);

        if (!isEmpty(insertedCampaignInfo)
                && !isEmpty(insertedCampaignInfo.getAssignedUserName())
                && insertedCampaignInfo.getAssignedUserId().equals(acsOwnerId)) {

            projectInfo.setAssignedUserName(insertedCampaignInfo.getAssignedUserName());
            projectInfo.setAssignedUseEmail(insertedCampaignInfo.getAssignedUseEmail());
        } else {

            // get template name
            String enteredByNameResp = callGetUrl(Constants.workfrontUserUrl + projectInfo.getAssignedUserId() + workfrontKey);

            JSONObject object;
            String assignedName = null;
            String assignedEmailId = null;
            try {
                object = new JSONObject(enteredByNameResp);
                JSONObject dataObject = object.getJSONObject("data");
                assignedName = dataObject.getString("name");
                assignedEmailId = dataObject.getString("emailAddr");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            projectInfo.setAssignedUserName(assignedName);
            projectInfo.setAssignedUseEmail(assignedEmailId);

        }

        return projectInfo;
    }

    public static boolean isEmpty(String string) {
        if(string == null || string.length() == 0) {
            return true;
        }

        return false;
    }

    private static String getAcsOwnerId(ArrayList<WorkfrontTasks> tasks) {

        for (WorkfrontTasks task : tasks) {

            if (task.getName().equals(Constants.ACSTaskName)) {
                return task.getAssignedToID();
            }
        }

        return null;
    }

    private static String getStatus(String status) {

//		DED - Cancelled - negative
//		CPL - Completed - Active
//		PLN - Plan Setup - yellow
//		ONH - Additional info needed - fuchsia
//		CUR - In Progress - positive
//		YSP - Live - seafoam (edited)

        String statusShow;
        if (status != null) {
            if (status.equals("DED")) {

                statusShow = "Cancelled";
            } else if (status.equals("CPL")) {

                statusShow = "Completed";
            } else if (status.equals("PLN")) {

                statusShow = "Plan Setup";
            } else if (status.equals("ONH")) {

                statusShow = "Additional info needed";
            } else if (status.equals("CUR")) {

                statusShow = "In Progress";
            } else if (status.equals("YSP")) {

                statusShow = "Live";
            } else {
                statusShow = status;
            }
        } else {

            statusShow = status;
        }

        return statusShow;
    }

    private static String filterWorkfrontResponse(String workfrontResponse, ArrayList<String> customFormList) {
        JSONObject jsonObject;

        try {

            jsonObject = new JSONObject(workfrontResponse);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONObject parameterValuesObject = dataObject.getJSONObject("parameterValues");

            for (String value : customFormList) {
                checkAndAddParameter(parameterValuesObject, value);
            }



        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return jsonObject.toString();
    }


    private static void checkAndAddParameter(JSONObject parameterValuesObject, String parameterName)
            throws JSONException {
        JSONArray jsonArray = null;
        String valString = parameterValuesObject.optString(parameterName, null);

        if ((valString != null) && (parameterValuesObject.get(parameterName) instanceof JSONArray)) {
            jsonArray = parameterValuesObject.getJSONArray(parameterName);
            String valueString = getString(jsonArray);

            parameterValuesObject.remove(parameterName);
            parameterValuesObject.put(parameterName, valueString);

        }
    }

    private static String getString(JSONArray jsonArray) {

        StringBuffer stringBuffer = new StringBuffer();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {

                if (i != 0) {

                    stringBuffer.append("," + jsonArray.getString(i));

                } else {
                    stringBuffer.append(jsonArray.get(i));
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            stringBuffer.append(",");
        }

        return stringBuffer.toString();

    }


    public static String callPostUrl(String url, String bodyRequest, String authorizationKey) throws IOException {

        OkHttpClient client = new OkHttpClient();
        client.newBuilder().callTimeout(2, TimeUnit.MINUTES).readTimeout(2, TimeUnit.MINUTES);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, bodyRequest);
        Request request = null;
        if(authorizationKey != null) {
            request = new Request.Builder().url(url).post(body).addHeader("authorization", authorizationKey)
                    .addHeader("Content-Type", "application/json").addHeader("cache-control", "no-cache").build();
        } else {

            request = new Request.Builder().url(url).post(body).addHeader("Content-Type", "application/json")
                    .addHeader("cache-control", "no-cache").build();
        }
        Response response = client.newCall(request).execute();
        String workfrontResponseStr = response.body().string();

        return workfrontResponseStr;
    }


    public static boolean isEmpty(Object object) {
        if(object == null) {
            return true;
        }

        // in this method we should be checking for the instance type as well
        // because it may be called on reference type than actual instance type

        if(object instanceof String) {
            return isEmpty((String) object);
        }

        if(object instanceof Collection) {
            return isEmpty((Collection) object);
        }

        if(object instanceof Map) {
            return isEmpty((Map) object);
        }

        if(object instanceof int[]) {
            return isEmpty((int[]) object);
        }

        if(object instanceof byte[]) {
            return isEmpty((byte[]) object);
        }

        if(object instanceof char[]) {
            return isEmpty((char[]) object);
        }

        if(object instanceof short[]) {
            return isEmpty((short[]) object);
        }

        if(object instanceof long[]) {
            return isEmpty((long[]) object);
        }

        if(object instanceof float[]) {
            return isEmpty((float[]) object);
        }

        if(object instanceof double[]) {
            return isEmpty((double[]) object);
        }

        return false;
    }


    public static String createUUID() {

        UUID id = UuidUtil.getTimeBasedUuid();
        String idStr = id.toString();
       // System.out.println("unique Id : " + idStr);
        return idStr;
    }
}
