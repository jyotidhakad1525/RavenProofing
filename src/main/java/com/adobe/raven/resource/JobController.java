package com.adobe.raven.resource;


import com.adobe.raven.Constants;
import com.adobe.raven.MailUtils;
import com.adobe.raven.Utils;
import com.adobe.raven.db.queries.*;
import com.adobe.raven.dto.ResponseError;
import com.adobe.raven.dto.cgen.CgenRepository;
import com.adobe.raven.dto.job.JobSegmentInfo;
import com.adobe.raven.dto.job.JobStep;
import com.adobe.raven.dto.job.MasterJob;
import com.adobe.raven.dto.message.JobMessageRequest;
import com.adobe.raven.dto.message.MessageRepository;
import com.adobe.raven.dto.proof.ProofItem;
import com.adobe.raven.dto.proof.ProofRepository;
import com.adobe.raven.dto.proof.ProofUpdateRequest;
import com.adobe.raven.dto.qa.QaItem;
import com.adobe.raven.dto.qa.QaRepository;
import com.adobe.raven.dto.user.UserInfo;
import com.adobe.raven.dto.workfront.ProjectInfo;
import com.adobe.raven.dto.workfront.WorkfrontRepository;
import com.adobe.raven.request.JobRequest;
import com.adobe.raven.request.ProofRequest;
import com.adobe.raven.request.WorkfrontRequest;
import com.adobe.raven.response.JobResponse;
import com.adobe.raven.response.ProofResponse;
import com.adobe.raven.response.WorkfrontResponse;
import com.adobe.raven.service.interfaces.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/job")
public class JobController {

	@Autowired
	private MasterJobQueries masterJobQueries;

	@Autowired
	private CgenRepositoryQueries cgenRepositoryQueries;

	@Autowired
	private MessageQueries messageQueries;

	@Autowired
	private QaRepositoryQueries qaRepositoryQueries;

	@Autowired
	private UserInfoQueries userInfoQueries;

	@Autowired
	private WorkfrontQueries workfrontQueries;

	@Autowired
	private GeoReviewerQueries geoReviewerQueries;

	@Autowired
	private ArrayList<String> customFormList;

	@Autowired
	private ProofRepositoryQueries proofRepositoryQueries;

	@Autowired
	private LanguageRepositoryQueries languageRepositoryQueries;

	@Autowired
	private ReshareQAInfoRepository reshareQAInfoRepository;

	@Autowired
	private JobService jobService;


	@RequestMapping(value="/hello", method = RequestMethod.GET)
	public String sayHello() {
		
		return "Hello!! I am live";
	}

	@CrossOrigin
	@RequestMapping(value="/id/{jobId}", method=RequestMethod.GET)
	public JobResponse getJobInfo(@PathVariable String jobId) {

		JobResponse jobResponse = new JobResponse();
		MasterJob masterJob = masterJobQueries.get(jobId);
		WorkfrontRepository workfrontRepository = workfrontQueries.get(masterJob.getWorkfrontId());
		masterJob.setWorkfrontInfo(workfrontRepository);
		ArrayList<JobSegmentInfo> segmentInfoArrayList = new ArrayList<>();
		JobSegmentInfo segmentInfo = new JobSegmentInfo();
		ArrayList<JobStep> jobSteps = masterJob.getSteps();
		for(JobStep step : jobSteps) {

			if(step.getType().equalsIgnoreCase("QA")) {

				QaRepository qaRepository = qaRepositoryQueries.get(step.getId());
				String cgenId = qaRepository.getCgenId();
				CgenRepository cgenRepository = cgenRepositoryQueries.get(cgenId);
				cgenRepository.setMetadata(null);
				step.setCgenInfo(cgenRepository);
				List<QaItem> qaItemList = qaRepository.getItems();
				if(qaItemList != null && !qaItemList.isEmpty()) {
					segmentInfoArrayList = new ArrayList<>();
					for (QaItem item : qaItemList) {

						segmentInfo.setBu(item.getBu());
						segmentInfo.setSegmentName(item.getSegment());
						segmentInfoArrayList.add(segmentInfo);
					}
				}
				step.setSegmentInfoList(segmentInfoArrayList);
				UserInfo userInfo = userInfoQueries.getByGuidID(qaRepository.getAssignee());
				step.setUserInfo(userInfo);
			} else if(step.getType().equalsIgnoreCase(Constants.Proofing_Type)) {

				ProofRepository proofingRepository = proofRepositoryQueries.get(step.getId());
				String cgenId = proofingRepository.getCgenId();
				CgenRepository cgenRepository = cgenRepositoryQueries.get(cgenId);
				cgenRepository.setMetadata(null);
				step.setCgenInfo(cgenRepository);
			}
		}


		jobResponse.setJobResult(masterJob);
		return jobResponse;
	}

	@CrossOrigin
	@RequestMapping(value="/id/{jobId}/sendProofs", method = RequestMethod.POST)
	public ProofResponse sendProofs(@PathVariable String jobId,
									@RequestBody ProofRequest proofRequest) {

		ProofResponse response = MailUtils.sendMails(jobId, proofRequest,
				masterJobQueries, proofRepositoryQueries,
				userInfoQueries, workfrontQueries,geoReviewerQueries,messageQueries, languageRepositoryQueries);



		return response;
	}



	@CrossOrigin
	@RequestMapping(value="/id/{jobId}/sendQaRetestProofs", method = RequestMethod.POST)
	public ProofResponse sendQaRetestProofs(@PathVariable String jobId,
									@RequestBody ProofRequest proofRequest) {

		ProofResponse response = jobService.sendQaRetestMails(jobId, proofRequest);

		return response;
	}

	@CrossOrigin
	@RequestMapping(value="/id/{jobId}/step/{stepId}", method = RequestMethod.POST)
	public ProofResponse updateProofStatus(@PathVariable String jobId,
										   @PathVariable String stepId,
										   @RequestBody ProofUpdateRequest proofUpdateRequest) {

		ProofResponse proofResponse = Utils.updateProofs(jobId, stepId, proofUpdateRequest, masterJobQueries,
				qaRepositoryQueries, proofRepositoryQueries, messageQueries, languageRepositoryQueries);
		return proofResponse;
	}


	@CrossOrigin
	@RequestMapping(value="/id/{jobId}/proofDetails", method = RequestMethod.GET)
	public ProofResponse getProofsDetails(@PathVariable String jobId) {

		ProofResponse proofResponse = Utils.getProofDetails(jobId, masterJobQueries,
				qaRepositoryQueries, proofRepositoryQueries, messageQueries, languageRepositoryQueries);
		return proofResponse;
	}

	@CrossOrigin
	@RequestMapping(value = "/id/{jobId}/proofMessageDetails/{language}", method = RequestMethod.GET)
	public ProofResponse getLanguageMessages(@PathVariable String jobId,
											 @PathVariable String language) {
		ProofResponse proofResponse = new ProofResponse();

		MasterJob masterJob = masterJobQueries.get(jobId);
		if(masterJob == null) {
			ResponseError error = Utils.getInvalidParametersError();
			proofResponse.setError(error);
		}

		JobStep proofJobStep = masterJob.getSteps().stream().filter(jobStep -> jobStep.getType()
				.equalsIgnoreCase(Constants.Proofing_Type)).findAny().orElse(null);

		ArrayList<MessageRepository> messages = new ArrayList<>();
		if(proofJobStep != null) {
			ProofRepository proofRepository = proofRepositoryQueries.get(proofJobStep.getId());
			ArrayList<ProofItem> items = proofRepository.getItems();

			List<ProofItem> languageProofItems = items.stream().filter(proofItem ->
					proofItem.getLanguage().equalsIgnoreCase(language)).collect(Collectors.toList());

			for(ProofItem proofItem : languageProofItems) {

				MessageRepository messageRepository = messageQueries.get(proofItem.getId());
				messageRepository.setMetadata(null);
				messages.add(messageRepository);
			}

			proofResponse.setMessages(messages);

		} else {
			ResponseError error = Utils.getInvalidParametersError();
			proofResponse.setError(error);
		}

		return proofResponse;
	}

	@CrossOrigin
	@RequestMapping(value="/checkMsg", method=RequestMethod.POST)
	public JobResponse updateJob(@RequestBody JobRequest jobRequest) {

		JobResponse jobResponse = new JobResponse();
		JobMessageRequest request = new JobMessageRequest();
		request.setBase64(jobRequest.getBase64());
		Utils.parseMessageRepository(request);
		//Utils.parseMessage(jobRequest.getBase64());

		return jobResponse;
	}

	@CrossOrigin
	@RequestMapping(value="/id/{jobId}", method=RequestMethod.POST)
	public JobResponse updateJob(@PathVariable String jobId, @RequestBody JobRequest jobRequest) {

		JobResponse jobResponse;

		jobResponse = Utils.updateJob(jobId, jobRequest, masterJobQueries,
				 qaRepositoryQueries, proofRepositoryQueries, workfrontQueries,
				messageQueries, cgenRepositoryQueries,userInfoQueries);

		return jobResponse;

	}

	@CrossOrigin
	@RequestMapping(value="/verifyContent", method=RequestMethod.POST)
	public JobResponse verifyContent(@RequestBody JobRequest jobRequest) {

		JobResponse jobResponse = Utils.verifyContent(jobRequest, messageQueries);
		return jobResponse;
	}

	@CrossOrigin
	@RequestMapping(value="/create", method=RequestMethod.POST)
	public JobResponse createJob(@RequestBody JobRequest jobRequest) {

		JobResponse jobResponse = Utils.createJob(jobRequest, masterJobQueries,
												workfrontQueries, qaRepositoryQueries,
												proofRepositoryQueries, messageQueries,
												cgenRepositoryQueries, userInfoQueries);

		return jobResponse;
	}


	@CrossOrigin
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public JobResponse getJobs() {

		JobResponse jobResponse = new JobResponse();
		List<MasterJob> results = masterJobQueries.getAllMasterJobs();
		jobResponse.setResult(results);

		return jobResponse;
	}

	@CrossOrigin
	@RequestMapping(value="/workfrontInfo", method=RequestMethod.POST)
	public WorkfrontResponse getWorkfrontInfo(@RequestBody WorkfrontRequest workfrontRequest) {

		String workfrontId = Utils.getWorkFrontKey(); //CommonFunctions.getWorkfrontKey();
		String url = Constants.workfrontProjectUrl + workfrontRequest.getProjectId() + workfrontId
				+ Constants.workfrontFields;

		// work around as workfront key not working
		ProjectInfo projectInfo = Utils.getProjectsFromWorkfront(url, customFormList);

		WorkfrontResponse projectResponse = new WorkfrontResponse();

		if (projectInfo != null) {

			projectResponse.setProjectInfo(projectInfo);
		} else {

			ResponseError responseError = Utils.getInvalidParametersError();//OrigamiCommonFunctions.getInvalidCampiagnIdError();
			projectResponse.setError(responseError);
		}

		return projectResponse;
	}
}
