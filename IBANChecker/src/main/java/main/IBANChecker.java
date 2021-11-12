package main;

import java.util.ArrayList;
import java.util.List;

import main.CheckResultEvent.StateEnum;
import main.IBANBlacklistLoader.LoadFailException;

/**
 * Implementation of a DocumentCheckService for ckecking IBANs
 * 
 * @author Thorben
 *
 */
public class IBANChecker implements DocumentCheckService{
	
	private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String ALL_NUMBERS = "0123456789";
	// all valid characters used in an IBAN
	private static final String ALL_VALID_CHARS = UPPER_CASE.concat(ALL_NUMBERS);
	// the maximum limit for the number of characters in an IBAN
	private static final int IBAN_LIMIT = 34;
	
	private static final StringBuilder builder = new StringBuilder();
	
	private static final FileTextParser pdfParser = new PDFTextParser();
	
	// blacklist loader for loading a blacklist of IBANS;
	// initialized as a default loader that always returns an empty list
	private IBANBlacklistLoader blacklistLoader = new IBANBlacklistLoader() {

		@Override
		public void loadIBANBlackList() throws LoadFailException {
			
		}

		@Override
		public List<IBAN> getBlacklistedIBANs() {
			return new ArrayList<>();
		}
		
	};
	
	/**
	 * constructs an IBANChecker with default blacklist loader
	 */
	public IBANChecker() {
		
	}
	
	/**
	 * constructs an IBANChecker with the given blacklist loader
	 * 
	 * @param blacklistLoader
	 */
	public IBANChecker(IBANBlacklistLoader blacklistLoader) {
		this.blacklistLoader = blacklistLoader;
	}
	
	/**
	 * sets the blacklist loader used for checking IBANs in a document
	 * 
	 * @param blacklistLoader
	 */
	public void setBlacklistLoader(IBANBlacklistLoader blacklistLoader) {
		this.blacklistLoader = blacklistLoader;
	}

	public CheckResultEvent checkDocument(CheckEvent event){
		
		// create CheckResultEvent to be returned
		CheckResultEvent returnEvent = new CheckResultEvent();
		returnEvent.setState(StateEnum.ERROR).setName(event.getUrl());
		
		String text = "";
		
		try {
			switch(event.getFileType()) {
			case "pdf":
				text = pdfParser.convertFileToText(event.getUrl());
				break;
			default:
				returnEvent.setDetails("the file type is not valid");
				return returnEvent;
			}
		}
		catch (Exception e) {
			returnEvent.setDetails(e.getMessage());
			return returnEvent;
		}
		
		// check all given IBANs for validity and remove invalid ones
		List<IBAN> IBANs = extractIBANs(text);
		List<IBAN> invalidIBANs = getInvalidIBANs(IBANs);
		IBANs.removeAll(invalidIBANs);
		
		try {
			// get a list of blacklisted IBANs;
			// if the blacklist can't be loaded, return an error as a result
			List<IBAN> blacklistedIBANs = getBlacklistedIBANs(IBANs);
			
			// if there are any blacklisted IBANs, generate a CheckResultEvent
			// with status SUSPICIOUS and list those IBANs
			if (!blacklistedIBANs.isEmpty()) {
				returnEvent.setState(StateEnum.SUSPICIOUS);
				
				builder.setLength(0);
				builder.append("the following IBANs were found to be blacklisted: ");
				for(IBAN iBAN : blacklistedIBANs) {
					builder.append(iBAN.ID).append(", ");
				}
				builder.setLength(builder.length() - 2);
				returnEvent.setDetails(builder.toString());
				return returnEvent;
			}
		} catch (LoadFailException e) {
			returnEvent.setDetails(e.getMessage());
			return returnEvent;
		}
		
		// if there are any invalid IBANs, generate a CheckResultEvent
		// with status SUSPICIOUS and list those IBANs
		if (!invalidIBANs.isEmpty()) {
			returnEvent.setState(StateEnum.SUSPICIOUS);
			
			builder.setLength(0);
			builder.append("the following IBANs were found to be invalid: ");
			for(IBAN iBAN : invalidIBANs) {
				builder.append(iBAN.ID).append(", ");
			}
			builder.setLength(builder.length() - 2);
			
			returnEvent.setDetails(builder.toString());
			return returnEvent;
		}
		
		// if all tests were passed, return a CheckResultEvent with status OK
		returnEvent.setState(StateEnum.OK);
		returnEvent.setDetails("all IBANs in this document passed the test");
		
		return returnEvent;
	}
	
	// returns an array of IBANs contained in the given text String
	private List<IBAN> extractIBANs(String docText) {
		
		List<IBAN> IBANs = new ArrayList<>();
		
		// split the text at every instance if "IBAN", then
		// iterate over all the substrings minus the first one
		// (there would be no labeled IBAN)
		String[] iBANStrings = docText.split("IBAN");
		iterateIBANs: for(int i = 1; i < iBANStrings.length; i++) {
			// remove all the blanks first
			String s = iBANStrings[i].replace(" ", "");
			
			// skip empty strings
			if(s.length() < 1) {
				continue;
			}
			
			// define start and end of an IBAN by checking for valid characters
			// and length
			int start = 0;
			int end = 0;
			while(!ALL_VALID_CHARS.contains(s.substring(start, start + 1))) {
				start++;
				if(start == s.length()) {
					continue iterateIBANs;
				}
			}
			
			end = start;
			do {
				end++;
				if(end == s.length() || end == IBAN_LIMIT) {
					break;
				}
			} while(ALL_VALID_CHARS.contains(s.substring(end, end + 1)));
			
			s = s.substring(start).replace(" ","").substring(0, end);
			
			// add a new IBAN to the IBAN list
			IBANs.add(new IBAN(s));
		}
		
		return IBANs;
		
	}
	
	// check all the IBANs in a given list for validity:
	// correct country code, valid length and checksum
	private List<IBAN> getInvalidIBANs(List<IBAN> IBANs) {
		
		List<IBAN> invalidIBANs = new ArrayList<>();
		
		for(IBAN iBAN : IBANs) {
			if(!iBAN.isValid()) {
				invalidIBANs.add(iBAN);
			}
		}
		
		return invalidIBANs;
		
	}
	
	// check all the IBANs in a given list for blacklist status
	private List<IBAN> getBlacklistedIBANs(List<IBAN> IBANs) throws LoadFailException {
		
		List<IBAN> blacklistedIBANs = new ArrayList<>();
		blacklistLoader.loadIBANBlackList();
		List<IBAN> allBlacklistedIBANs = blacklistLoader.getBlacklistedIBANs();
		builder.setLength(0);
		for(IBAN iBAN : allBlacklistedIBANs) {
			builder.append(iBAN.ID);
		}
		String blacklistString = builder.toString();
		
		for(IBAN iBAN : IBANs) {
			if(blacklistString.contains(iBAN.ID)) {
				blacklistedIBANs.add(iBAN);
			}
		}
		
		return blacklistedIBANs;
		
	}
	
}
