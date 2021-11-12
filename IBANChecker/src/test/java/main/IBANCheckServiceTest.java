package main;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import main.CheckResultEvent.StateEnum;

/**
 * Test class for the IBAN checker
 * 
 * @author Thorben
 */
public class IBANCheckServiceTest extends TestCase {
	
	private DocumentCheckService checkService;
	private IBANBlacklistLoader testLoaderA = new IBANBlacklistLoader() {

		List<IBAN> IBANs = new ArrayList<>();
		
		@Override
		public void loadIBANBlackList() throws LoadFailException {
			IBAN iBAN = new IBAN("DE15 1234 0601 0505 7807 80");
			IBANs.add(iBAN);
		}

		@Override
		public List<IBAN> getBlacklistedIBANs() {
			return IBANs;
		}
		
	};
	
	private IBANBlacklistLoader testLoaderB = new IBANBlacklistLoader() {

		List<IBAN> IBANs = new ArrayList<>();
		
		@Override
		public void loadIBANBlackList() throws LoadFailException {
			IBAN iBAN = new IBAN("DE15 3006 0601 0505 7807 80");
			IBANs.add(iBAN);
		}

		@Override
		public List<IBAN> getBlacklistedIBANs() {
			return IBANs;
		}
		
	};

	protected void setUp() throws Exception {
		super.setUp();
		
		checkService = new IBANChecker();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * file type denoted in the event is no pdf, should return error
	 */
	public void testCheckDocumentError1() {		
		CheckEvent event = new CheckEvent();
		event.setUrl("https://dl.dropboxusercontent.com/s/859l15vmwocg9ai/textfile.txt?dl=0");
		event.setFileType("txt");
		CheckResultEvent result = checkService.checkDocument(event);
		
		assertTrue(result.getState() == StateEnum.ERROR);
		assertTrue(result.getDetails().equals("the file type is not valid"));
	}
	
	/**
	 * file type denoted in the event is pdf, but file is no pdf;
	 * should return error
	 */
	public void testCheckDocumentError2() {		
		CheckEvent event = new CheckEvent();
		event.setUrl("https://dl.dropboxusercontent.com/s/859l15vmwocg9ai/textfile.txt?dl=0");
		event.setFileType("pdf");
		CheckResultEvent result = checkService.checkDocument(event);
		
		assertTrue(result.getState() == StateEnum.ERROR);
		assertTrue(result.getDetails().equals("the file at the given URL could not be parsed to text"));
	}
	
	/**
	 * file type denoted in the event is pdf, but there is no file at the given URL;
	 * should return error
	 */
	public void testCheckDocumentError3() {		
		CheckEvent event = new CheckEvent();
		event.setUrl("https://dl.dropboxusercontent.com/s/859l10vmwocg9ai/textfile.txt?dl=0");
		event.setFileType("pdf");
		CheckResultEvent result = checkService.checkDocument(event);
		
		assertTrue(result.getState() == StateEnum.ERROR);
		assertTrue(result.getDetails().equals("the file at the given URL could not be opened"));
	}
	
	/**
	 *  given test pdf, should return with status OK
	 */
	public void testCheckDocumentStatusOK() {
		IBANChecker checker = (IBANChecker) checkService;
		checker.setBlacklistLoader(testLoaderA);
		CheckEvent event = new CheckEvent();
		event.setUrl("https://dl.dropboxusercontent.com/s/dl4v0p2m1zfhtv9/Testdaten_Rechnungseinreichung.pdf?dl=0");
		event.setFileType("pdf");
		CheckResultEvent result = checkService.checkDocument(event);
		
		assertTrue(result.getState() == StateEnum.OK);
		assertTrue(result.getDetails().equals("all IBANs in this document passed the test"));
	}
	
	/**
	 *  given test pdf, but IBAN DE15 3006 0601 0505 7807 80 is blacklisted;
	 *  should return with status SUSPICIOUS
	 */
	public void testCheckDocumentStatusFailed1() {
		IBANChecker checker = (IBANChecker) checkService;
		checker.setBlacklistLoader(testLoaderB);
		CheckEvent event = new CheckEvent();
		event.setUrl("https://dl.dropboxusercontent.com/s/dl4v0p2m1zfhtv9/Testdaten_Rechnungseinreichung.pdf?dl=0");
		event.setFileType("pdf");
		CheckResultEvent result = checkService.checkDocument(event);
		
		assertTrue(result.getState() == StateEnum.SUSPICIOUS);
		assertTrue(result.getDetails().substring(0, 97).equals("the following IBANs were found to be blacklisted: DE15300606010505780780, DE15300606010505780780,"));
	}

}
