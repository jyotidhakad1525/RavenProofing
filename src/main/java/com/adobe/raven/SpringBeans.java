package com.adobe.raven;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;


@Configuration
public class SpringBeans {

	//5b17950201941a929e5cd0c6ef2a07dc
	

	
	@Bean
	public ArrayList<String> customFormList() {
		
		ArrayList<String> customFormList = new ArrayList<String>();
		
		customFormList.add(CustomFormKey_SelectedRegions);
		customFormList.add(CustomFormKey_DeploymentFlexibility);
		customFormList.add(CustomFormKey_ReferencePriorJob);
		customFormList.add(CustomFormKey_LinksLive);
		customFormList.add(CustomFormKey_PrimaryRegion);
		customFormList.add(CustomFormKey_FunnelStatus);
		customFormList.add(CustomFormKey_DeployVL);
		customFormList.add(CustomFormKey_BusinessGroupL);
		customFormList.add(CustomFormKey_Region);
		customFormList.add(CustomFormKey_EmailDropDate);
		customFormList.add(CustomFormKey_DeploymentTeam);
		customFormList.add(CustomFormKey_LifecycleMarket);
		customFormList.add(CustomFormKey_PlannedProject);
		customFormList.add(CustomFormKey_CgenProgramId);
		customFormList.add(CustomFormKey_ReportCat);
		customFormList.add(CustomFormKey_EStandardSeedList);
		customFormList.add(CustomFormKey_CampaignName);
		customFormList.add(CustomFormKey_Complexity);
		customFormList.add(CustomFormKey_deploymentDateTime2);
		customFormList.add(CustomFormKey_CampaignIncPersonalization);
		
		customFormList.add(CustomFormKey_SeedlistEmailAndSegments);
		customFormList.add(CustomFormKey_EnglishCombinantions);
		customFormList.add(CustomFormKey_SelectedBuisnessGroup);
		customFormList.add(CustomFormKey_EmailAddressesForProofTests);
		customFormList.add(CustomFormKey_DfCategory);
		customFormList.add(CustomFormKey_ListCountForcast);
		customFormList.add(CustomFormKey_Languages);
		customFormList.add(CustomFormKey_Notes);
		customFormList.add(CustomFormKey_CampaignIncludeABSubject);
		customFormList.add(CustomFormKey_Tier2LanguageCombinations);
		
		customFormList.add(CustomFormKey_FromNameAndAddress);
		customFormList.add(CustomFormKey_ProofTesting);
		customFormList.add(CustomFormKey_SelectedFunnelStatus);
		customFormList.add(CustomFormKey_Products);
		customFormList.add(CustomFormKey_SelectedProgramPurpose);
		customFormList.add(CustomFormKey_ReportCatBatched);
		customFormList.add(CustomFormKey_TranslatedHtmlFiles);
		customFormList.add(CustomFormKey_DeploymentDateTime);
		customFormList.add(CustomFormKey_FiscalQuatorCald);
		customFormList.add(CustomFormKey_EmailFiles);
		customFormList.add(CustomFormKey_EmailLabel);
		customFormList.add(CustomFormKey_PrCalc);
		customFormList.add(CustomFormKey_LetBy);
		customFormList.add(CustomFormKey_CreativeUpdates);
		customFormList.add(CustomFormKey_ProjectRequest);
		
		customFormList.add(CustomFormKey_TestsIncluded);
		customFormList.add(CustomFormKey_SelectedLocales);
		customFormList.add(CustomFormKey_ReportCatBanners);
		customFormList.add(CustomFormKey_EmailCintentType);
		customFormList.add(CustomFormKey_Activities);
		customFormList.add(CustomFormKey_ReportCatTriggeredMails);
		customFormList.add(CustomFormKey_RequestType);
		customFormList.add(CustomFormKey_AlternateSelectedLocales);
		customFormList.add(CustomFormKey_CreativeRequirement);
		customFormList.add(CustomFormKey_EmailDropDates);
		customFormList.add(CustomFormKey_ProjectRequestCreated);
		
		
		
		return customFormList;
	}
	
	
	public static String CustomFormKey_SelectedRegions = "DE:Selected Regions";
	public static String CustomFormKey_DeploymentFlexibility = "DE:Deployment Flexibility";
	public static String CustomFormKey_ReferencePriorJob = "DE:Reference Prior Job#/LR";
	public static String CustomFormKey_LinksLive = "DE:Are all the links live?";
	public static String CustomFormKey_PrimaryRegion = "DE:Primary Region";
	public static String CustomFormKey_FunnelStatus = "DE:Funnel Status";
	public static String CustomFormKey_DeployVL = "DE:Deploy VIA/LR";
	public static String CustomFormKey_BusinessGroupL = "DE:Business Group";
	public static String CustomFormKey_Region = "DE:Region";
	public static String CustomFormKey_EmailDropDate = "DE:Does this request have more than 1 email drop date?";
	public static String CustomFormKey_DeploymentTeam = "DE:Deployment Team";
	public static String CustomFormKey_LifecycleMarket = "DE:Lifecycle Marketing Request Type";
	public static String CustomFormKey_PlannedProject = "DE:Planned Project";
	public static String CustomFormKey_CgenProgramId = "DE:CGEN Program ID";
	public static String CustomFormKey_ReportCat = "DE:Report Cat - THOR Notifications";
	public static String CustomFormKey_EStandardSeedList = "DE:EMEA Standard Seed List";
	public static String CustomFormKey_CampaignName = "DE:Campaign Name";
	public static String CustomFormKey_Complexity = "DE:Complexity";
	public static String CustomFormKey_deploymentDateTime2 = "DE:Deployment Date/Time2";
	public static String CustomFormKey_CampaignIncPersonalization = "DE:Does this campaign include personalization?";
	public static String CustomFormKey_SeedlistEmailAndSegments = "DE:Seed list email addresses and segments";
	public static String CustomFormKey_EnglishCombinantions = "DE:English Language Combinations";
	public static String CustomFormKey_SelectedBuisnessGroup = "DE:Selected Business Groups";
	public static String CustomFormKey_EmailAddressesForProofTests = "DE:Email addresses that should receive all proof tests";
	public static String CustomFormKey_DfCategory = "DE:DF Category";
	public static String CustomFormKey_ListCountForcast = "DE:List Count Forecast/LR";
	public static String CustomFormKey_Languages = "DE:No. of Languages";
	public static String CustomFormKey_Notes = "DE:Notes";
	public static String CustomFormKey_CampaignIncludeABSubject = "DE:Does this campaign include an A/B subject line test?";
	public static String CustomFormKey_Tier2LanguageCombinations = "DE:Tier 2 Language Combinations";
	public static String CustomFormKey_FromNameAndAddress = "DE:From Name/Address";
	public static String CustomFormKey_ProofTesting = "DE:Proof Testing Email Options";
	public static String CustomFormKey_SelectedFunnelStatus = "DE:Selected Funnel Status";
	public static String CustomFormKey_Products = "DE:Products";
	public static String CustomFormKey_SelectedProgramPurpose = "DE:Selected Program Purpose";
	public static String CustomFormKey_ReportCatBatched = "DE:Report Cat - Batched Emails - Completed Programs";
	public static String CustomFormKey_TranslatedHtmlFiles = "DE:# of Translated Html Files";
	public static String CustomFormKey_DeploymentDateTime = "DE:Deployment Date/Time";
	public static String CustomFormKey_FiscalQuatorCald = "DE:Fiscal Quarter - Calcd";
	public static String CustomFormKey_EmailFiles = "DE:No. of Email files";
	public static String CustomFormKey_EmailLabel = "DE:Email label";
	public static String CustomFormKey_PrCalc = "DE:PR Calc";
	public static String CustomFormKey_LetBy = "DE:Led by/Track";
	public static String CustomFormKey_CreativeUpdates = "DE:What updates to creative are needed?";
	public static String CustomFormKey_ProjectRequest = "DE:Project Request #";
	public static String CustomFormKey_TestsIncluded = "DE:Tests included";
	public static String CustomFormKey_SelectedLocales = "DE:Selected Locales";
	public static String CustomFormKey_ReportCatBanners = "DE:Report Cat - THOR - Banners";
	public static String CustomFormKey_EmailCintentType = "DE:Email Content Type";
	public static String CustomFormKey_Activities = "DE:No. of Activities";
	public static String CustomFormKey_ReportCatTriggeredMails = "DE:Report Cat - Triggered Emails";
	public static String CustomFormKey_RequestType = "DE:Request Type";
	public static String CustomFormKey_AlternateSelectedLocales = "DE:Alternate Selected Locales";
	public static String CustomFormKey_CreativeRequirement = "DE:Creative Requirement";
	public static String CustomFormKey_EmailDropDates = "DE:Email drop dates";
	public static String CustomFormKey_ProjectRequestCreated = "DE:Project Request Created";
}
