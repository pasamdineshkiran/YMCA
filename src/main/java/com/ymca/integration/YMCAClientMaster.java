package com.ymca.integration;

import java.sql.SQLException;

public class YMCAClientMaster {
	
	public static void main(String[] args) {
		try{
			YMCAUtils.loadConfigurationProps();
			YMCAUtils.intialize();
			
			//YMCAUtils.getLogger();
			YMCAUtils.LOGGER.info("Getting the SQL connection");
			YMCAUtils.getSqlServConnection();
			
			YMCAUtils.LOGGER.info("Loading Person info for all action types from DB");
			YMCAUtils.loadAllPersonsActionsList();
			
			// CREATE PERSON RECORDS
			if(Boolean.valueOf(YMCAUtils.PERSON_CREATE)) {
				YMCAUtils.LOGGER.info("Initiating CREATE action for person records");
				for (Person person : YMCAUtils.createPersonList)
					YMCAPersonServiceManager.createPersonService(person);
			}

			// UPDATE
			if(Boolean.valueOf(YMCAUtils.PERSON_UPDATE)) {
				YMCAUtils.LOGGER.info("Initiating UPDATE action for person records");
				for (Person person : YMCAUtils.updatePersonList) {
					YMCAPersonServiceManager.updatePersonWorkRelService(person);
				}
			}
			
			// TERMINATE
			if(Boolean.valueOf(YMCAUtils.PERSON_TERMINATE)) {
				YMCAUtils.LOGGER.info("Initiating TERMINATE action for person records");
				for (Person person : YMCAUtils.terminatePersonList)
					YMCAPersonServiceManager.terminatePersonService(Integer.valueOf(person.getpNumber()));
			}

			YMCAUtils.sendEmail();
		}catch(Exception ex) {
			YMCAUtils.LOGGER.severe("******************************************");
			YMCAUtils.LOGGER.severe("**** SOMETHING IS NOT RIGHT !!!! ******");
			YMCAUtils.LOGGER.severe("******************************************");
			YMCAUtils.LOGGER.severe(ex.getMessage());
			YMCAUtils.fatalErr = true;
			YMCAUtils.sendEmail();
		}
		finally {
			try {
				if (YMCAUtils.conn != null && !YMCAUtils.conn.isClosed()) {
					YMCAUtils.conn.close();
				}
			} catch (SQLException ex) {
				YMCAUtils.LOGGER.severe("Error closing DB Connection : "+ ex.getMessage());
			}
			YMCAUtils.LOGGER.info("FINSIHED!\n------------");
		}

	}

}
