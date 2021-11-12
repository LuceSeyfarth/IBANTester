package main;

import java.util.List;

/**
 * An interface for a IBAN blacklist loader
 * 
 * @author Thorben
 */
public interface IBANBlacklistLoader {

	/**
	 * An Exception used when the loading of an IBAN blacklist fails
	 * 
	 * @author Thorben
	 */
	static class LoadFailException extends Exception {
		
		private static final long serialVersionUID = -1516029228592081325L;

		public LoadFailException(String message) {
			super(message);
		}
		
	}
	
	/**
	 * attempts to load the IBAN blacklist defined by this IBANBlacklistLoader
	 * 
	 * @throws LoadFailException
	 * 			when the list couldn't be loaded
	 */
	void loadIBANBlackList() throws LoadFailException;
	
	/**
	 * gets a list of all IBANs present on the currently loaded list
	 * 
	 * @return
	 */
	List<IBAN> getBlacklistedIBANs();
	
}
