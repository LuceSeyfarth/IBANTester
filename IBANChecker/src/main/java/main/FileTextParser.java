package main;

/**
 * an Interface for a parser that converts files from a given URL to text
 * 
 * @author Thorben
 */
public interface FileTextParser {
	
	/**
	 * converts the file a the given URL to text if possible
	 * 
	 * @param file
	 * 			the URL where the file can be found at
	 * @return
	 * 			a text string representing the file
	 * @throws
	 * 			an Exception if the file could not be parsed
	 * 			to text
	 */
	public String convertFileToText(String url) throws Exception;
	
}
