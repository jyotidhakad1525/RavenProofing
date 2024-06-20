package com.adobe.raven;

public class Constants {

    public static String MessageParserUrl = "http://10.42.69.206:5000/parse";

    public static String CgenType = "application/json";
    public static String QAItemStatus = "Created";
    public static String ProofingItemStatus = "Created";
    public static String MessageFileType = "application/vnd.ms-outlook";

    public static String JobState_InDraft = "In Draft";
    public static String QALevel_Self = "Self QA";
    public static String QALevel_Others = "QA Level ";

    public static String Proofing_Name = "PROOF";
    public static String Proofing_Type = "actionProof";
    public static String QaType = "QA";

    // for prod
    public static String JobPage = "https://raven.corp.adobe.com/projects/";

    // for stage
  //  public static String JobPage = "https://stage.raven.corp.adobe.com/projects/";

    public static String workfrontUrl = "https://adoberm.my.workfront.com/attask/api/v10.0/";
    public static String workfrontUserUrl = workfrontUrl + "user/";
    public static String workfrontTemplateUrl = workfrontUrl + "template/";
    public static String workfrontProjectUrl = workfrontUrl + "project/";

    public static String ACSTaskName = "ACS Activity/Deployment";

    public static String MailBody = "Hi {{ReviewerName}},\n" +
            "\n" +
            "Hope you’re having a good day! \n" +
            "\n" +
            "Please review the {{languageName}} proofs for {{campaignName}}. The deployment date for the program is {{deploymentDate}}." +
            " Please share your feedback for the attached proofs at the earliest. \n" +
            "\n" +
            "Thanks & Regards,\n" +
            "\n" +
            "{{createdUser}}";

    public static String NextQaMailBody = "Hi {{QaName}},\n" +
            "\n" +
            "Hope you’re having a good day! \n" +
            "\n" +
            "{{QaCurrentLevel}} have been completed in Raven. Please do the {{QaNextLevel}} for the proofs of {{campaignName}}. The deployment date for the program is {{deploymentDate}}." +
            "\n" +
            "\nThanks & Regards,\n" +
            "\n" +
            "Raven Team";

    public static String NextProofMailBody = "Hi {{userName}},\n" +
            "\n" +
            "Hope you’re having a good day! \n" +
            "\n" +
            "{{QaCurrentLevel}} have been completed in Raven. Proofs of {{campaignName}} are ready to be sent to geo reviewers. The deployment date for the program is {{deploymentDate}}." +
            "\n" +
            "\nThanks & Regards,\n" +
            "\n" +
            "Raven Team";

    public static String workfrontFields = "&fields=owner,plannedCompletionDate,"
            + "enteredByID,status,description,sponsor,parameterValues,templateID,tasks,tasks:assignedToID";

    public static String ErrorActivityIdMissing = "For given CGEN Activity Id missing for row number {{rowNumber}}";
    public static String ErrorCreativeFileNameMissing = "For given CGEN Creative Filename missing for row number {{rowNumber}}";
    public static String ErrorActivityIdMissingInCgen = "Activity Id {{activityId}} is missing in Cgen";
    public static String ErrorActivityIdMissingInSelfQa = "Activity Id {{activityId}} is not uploaded while doing SelfQa.";
    public static String ErrorActivityIdMissingInCurrentQa = "Activity Id {{activityId}} is not uploaded.";


    public static String WarningMissingActivityIdMsgFile = "Activity Id {{activityId}} Content file is missing.";

    public static String UserAlreadyPresent="User is Already Present";

    public static String UserDeleted="User Deleted Successfully";

    public static String UserNotFound="User Not Found";
}
