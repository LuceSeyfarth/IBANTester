package main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * implementation of a FileTextParser for PDF files
 * 
 * @author Thorben
 */
public class PDFTextParser implements FileTextParser {

	// the name of a temporary test file
	private static final String CHECK_FILE = "testFile.pdf";
	
	@Override
	public String convertFileToText(String url) throws Exception {
		
		File file = createLocalFile(url);
		// wenn Datei nicht erstellt werden konnte, wirf Exception
		if(file == null) {
			throw new Exception("the file at the given URL could not be opened");
		}
		
		RandomAccessBufferedFileInputStream inputStream = null;
		
		PDFParser parser = null;
	    PDDocument pdDoc = null;
	    COSDocument cosDoc = null;
	    PDFTextStripper pdfStripper;
	    String parsedText = null;
	    try {
	    	inputStream = new RandomAccessBufferedFileInputStream(file);
	        parser = new PDFParser(inputStream);
	        parser.parse();
	        cosDoc = parser.getDocument();
	        pdfStripper = new PDFTextStripper();
	        pdDoc = new PDDocument(cosDoc);
	        parsedText = pdfStripper.getText(pdDoc);
	        parsedText.replaceAll("[^A-Za-z0-9. ]+", "");
	        inputStream.close();
	        cosDoc.close();
	        pdDoc.close();
	    } catch (Exception e) {
	        try {
	        	if (inputStream != null)
	        		inputStream.close();
	            if (cosDoc != null)
	                cosDoc.close();
	            if (pdDoc != null)
	                pdDoc.close();
	        } catch (Exception e1) {
	            
	        }
	    }
	    
	    file.delete();
	    
	    // wenn der Text nicht erstellt werden konnte, wird eine Exception
	    if(parsedText == null) {
	    	throw new Exception("the file at the given URL could not be parsed to text");
	    }
	    
	    return parsedText;
	    
	}

	// creates and returns a local copy of the pdf at the given url
	private File createLocalFile(String url) {
	
		File pdf = null;
		
		try (InputStream in = new URL(url).openStream()) {
			pdf = new File(CHECK_FILE);
			pdf.delete();
			Files.copy(in, Paths.get(pdf.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
		} catch (MalformedURLException e) {
		
		} catch (IOException e) {
			
		}
	
	return pdf;
	
	}
	
}
