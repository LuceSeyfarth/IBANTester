package main;

/**
 * Interface for a DocumentCheckService
 * 
 * @author Thorben
 *
 */
public interface DocumentCheckService {

	/**
	 * checks the Document from the URL contained in the given CheckEvent
	 * 
	 * @param event
	 * 			the CheckEvent containing an URL to be checked
	 * @return
	 * 			a CheckResultEvent containing the status of the check
	 */
	public CheckResultEvent checkDocument(CheckEvent event);
	
}
