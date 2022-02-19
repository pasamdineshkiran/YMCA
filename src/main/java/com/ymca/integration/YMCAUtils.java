package com.ymca.integration;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class YMCAUtils {

	public final static String ACTION_CODE_CREATE = "Create";
	public final static String ACTION_CODE_UPDATE = "Update";
	public final static String ACTION_CODE_TERMINATE = "Terminate";

	public final static String REPORT_HEADER_ROW = "EMPLOYEE_NUMBER,ACTION,DATE,STATUS,COMMENTS";
	
	public static String BASE_URL;
	public static String BASIC_AUTH_USERNAME;
	public static String BASIC_AUTH_PASSWORD;

	public static String DB_HOST;
	public static String DB_PORT; 
	public static String DATABASE_NAME;
	public static String DB_USERNAME;
	public static String DB_PASSWORD; 
	public static String DB_TABLE_NAME;
	
	public static String PERSON_CREATE;
	public static String PERSON_USER_ACCOUNT;
	public static String PERSON_UPDATE;
	public static String PERSON_TERMINATE;
	
	public static Boolean ONLY_STATUS_FLAG_NULL;
	
	public static String START_DATE;
	public static String LOG_LEVEL;
	public static String RUN_MODE;
	
	public static List<Person> createPersonList;
	public static List<Person> updatePersonList;
	public static List<Person> terminatePersonList;
	public static List<String> actionCodeList;
	
	public static List<Person> reportList;
	
	public static int CREATE_PASS_COUNT;
	public static int CREATE_FAIL_COUNT;
	public static int TERMINATE_PASS_COUNT;
	public static int TERMINATE_FAIL_COUNT;
	public static int UPDATE_PASS_COUNT;
	public static int UPDATE_FAIL_COUNT;
	public static int PROCESSED_ENTRIES_COUNT;
	
	public static String NUMBER_OF_RECORDS;

	public final static int MSG_FAILED = 2;
	public final static int MSG_SUCCESS = 1;
	public final static String MSG_DONE = "DONE";
	
	public static Connection conn = null;
	
	public static Logger LOGGER = null;
	
	public static String mailFrom;
	public static String mailTo; 		
	public static String mailSubject;
	public static String mailBody;
	
	public static String host;
	public static String port;
	public static String auth;
	public static String tlsenabled;
	public static String user;
	public static String password;

	public static StringBuilder sbr = new StringBuilder();
	
	public static boolean fatalErr =  false;
	public static String reportFileName;

	
	public static void  intialize() {
		createPersonList = new ArrayList<Person>();
		updatePersonList = new ArrayList<Person>();
		terminatePersonList = new ArrayList<Person>();
		actionCodeList = Arrays.asList(new String[] { ACTION_CODE_CREATE, ACTION_CODE_UPDATE, ACTION_CODE_TERMINATE });
		LOGGER = getLogger();
		reportList = new ArrayList<Person>();
	}

	private static final String CREATE_PERSON_JSON_TEMPLATE = "{\r\n" + "	\"PersonNumber\": \"#PERSON_NUMBER#\",\r\n"
			+ "    \"names\": [\r\n" + "        {\r\n" + "            \"LegislationCode\": \"US\",\r\n"
			+ "            \"LastName\": \"#LAST_NAME#\",\r\n" + "            \"FirstName\": \"#FIRST_NAME#\"\r\n"
			+ "        }\r\n" + "    ],\r\n" + "    \"emails\": [\r\n" + "        {\r\n"
			+ "            \"EmailType\": \"W1\",\r\n" + "            \"EmailAddress\": \"#EMAIL_ADDRESS#\"\r\n"
			+ "        }\r\n" + "    ],\r\n" + "    \"workRelationships\": [\r\n" + "        {\r\n"
			+ "            \"LegalEmployerName\": \"YMCA Of Silicon Valley\",\r\n"
			+ "            \"WorkerType\": \"E\",\r\n" + "            \"assignments\": [\r\n" + "                {\r\n"
			+ "                    \"ActionCode\": \"HIRE\",\r\n"
			+ "                    \"DefaultExpenseAccount\": \"#DEFAULT_EXPENSE_ACCOUNT#\",\r\n"
			+ "                    \"BusinessUnitName\": \"YMCA Of Silicon Valley\",\r\n"
			+ "					\"LocationCode\": \"#LOCATION_CODE#\",\r\n" + "					\"managers\":[\r\n"
			+ "						{\r\n"
			+ "						    \"ManagerAssignmentNumber\": \"#MANAGER_ASSIGNMENT_NUMBER#\",\r\n"
			+ "							\"ActionCode\": \"MANAGER_CHANGE\",\r\n"
			+ "							\"ManagerType\": \"LINE_MANAGER\"\r\n" + "						}\r\n"
			+ "					]\r\n" + "					\r\n" + "                }\r\n" + "            ]\r\n"
			+ "        }\r\n" + "    ]\r\n" + "}";

	private static final String UPDATE_PERSON_NAME_TEMPLATE = "{\r\n"
			+ "    \"LastName\": \"#FIRST_NAME#\",\r\n"
			+ "    \"FirstName\": \"#LAST_NAME#\"\r\n"
			+ "}";
	
	private static final String UPDATE_PERSON_EMAIL_TEMPLATE = "{\r\n"
			+ "    \"EmailAddress\": \"#EMAIL_ADDRESS#\"\r\n"
			+ "}";
	
	private static final String UPDATE_PERSON_WORK_REL_TEMPLATE = "{\r\n"
			+ "    \"DefaultExpenseAccount\": \"#DEFAULT_EXPENSE_ACCOUNT#\",\r\n"
			+ "	\"ActionCode\": \"ASG_CHANGE\",\r\n"
			+ "	\"LocationCode\": \"#LOCATION_CODE#\"\r\n"
			+ "}";
	
	private static final String UPDATE_PERSON_MGR_TEMPLATE = "{\r\n"
			+ "    \"ManagerAssignmentNumber\": \"#MANAGER_ASSIGNMENT_NUMBER#\",\r\n"
			+ "	\"ActionCode\": \"MANAGER_CHANGE\",\r\n"
			+ "	\"ManagerType\": \"LINE_MANAGER\"\r\n"
			+ "}";
	
	public static final String TERMINATE_PERSON_TEMPLATE = "{ \r\n" + "    \"actionCode\": \"RESIGNATION\", \r\n"
			+ "    \"terminationDate\": \"2019-05-10\" \r\n" + "} ";
	
	public static void main(String[] args) {
		try {
			getSqlServConnection();
			loadAllPersonsActionsList();
			printAllPersonsRecordsFromDB();
		} finally {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void loadConfigurationProps() {
		//LOGGER.severe("LOADING CONFIURATIONS FROM PROPERTIES");
	    Properties props = new Properties();  
	    try {
	    	FileReader reader=new FileReader("YMCA_HCM_Configurations.properties");
	    	props.load(reader);
			BASE_URL=props.getProperty("BASE_URL");
			BASIC_AUTH_USERNAME=props.getProperty("BASIC_AUTH_USERNAME");
			BASIC_AUTH_PASSWORD=props.getProperty("BASIC_AUTH_PASSWORD");
			
			DB_HOST=props.getProperty("DB_HOST");
			DB_PORT=props.getProperty("DB_PORT");
			DATABASE_NAME=props.getProperty("DATABASE_NAME");
			DB_USERNAME=props.getProperty("DB_USERNAME");
			DB_PASSWORD=props.getProperty("DB_PASSWORD");
			DB_TABLE_NAME=props.getProperty("DB_TABLE_NAME");
			
			PERSON_CREATE=props.getProperty("PERSON_CREATE");
			PERSON_USER_ACCOUNT=props.getProperty("PERSON_USER_ACCOUNT");
			PERSON_UPDATE=props.getProperty("PERSON_UPDATE");
			PERSON_TERMINATE=props.getProperty("PERSON_TERMINATE");
			
			
			RUN_MODE=props.getProperty("RUN_MODE");
			LOG_LEVEL=props.getProperty("LOG_LEVEL"); 
			NUMBER_OF_RECORDS=props.getProperty("NUMBER_OF_RECORDS");
			
			mailFrom = props.getProperty("job.status.emails.from");
			mailTo = props.getProperty("job.status.emails.to");
			mailSubject = props.getProperty("job.status.emails.subject");
			mailBody = props.getProperty("job.status.emails.body");
			
			host=props.getProperty("mail.smtp.host");
			port=props.getProperty("mail.smtp.port");
			auth=props.getProperty("mail.smtp.auth");
			tlsenabled=props.getProperty("mail.smtp.tlsenabled");
			user=props.getProperty("mail.smtp.user");
			password=props.getProperty("mail.smtp.password");
			
			ONLY_STATUS_FLAG_NULL = Boolean.parseBoolean(props.getProperty("ONLY_STATUS_FLAG_NULL"));

			
		} catch (IOException e) {
			e.printStackTrace();
			//LOGGER.severe("ERROR LOADING CONFIURATIONS FROM PROPERTIES: "+e.getMessage());
		}  
	}

	public static void loadAllPersonsActionsList() {
		actionCodeList.stream().forEach((actionCode) -> getPersonRecords(actionCode));
	}

	public static void printAllPersonsRecordsFromDB() {
		System.out.println(createPersonList);
		System.out.println(updatePersonList);
		System.out.println(terminatePersonList);
	}

	public static void getSqlServConnection() {
		try {
			YMCAUtils.LOGGER.info("ESTABLISHING DB CONNECTION");
			String dbURLCreds = "jdbc:sqlserver://"+DB_HOST+":"+DB_PORT+";"+"databaseName="+DATABASE_NAME+";user="+DB_USERNAME+";password="+DB_PASSWORD;
			conn = DriverManager.getConnection(dbURLCreds);
			if (conn != null) {
				DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();
				YMCAUtils.LOGGER.info("Driver name: " + dm.getDriverName());
				YMCAUtils.LOGGER.info("Driver version: " + dm.getDriverVersion());
				YMCAUtils.LOGGER.info("Product name: " + dm.getDatabaseProductName());
				YMCAUtils.LOGGER.info("Product version: " + dm.getDatabaseProductVersion());
			}
		} catch (SQLException ex) {
			YMCAUtils.LOGGER.severe("ERROR ESTABLISHING DB CONNECTION: "+ex.getMessage());
		}
	}

	public static void getPersonRecords(String actionCode) {
		try {
			YMCAUtils.LOGGER.info("FETCHING PERSON RECORDS FOR ALL ACTIONS FROM DB");
			Statement s1 = conn.createStatement();
			String selectQuery = null;
			if(NUMBER_OF_RECORDS != null && Integer.parseInt(NUMBER_OF_RECORDS) > 0) {
				if(ONLY_STATUS_FLAG_NULL) {
					selectQuery  = "SELECT TOP("+ NUMBER_OF_RECORDS +") * FROM "+DB_TABLE_NAME+" WHERE ACTION = '" + actionCode + "' AND STATUSFLAG IS NULL ORDER BY REQUESTDATE ASC";
				}else {
					selectQuery  = "SELECT TOP("+ NUMBER_OF_RECORDS +") * FROM "+DB_TABLE_NAME+" WHERE ACTION = '" + actionCode + "' AND (STATUSFLAG IS NULL OR STATUSFLAG = 2) ORDER BY REQUESTDATE ASC";	
				}
			}else {
				if(ONLY_STATUS_FLAG_NULL) {
					selectQuery  = "SELECT * FROM "+DB_TABLE_NAME+" WHERE ACTION = '" + actionCode + "' AND STATUSFLAG IS NULL ORDER BY REQUESTDATE ASC";
				}else {
					selectQuery  = "SELECT * FROM "+DB_TABLE_NAME+" WHERE ACTION = '" + actionCode + "' AND (STATUSFLAG IS NULL OR STATUSFLAG = 2) ORDER BY REQUESTDATE ASC";	
				} 
			}
			YMCAUtils.LOGGER.info(selectQuery);
			//selectQuery  = "SELECT * FROM "+DB_TABLE_NAME+" WHERE ACTION = '" + actionCode + "' AND EMPLOYEE_NUMBER = 2097";
			ResultSet rs = s1.executeQuery(selectQuery);
			if (rs != null) {
				while (rs.next()) {
					String fName = rs.getString("FirstName");
					String lName = rs.getString("LastName");
					String emailId = rs.getString("AD_Email");
					String defaultExpCode = rs.getString("DefaultExpenseAccount");
					String locCode = rs.getString("FusionLocationCode");
					String mgrCode = rs.getString("manager_id");
					String pNumber = rs.getString("employee_number");
					pNumber = pNumber.substring(0, pNumber.length() - 2); //Removing decimal .0 at the end
					//pNumber, fName, lName, emailId, mgrCode, defaultExpCode, locCode
					switch (actionCode) {
					case ACTION_CODE_CREATE:
						createPersonList
								.add(new Person(pNumber, fName, lName, emailId, mgrCode, defaultExpCode, locCode));
						break;
					case ACTION_CODE_UPDATE:
						updatePersonList
								.add(new Person(pNumber, fName, lName, emailId, mgrCode, defaultExpCode, locCode));
						break;
					case ACTION_CODE_TERMINATE:
						terminatePersonList
								.add(new Person(pNumber, fName, lName, emailId, mgrCode, defaultExpCode, locCode));
						break;
					}
				}
			}
		} catch (SQLException ex) {
			YMCAUtils.LOGGER.severe("Error getting person records from DB - "+ex.getMessage());
		}
	}

	public static void updateStatusMessage(int employeeNumber, String statusMessage, String action, int statusFlag) {
		try {
			statusMessage = statusMessage.replaceAll("'", "");
			String query = "UPDATE "+DB_TABLE_NAME+" SET STATUSMESSAGE = '" + statusMessage + "', STATUSFLAG = '" + 
					statusFlag + "', STATUSDATE = GETDATE() WHERE EMPLOYEE_NUMBER = "+ employeeNumber + " AND ACTION = '" +action+ "'"; //FIXME - emp_num/ action/ statusFlag
			PreparedStatement stmt = conn.prepareStatement(query);
			YMCAUtils.LOGGER.info("UPDATING DB ENTRY FOR PERSON WITH QUERY: "+ query);
			stmt.executeUpdate();
		} catch (SQLException ex) {
			YMCAUtils.LOGGER.severe("ERROR WHILE UPDATING DB : "+ ex.getMessage());
		}
	}

	public static String getCreatePersonJSON(Person perosonObj) {
		return CREATE_PERSON_JSON_TEMPLATE.replaceAll("#FIRST_NAME#", perosonObj.getfName())
				.replaceAll("#LAST_NAME#", perosonObj.getlName()).replaceAll("#EMAIL_ADDRESS#", perosonObj.getEmailId())
				.replaceAll("#DEFAULT_EXPENSE_ACCOUNT#", perosonObj.getDefaultExpCode())
				.replaceAll("#LOCATION_CODE#", perosonObj.getLocCode())
				.replaceAll("#MANAGER_ASSIGNMENT_NUMBER#", "E" + perosonObj.getMgrCode())
				.replaceAll("#PERSON_NUMBER#", perosonObj.getpNumber());
	}
	
	public static String getUpdatePersonNamesJSON(Person perosonObj) {
		return UPDATE_PERSON_NAME_TEMPLATE.replaceAll("#FIRST_NAME#", perosonObj.getfName())
				.replaceAll("#LAST_NAME#", perosonObj.getlName());
	}

	public static String getUpdatePersonEMailJSON(Person perosonObj) {
		return UPDATE_PERSON_EMAIL_TEMPLATE.replaceAll("#EMAIL_ADDRESS#", perosonObj.getEmailId());
	}

	public static String getUpdatePersonWorkRelJSON(Person perosonObj) {
		return UPDATE_PERSON_WORK_REL_TEMPLATE.replaceAll("#DEFAULT_EXPENSE_ACCOUNT#", perosonObj.getDefaultExpCode())
				.replaceAll("#LOCATION_CODE#", perosonObj.getLocCode());
	}

	public static String getUpdatePersonMgrJSON(Person perosonObj) {
		return UPDATE_PERSON_MGR_TEMPLATE.replaceAll("#MANAGER_ASSIGNMENT_NUMBER#", "E" + perosonObj.getMgrCode());
	}
	
	public static String getTerminatePersonNamesJSON(Person perosonObj) {
		return TERMINATE_PERSON_TEMPLATE;
	}


	
	public static String getTerminatePersonURL(String workRelURL, String serviceID) {
		return workRelURL + "/" + serviceID + "/action/terminate";
	}

	public static String getLinkURLByType(String JSONString, String linkType) throws ParseException {
		JSONObject jo = (JSONObject) new JSONParser().parse(JSONString);
		JSONObject personObj = null;
		if(jo.get("items") != null) {
			personObj = (JSONObject) ((JSONArray) jo.get("items")).get(0);
		} else {
			personObj = (JSONObject) jo;
		}
		START_DATE = personObj.get("StartDate")!= null? personObj.get("StartDate").toString():"2014-01-01"; 
		JSONArray personLinksArray = ((JSONArray) personObj.get("links"));
		Iterator itr = personLinksArray.iterator();
		String hrefWorkRelURL = null;
		while (itr.hasNext()) {
			Iterator linkObj = ((Map) itr.next()).entrySet().iterator();
			while (linkObj.hasNext()) {
				Map.Entry pair = (Entry) linkObj.next();
				String key = pair.getKey().toString();
				String value = pair.getValue().toString();
				if (key.equalsIgnoreCase("href") && value.contains(linkType))
					hrefWorkRelURL = value;
				//System.out.println(key + " : " + value);
			}
		}
		return hrefWorkRelURL;
	}

	public static String getServiceId(String JSONString) throws ParseException {
		JSONObject jo = (JSONObject) new JSONParser().parse(JSONString);
		JSONObject workRelObj = (JSONObject) ((JSONArray) jo.get("items")).get(0);
		String serviceId = workRelObj.get("PeriodOfServiceId").toString();
		return serviceId;

	}
	
	public static String getFirstLinkHashURL(String JSONString) throws ParseException {
		JSONObject jo = (JSONObject) new JSONParser().parse(JSONString);
		JSONObject personObj = (JSONObject) ((JSONArray) jo.get("items")).get(0);
		JSONArray personLinksArray = ((JSONArray) personObj.get("links"));
		JSONObject workerHashObj = (JSONObject) personLinksArray.get(0);
		String workerHashURL = workerHashObj.get("href").toString();
		return workerHashURL;

	}
	
	public static Logger getLogger() {
		final Logger LOGGER = Logger.getLogger(YMCAUtils.class.getName());
		Handler fileHandler = null;
        Formatter simpleFormatter = null;
        try{
             
            // Creating FileHandler
            fileHandler = new FileHandler("YMCA_HCM_Integration.log");
             
            // Creating SimpleFormatter
            simpleFormatter = new SimpleFormatter();
             
            // Assigning handler to logger
            LOGGER.addHandler(fileHandler);
             
            // Logging message of Level info (this should be publish in the default format i.e. XMLFormat)
            LOGGER.info("Initalizing the Logger..");
             
            // Setting formatter to the handler
            fileHandler.setFormatter(simpleFormatter);
             
            // Setting Level to ALL
            fileHandler.setLevel(Level.ALL);
            if(LOG_LEVEL != null && LOG_LEVEL.equalsIgnoreCase("FINEST")) {
            	LOGGER.setLevel(Level.FINEST);
            }else {
            	LOGGER.setLevel(Level.INFO);
            }
			/*
			 * switch(LOG_LEVEL) { case "INFO": LOGGER.setLevel(Level.INFO); break; case
			 * "FINEST": LOGGER.setLevel(Level.FINEST); break; }
			 */
            
             
        }catch(IOException exception){
            LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", exception);
        }
        return LOGGER;
	}
	
	public static String getEncodedString() {
		return Base64.getEncoder().encodeToString(
				(BASIC_AUTH_USERNAME + ":" + BASIC_AUTH_PASSWORD).getBytes(StandardCharsets.UTF_8));
	}
	
	public static void generateReport() {
		String COMMA = ",";
		String NEWLINE = "\n";
		FileWriter myWriter = null;		
		try {
			reportFileName = "IntegrationReport_"+ getCurrentDateTime("yyyy-MM-dd hh:mm:ss")+".csv";
			myWriter = new FileWriter(reportFileName);
			myWriter.write(REPORT_HEADER_ROW+"\n");
			for(Person p: reportList) {
				myWriter.write(p.getpNumber() + COMMA + p.getACTION() + COMMA + p.getDATE() + COMMA + p.getSTATUS() + COMMA + "\"" + p.getCOMMENTS() + "\"" + NEWLINE);
			}
		} catch (Exception e) {
			YMCAUtils.LOGGER.severe("ERROR GENERATING REPORT: "+e.getMessage());
		}finally {
			try {
				myWriter.close();
			} catch (IOException e) {
				YMCAUtils.LOGGER.severe("ERROR WRITTING TO REPORT: "+e.getMessage());
			}
		}
	}
	
	public static String getCurrentDateTime(String dateFormat) {
		dateFormat = dateFormat != null? dateFormat: "yyyy-MM-dd";
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);  
	    Date date = new Date();  
	    String dateStr = formatter.format(date);
	    return dateStr.replace( ":" , "." );
	}
	
	public static void calculatePassPercent() {
		sbr.append("TOTAL ENTRIES PROCESSED: "+PROCESSED_ENTRIES_COUNT);
		sbr.append("\n");
		sbr.append("CREATE REQUESTS SUCCEEDD : "+CREATE_PASS_COUNT);
		sbr.append("\n");
		sbr.append("CREATE REQUESTS FAILED: "+CREATE_FAIL_COUNT);
		sbr.append("\n");
		sbr.append("TERMINATE REQUESTS SUCCEEDD : "+TERMINATE_PASS_COUNT);
		sbr.append("\n");
		sbr.append("TERMINATE REQUESTS FAILED: "+TERMINATE_FAIL_COUNT);
		sbr.append("\n");
		sbr.append("UPDATE REQUESTS SUCCEEDD : "+UPDATE_PASS_COUNT);
		sbr.append("\n");
		sbr.append("UPDATE REQUESTS FAILED: "+UPDATE_FAIL_COUNT);
		sbr.append("\n");
	}
	
	public static void sendEmail(){
		
		  LOGGER.info("GENERATING REPORT");
		  generateReport();
		
	      Properties props = new Properties();
	      props.put("mail.smtp.auth", auth);
	      props.put("mail.smtp.starttls.enable", tlsenabled);
	      props.put("mail.smtp.host", host);
	      props.put("mail.smtp.port", port);
	      
	      String ErrMsgSubject = "[IMPORTANT] INTEGRATION JOB FAILED WITH AN UNEXPECTED ERROR !!!";
	      String ErrMsgBody = "SOMETHING IS NOT RIGHT, INTEGRATION JOB TRIED TO RUN BUT FAILED DUE TO SOME FATAL ERROR!!! \n CHECK THE LOG FILE GENERATED FOR MORE DETAILS.";
	      
	      
	      
	      // Get the Session object.
	      Session session = Session.getInstance(props,
	         new javax.mail.Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	               return new PasswordAuthentication(user, password);
	            }
	         });

	      try {
	    	  int processedPercent = 0;
	    	  
	    	  if(PROCESSED_ENTRIES_COUNT >0)
	    		  processedPercent = ((CREATE_PASS_COUNT + TERMINATE_PASS_COUNT + UPDATE_PASS_COUNT)/ PROCESSED_ENTRIES_COUNT) * 100;
	    	  
	    	  // Create a default MimeMessage object.
	         Message message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(mailFrom));

	         // Set To: header field of the header.
	         message.setRecipients(Message.RecipientType.TO,
	            InternetAddress.parse(mailTo));

	         // Set Subject: header field
	         calculatePassPercent();
	         mailSubject =  "["+ RUN_MODE + "]  [" + processedPercent + "% PASSED]  " + mailSubject;
	         if(fatalErr) {
	        	 mailSubject =  ErrMsgSubject;
	         }
	         message.setSubject(mailSubject);

	         // Create the message part
	         BodyPart messageBodyPart = new MimeBodyPart();

	         // Now set the actual message
	         if(fatalErr) {
	        	mailBody = ErrMsgBody; 
	         } 
	         messageBodyPart.setText(mailBody +"\n\n"+ sbr.toString());

	         // Create a multipart message
	         Multipart multipart = new MimeMultipart();

	         // Set text message part
	         multipart.addBodyPart(messageBodyPart);

	         // Part two is attachment
	         messageBodyPart = new MimeBodyPart();
	         DataSource source = new FileDataSource(reportFileName);
	         messageBodyPart.setDataHandler(new DataHandler(source));
	         messageBodyPart.setFileName(reportFileName);
	         multipart.addBodyPart(messageBodyPart);

	         // Send the complete message parts
	         message.setContent(multipart);
	         
	         // Send message
	         YMCAUtils.LOGGER.info("SENDING EMAIL..");
	         Transport.send(message);
	         
	         YMCAUtils.LOGGER.info("EMAIL SENT SUCCESSFUL!");
	         
	      } catch (Exception e) {
	    	  YMCAUtils.LOGGER.severe("ERROR SENDING EMAIL!"+e.getMessage());
	      }
		
	}

}
