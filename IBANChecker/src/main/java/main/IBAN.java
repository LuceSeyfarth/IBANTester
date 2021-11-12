package main;

import java.math.BigInteger;

/**
 * a class representing an IBAN
 * 
 * @author Thorben
 */
public class IBAN {
	
	private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String ALL_NUMBERS = "0123456789";
	private static final String ALL_VALID_CHARS = UPPER_CASE.concat(ALL_NUMBERS);
	// modulus used for IBAN validation
	private static final BigInteger IBAN_MODULUS = new BigInteger("97");
	
	/**
	 * An Enum containing all defined country codes for IBANs
	 * 
	 * @author Thorben
	 */
	public static enum IBANCountryCode {

		EG(29), AL(28), DZ(24), AD(24), AO(25), AZ(28), BH(22), BY(28), BE(16), BJ(28),
		BA(20), BR(29), VG(24), BG(22), BF(27), BI(16), CR(22), CI(28), DK(18), DE(22),
		DO(28), SV(28), EE(20), FO(18), FI(18), FR(27), GA(27), GE(22), GI(23), GR(27),
		GL(18), GT(28), IQ(23), IR(26), IE(22), IS(26), IL(23), IT(27), JO(30), CM(27),
		CV(25), KZ(20), QA(29), CG(27), XK(20), HR(21), KW(30), LV(21), LB(28), LI(21),
		LT(20), LU(20), MG(27), ML(28), MT(31), MR(27), MU(30), MD(24), MC(27), ME(22),
		MZ(25), NL(18), MK(19), NO(15), AT(20), TL(23), PK(24), PS(29), PL(28), PT(25),
		RO(24), SM(27), ST(25), SA(24), SE(24), CH(21), SN(28), RS(22), SC(31), SK(24),
		SI(19), ES(24), LC(32), CZ(24), TN(24), TR(26), UA(29), HU(28), VA(22), AE(23),
		GB(22), CY(28), CF(27);
		
		/**
		 * returns an IBANCountryCode with the given name
		 * 
		 * @param code
		 * 			the name of the code
		 * @return
		 * 			an IBANCountryCode with the given name, if such an instance exists
		 */
		public static IBANCountryCode getCountryCodeByName(String code) {
			
			for(IBANCountryCode value : values()) {
				if (value.name().equals(code)) {
					return value;
				}
			}
			
			return null;
			
		}
		
		/**
		 * the length of IBANs used in this country
		 */
		public final int IBANlength;
		
		private IBANCountryCode(int IBANlength) {
			
			this.IBANlength = IBANlength;
			
		}
		
	}

	/**
	 * the text representation of this IBAN, without any blanks
	 */
	public final String ID;

	private final IBANCountryCode countryCode;	
	private Boolean isValid;
	
	/**
	 * constructs a new IBAN with the given ID
	 * 
	 * @param ID
	 */
	public IBAN(String ID) {
		// remove any blanks
		ID = ID.replace(" ", "");
		// get country code, and if it is a valid country code, use the
		// IBAN length to trim the given ID in necessary
		this.countryCode = IBANCountryCode.getCountryCodeByName(ID.substring(0, 2));
		if(this.countryCode == null) {
			this.ID = ID;
		}
		else {
			this.ID = ID.substring(0, this.countryCode.IBANlength);
		}
	}
	
	@Override
	public boolean equals(Object object) {
		
		try {
			IBAN otherIBAN = (IBAN) object;
			
			if(!ID.equals(otherIBAN.ID)) {
				return false;
			}
			
		}
		catch (ClassCastException e) {
			return false;
		}
		
		return true;
		
	}
	
	/**
	 * returns true, if this is a valid IBAN;
	 * an IBAN is valid if it has a valid country code, character length
	 * and checksum
	 * 
	 * @return
	 */
	public boolean isValid() {
		
		if(isValid != null) {
			return isValid;
		}
		
		isValid = validate();
		return isValid;
		
	}
	
	private boolean validate() {
		
		if (countryCode == null) {
			return false;
		}
		
		if (ID.length() != countryCode.IBANlength) {
			return false;
		}
		
		if(!ALL_NUMBERS.contains(ID.substring(2, 3))
				|| !ALL_NUMBERS.contains(ID.substring(3, 4))) {
			return false;
		}
		
		for(int i = 4; i < ID.length(); i++) {
			if (!ALL_VALID_CHARS.contains(ID.substring(i, i + 1))) {
				return false;
			}
		}
		
		return checksumValid();
		
	}
	
	private boolean checksumValid() {
		
		String testSum = ID.substring(4).concat(ID.substring(0, 4));
		
		for(int i = 0; i < UPPER_CASE.length(); i++) {
			testSum = testSum.replace(UPPER_CASE.substring(i, i + 1),
					String.valueOf(10 + i));
		}
		
		try {
			BigInteger check = new BigInteger(testSum).mod(IBAN_MODULUS);
			return check.intValue() == 1;
		}
		catch (NumberFormatException e) {
			return false;
		}
		
	}
	
}
