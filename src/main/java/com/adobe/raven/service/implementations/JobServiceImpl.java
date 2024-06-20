package com.adobe.raven.service.implementations;

import com.adobe.raven.Constants;
import com.adobe.raven.MailUtils;
import com.adobe.raven.db.queries.*;
import com.adobe.raven.dto.LanguageCodeRepository;
import com.adobe.raven.dto.cgen.CgenContent;
import com.adobe.raven.dto.cgen.CgenRepository;
import com.adobe.raven.dto.geoReviewer.GeoReviewer;
import com.adobe.raven.dto.job.JobStep;
import com.adobe.raven.dto.job.MasterJob;
import com.adobe.raven.dto.message.MessageRepository;
import com.adobe.raven.dto.proof.MailChains;
import com.adobe.raven.dto.qa.QaItem;
import com.adobe.raven.dto.qa.QaRepository;
import com.adobe.raven.dto.qa.ReshareQaRepository;
import com.adobe.raven.dto.user.UserInfo;
import com.adobe.raven.dto.workfront.WorkfrontRepository;
import com.adobe.raven.request.ProofRequest;
import com.adobe.raven.response.ProofResponse;
import com.adobe.raven.service.interfaces.JobService;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.Session;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {
    @Autowired
    MasterJobQueries masterJobQueries;
    @Autowired
    ProofRepositoryQueries proofRepositoryQueries;
    @Autowired
    UserInfoQueries userInfoQueries;
    @Autowired
    WorkfrontQueries workfrontQueries;
    @Autowired
    GeoReviewerQueries geoReviewerQueries;
    @Autowired
    MessageQueries messageQueries;
    @Autowired
    LanguageRepositoryQueries languageRepositoryQueries;
    @Autowired
    QaRepositoryQueries qaRepositoryQueries;
    @Autowired
    ReshareQAInfoRepository reshareQAInfoRepository;

    @Autowired
    CgenRepositoryQueries cgenRepositoryQueries;

    @Override
    public  ProofResponse sendQaRetestMails(String jobId, ProofRequest proofRequest) {

        ProofResponse proofResponse = new ProofResponse();
        MasterJob masterJob = masterJobQueries.get(jobId);

 //       String guIdID = masterJob.getMasterJobMetadata().getLastModifiedById();
        UserInfo createdUserInfo = userInfoQueries.getByGuidID(proofRequest.getGuidId());
        WorkfrontRepository workfrontRepository = workfrontQueries.get(masterJob.getWorkfrontId());

        ArrayList<JobStep> steps = masterJob.getSteps();

        JobStep jobStep = steps.stream().filter(step -> step
                        .getName().equalsIgnoreCase(Constants.QALevel_Self))
                .findAny().orElse(null);

        QaRepository qaRepository = qaRepositoryQueries.get(jobStep.getId());
        ArrayList<QaItem> qaItems = qaRepository.getItems();
        List<String> qaItemsId = qaItems.stream().map(x -> x.getId()).collect(Collectors.toList());

        CgenRepository cgenRepository=cgenRepositoryQueries.get(qaRepository.getCgenId());
        ArrayList<CgenContent> cgenContents=cgenRepository.getContent();

        for (String msgId : proofRequest.getMessagesIds()) {

            if(!qaItemsId.contains(msgId)){
                ReshareQaRepository reshareQaRepository = reshareQAInfoRepository.get(msgId);
                QaItem qaItem = new QaItem();
                qaItem.setId(reshareQaRepository.getId());
                qaItem.setStatus(reshareQaRepository.getStatus());
                MessageRepository messageRepository=messageQueries.get(msgId);
                String messageActivityId=messageRepository.getActivityId();
                CgenContent foundCgenContent = cgenContents.stream().filter(cgenContent ->
                                cgenContent.getActivityId().equals(messageActivityId))
                        .findAny().orElse(null);

                if(foundCgenContent != null) {
                    String  cgenSegmentName=foundCgenContent.getCreativeFileName();
                    qaItem.setSegment(cgenSegmentName);
                    QaItem qaItemEntitys= qaItems.stream().filter(qaItemEntity ->
                                    qaItemEntity.getSegment().equals(cgenSegmentName))
                            .findAny().orElse(null);
                    try{
                        qaItem.setSegment(qaItemEntitys.getBu());
                    }
                    catch(NullPointerException e)
                    {
                        e.printStackTrace();
                    }
                }
                qaItem.setCheckList(reshareQaRepository.getCheckList());
                Boolean result = qaRepositoryQueries.insertIteam(jobStep.getId(), qaItem);
            }

            //           }
        }
        ArrayList<MessageRepository> messages = new ArrayList<>();
        QaRepository qaRepositorys = qaRepositoryQueries.get(jobStep.getId());
        ArrayList<QaItem> qaItemss = qaRepositorys.getItems();
        List<String> qaItemsIds = qaItems.stream().map(x -> x.getId()).collect(Collectors.toList());

        for (String qaItemId : qaItemsIds) {

            MessageRepository messageRepository = messageQueries.get(qaItemId);
            messages.add(messageRepository);
        }

       ArrayList<String> toList= proofRequest.getTo();
        GeoReviewer GeoReviewer=geoReviewerQueries.getByEmail(toList.get(0));
        LanguageCodeRepository languageCodeRepository=languageRepositoryQueries.get(GeoReviewer.getLanguage());

        ArrayList<String> mailIds=new ArrayList<>();
         mailIds.addAll(proofRequest.getTo());
         HashSet<String> name=new HashSet<>();

        for(String mailId:mailIds)
        {
            name.add(geoReviewerQueries.getByEmail(mailId).getName());
        }
//        List<GeoReviewer> geoReviewers=geoReviewerQueries.getByEmails(mailIds);

        String names= name.stream().collect(Collectors.joining("/ "));

        ArrayList<String> ccLists=new ArrayList<String>();
        ccLists.addAll(proofRequest.getCc());
        ccLists.add("proofs@adobe.com");

        if (createdUserInfo != null) {
            ccLists.add(createdUserInfo.getEmailId());
        }

        MailChains mailChains = new MailChains();
        mailChains.setAttachments(messages);
        mailChains.setToList(MailUtils.changeListToString(proofRequest.getTo()));
        mailChains.setCcList(MailUtils.changeListToString(ccLists));
        mailChains.setSubject(proofRequest.getSubject());

//          GeoReviewer geoReviewer = geoReviewerQueries.getByEmail(reviewer.getSendList().get(0));

        String body = Constants.MailBody
                .replaceAll("\\{\\{ReviewerName\\}\\}", names)
                .replaceAll("\\{\\{createdUser\\}\\}", createdUserInfo.getName())
                .replaceAll("\\{\\{campaignName\\}\\}", masterJob.getName())
                .replaceAll("\\{\\{deploymentDate\\}\\}", workfrontRepository.getDeploymentDate())
                .replaceAll("\\{\\{languageName\\}\\}",languageCodeRepository.getLanguageName());


        mailChains.setBody(body);
        Session session = MailUtils.outlookAuthenticate();
        boolean isSent = MailUtils.sendLanguageMail(mailChains, session);
        proofResponse.setProofSent(isSent);

        return proofResponse;
    }
}
