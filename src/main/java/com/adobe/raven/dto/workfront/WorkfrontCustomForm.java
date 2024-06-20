package com.adobe.raven.dto.workfront;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

public @Data
class WorkfrontCustomForm {

    @SerializedName("DE:Selected Regions")
    private String selectedRegions;

    @SerializedName("DE:Deployment Flexibility")
    private String deploymentFlexibility;

    @SerializedName("DE:Reference Prior Job#/LR")
    private String referencePriorJob;

    @SerializedName("DE:Are all the links live?")
    private String linksLive;

    @SerializedName("DE:Primary Region")
    private String primaryRegion;

    @SerializedName("DE:Funnel Status")
    private String funnelStatus;

    @SerializedName("DE:Deploy VIA/LR")
    private String deployVL;

    @SerializedName("DE:Business Group")
    private String businessGroupL;

    @SerializedName("DE:Region")
    private String region;

    @SerializedName("DE:Does this request have more than 1 email drop date?")
    private String emailDropDate;

    @SerializedName("DE:Deployment Team")
    private String deploymentTeam;

    @SerializedName("DE:Lifecycle Marketing Request Type")
    private String lifecycleMarket;

    @SerializedName("DE:Planned Project")
    private String plannedProject;

    @SerializedName("DE:CGEN Program ID")
    private String cgenProgramId;

    @SerializedName("DE:Report Cat - THOR Notifications")
    private String reportCat;

    @SerializedName("DE:EMEA Standard Seed List")
    private String eStandardSeedList;

    @SerializedName("DE:Campaign Name")
    private String campaignName;

    @SerializedName("DE:Complexity")
    private String complexity;

    @SerializedName("DE:Deployment Date/Time2")
    private String deploymentDateTime2;

    @SerializedName("DE:Does this campaign include personalization?")
    private String campaignIncPersonalization;

    @SerializedName("DE:Seed list email addresses and segments")
    private String seedlistEmailAndSegments;

    @SerializedName("DE:English Language Combinations")
    private String englishCombinantions;

    @SerializedName("DE:Selected Business Groups")
    private String selectedBuisnessGroup;

    @SerializedName("DE:Email addresses that should receive all proof tests")
    private String emailAddressesForProofTests;

    @SerializedName("DE:DF Category")
    private String dfCategory;

    @SerializedName("DE:List Count Forecast/LR")
    private String listCountForcast;

    @SerializedName("DE:No. of Languages")
    private int languages;

    @SerializedName("DE:Notes")
    private String notes;

    @SerializedName("DE:Does this campaign include an A/B subject line test?")
    private String campaignIncludeABSubject;

    @SerializedName("DE:Tier 2 Language Combinations")
    private String tier2LanguageCombinations;

    @SerializedName("DE:From Name/Address")
    private String fromNameAndAddress;

    @SerializedName("DE:Proof Testing Email Options")
    private String proofTesting;

    @SerializedName("DE:Selected Funnel Status")
    private String selectedFunnelStatus;

    @SerializedName("DE:Products")
    private String products;

    @SerializedName("DE:Selected Program Purpose")
    private String selectedProgramPurpose;

    @SerializedName("DE:Report Cat - Batched Emails - Completed Programs")
    private String reportCatBatched;

    @SerializedName("DE:# of Translated Html Files")
    private boolean translatedHtmlFiles;


   // @SerializedName("DE:Deployment Date/Time")
    @SerializedName("DE:Deployment Date/Time and Zone")
    private String deploymentDateTime;

    @SerializedName("DE:Fiscal Quarter - Calcd")
    private String fiscalQuatorCald;

    @SerializedName("DE:No. of Email files")
    private int emailFiles;

    @SerializedName("DE:Email label")
    private String emailLabel;

    @SerializedName("DE:PR Calc")
    private String prCalc;

    @SerializedName("DE:Led by/Track")
    private String letBy;

    @SerializedName("DE:What updates to creative are needed?")
    private String creativeUpdates;

    @SerializedName("DE:Project Request #")
    private String projectRequest;

    @SerializedName("DE:Tests included")
    private String testsIncluded;

    @SerializedName("DE:Selected Locales")
    private String selectedLocales;

    @SerializedName("DE:Report Cat - THOR - Banners")
    private int reportCatBanners;

    @SerializedName("DE:Email Content Type")
    private String emailCintentType;

    @SerializedName("DE:No. of Activities")
    private int activities;

    @SerializedName("DE:Report Cat - Triggered Emails")
    private int reportCatTriggeredMails;

    @SerializedName("DE:Request Type")
    private String requestType;

    @SerializedName("DE:Alternate Selected Locales")
    private String alternateSelectedLocales;

    @SerializedName("DE:Creative Requirement")
    private String creativeRequirement;

    @SerializedName("DE:Email drop dates")
    private String emailDropDates;

    @SerializedName("DE:Project Request Created")
    private String projectRequestCreated;

    @SerializedName("DE:Current MDS Job Number/s")
    private String mdsNumber;
}
