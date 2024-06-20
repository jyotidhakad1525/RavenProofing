package com.adobe.raven.service.interfaces;

import com.adobe.raven.request.ProofRequest;
import com.adobe.raven.response.ProofResponse;

public interface JobService {

    public  ProofResponse sendQaRetestMails(String jobId, ProofRequest proofRequest);

}
