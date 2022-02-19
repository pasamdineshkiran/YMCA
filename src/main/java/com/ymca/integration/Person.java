package com.ymca.integration;

/**
 * 
 * This is a POJO for Person and all attributes are of type Strings so that it
 * is convenient to trigger REST API request.
 *
 */
public class Person {
	String fName, lName, emailId, defaultExpCode, locCode, mgrCode, pNumber;
	String ACTION,DATE,STATUS,COMMENTS;
	int updateActionFlag = 0;

	public Person(String pNumber, String ACTION,String DATE,String STATUS,String COMMENTS) {
		this.pNumber = pNumber;
		this.ACTION = ACTION;
		this.DATE = DATE;
		this.STATUS = STATUS;
		this.COMMENTS = COMMENTS;
	}
	
	public Person(String pNumber, String fName, String lName, String emailId, String mgrCode, String defaultExpCode,
			String locCode) {
		super();
		this.fName = fName;
		this.lName = lName;
		this.emailId = emailId;
		this.defaultExpCode = defaultExpCode;
		this.locCode = locCode;
		this.mgrCode = mgrCode;
		this.pNumber = pNumber;
	}

	public String getfName() {
		return fName;
	}

	public String getlName() {
		return lName;
	}

	public String getEmailId() {
		return emailId;
	}

	public String getDefaultExpCode() {
		return defaultExpCode;
	}

	public String getLocCode() {
		return locCode;
	}

	public String getMgrCode() {
		return mgrCode;
	}

	public String getpNumber() {
		return pNumber;
	}

	@Override
	public String toString() {
		return "Person [fName=" + fName + ", lName=" + lName + ", emailId=" + emailId + ", defaultExpCode="
				+ defaultExpCode + ", locCode=" + locCode + ", mgrCode=" + mgrCode + ", pNumber=" + pNumber + "]";
	}

	public String getACTION() {
		return ACTION;
	}

	public void setACTION(String aCTION) {
		ACTION = aCTION;
	}

	public String getDATE() {
		return DATE;
	}

	public void setDATE(String dATE) {
		DATE = dATE;
	}

	public String getSTATUS() {
		return STATUS;
	}

	public void setSTATUS(String sTATUS) {
		STATUS = sTATUS;
	}

	public String getCOMMENTS() {
		return COMMENTS;
	}

	public void setCOMMENTS(String cOMMENTS) {
		COMMENTS = cOMMENTS;
	}

	public int getUpdateActionFlag() {
		return updateActionFlag;
	}

	public void setUpdateActionFlag(int updateActionFlag) {
		this.updateActionFlag = updateActionFlag;
	}
	
	
	

}
