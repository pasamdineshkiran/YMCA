package com.ymca.integration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class YMCAPersonServiceManager {

	// GET Request - Generic
	public static String getResponseFromGETRequest(String URL) {

		HttpsURLConnection conn = null;
		StringBuilder responseBuilder = new StringBuilder();
		String errRespMsg = null;
		try {

			URL url = new URL(URL);
			conn = (HttpsURLConnection) url.openConnection();
			String encoded = YMCAUtils.getEncodedString();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Basic " + encoded);
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");

			if (conn.getResponseCode() >= 400) {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				String output = null;
				StringBuilder sb = new StringBuilder();
				while ((output = br.readLine()) != null) {
					sb.append(output);
				}
				errRespMsg = conn.getResponseMessage() + " | " + conn.getResponseCode() + " | " + sb.toString();
				YMCAUtils.LOGGER.severe("GET REQUEST FAILED WITH RESPONSE BODY : " + sb.toString());
				throw new RuntimeException("ERROR GETTING PERSON: " + errRespMsg);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			while ((output = br.readLine()) != null) {
				responseBuilder.append(output);
			}
			YMCAUtils.LOGGER.finest("GET RESPONSE BODY : " + responseBuilder.toString());
		} catch (Exception e) {
			YMCAUtils.LOGGER.severe("ERROR OCCURED GETTING PERSON INFO: " + e.getMessage());
		} finally {
			conn.disconnect();
		}
		return responseBuilder.toString();
	}

	// POST Request - Terminate
	public static void terminatePersonService(int personId) {
		YMCAUtils.LOGGER.info("-------------------------------------------------");
		YMCAUtils.LOGGER.info("TERMINATE  request for person: " + personId);
		YMCAUtils.PROCESSED_ENTRIES_COUNT++;
		HttpsURLConnection conn = null;
		String errRespMsg = null;
		try {

			String personQueryURL = YMCAUtils.BASE_URL + "workers?q=PersonNumber=" + personId;
			String personJson = getResponseFromGETRequest(personQueryURL);
			String workRelHrefURL = YMCAUtils.getLinkURLByType(personJson, "child/workRelationships");

			String workRelJson = getResponseFromGETRequest(workRelHrefURL);
			String serviceId = YMCAUtils.getServiceId(workRelJson);

			String terminationURL = YMCAUtils.getTerminatePersonURL(workRelHrefURL, serviceId);

			URL url = new URL(terminationURL);
			conn = (HttpsURLConnection) url.openConnection();
			String encoded = YMCAUtils.getEncodedString();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/vnd.oracle.adf.action+json");
			conn.setRequestProperty("Authorization", "Basic " + encoded);
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");

			String input = YMCAUtils.TERMINATE_PERSON_TEMPLATE;
			YMCAUtils.LOGGER.finest("TERMINATE  PAYLOAD: " + input);
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() >= 400) {
				YMCAUtils.TERMINATE_FAIL_COUNT++;
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				String output = null;
				StringBuilder sb = new StringBuilder();
				while ((output = br.readLine()) != null) {
					sb.append(output);
				}
				errRespMsg = conn.getResponseMessage() + " | " + conn.getResponseCode() + " | " + sb.toString();
				YMCAUtils.LOGGER.info("TERMINATE REQUEST FAILED WITH RESPONSE BODY : " + sb.toString());
				throw new RuntimeException("ERROR TERMINATING PERSON: " + errRespMsg);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			StringBuilder sb = new StringBuilder();
			YMCAUtils.LOGGER.finest("RESPONSE FROM SERVER..");
			// System.out.println("Response from Server .... \n");
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			YMCAUtils.LOGGER.finest(output);
			YMCAUtils.LOGGER.info("TERMINATE REQUEST SUCCESS!");
			YMCAUtils.updateStatusMessage(personId, YMCAUtils.MSG_DONE, YMCAUtils.ACTION_CODE_TERMINATE,
					YMCAUtils.MSG_SUCCESS);
			YMCAUtils.TERMINATE_PASS_COUNT++;
			YMCAUtils.reportList.add(new Person(personId + "", YMCAUtils.ACTION_CODE_TERMINATE,
					YMCAUtils.getCurrentDateTime("yyyy-MM-dd"), "PASS", ""));
		} catch (Exception e) {
			YMCAUtils.LOGGER.severe("TERMINATE REQUEST EXCEPTION STACK: " + e.getMessage());
			YMCAUtils.updateStatusMessage(personId, errRespMsg, YMCAUtils.ACTION_CODE_TERMINATE, YMCAUtils.MSG_FAILED);
			YMCAUtils.reportList.add(new Person(personId + "", YMCAUtils.ACTION_CODE_TERMINATE,
					YMCAUtils.getCurrentDateTime("yyyy-MM-dd"), "FAIL", errRespMsg));
		} finally {
			conn.disconnect();
		}
	}

	// POST Request - Create
	public static void createPersonService(Person person) {
		YMCAUtils.LOGGER.info("-------------------------------------------------");
		YMCAUtils.LOGGER.info("CREATE request for person: " + person.toString());
		YMCAUtils.PROCESSED_ENTRIES_COUNT++;
		HttpsURLConnection conn = null;
		String errRespMsg = null;
		try {
			URL url = new URL(YMCAUtils.BASE_URL + "workers");
			conn = (HttpsURLConnection) url.openConnection();
			String encoded = YMCAUtils.getEncodedString();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Basic " + encoded);
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");

			String input = YMCAUtils.getCreatePersonJSON(person);
			YMCAUtils.LOGGER.finest("Create Request JSON: " + input);
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() >= 400) {
				YMCAUtils.CREATE_FAIL_COUNT++;
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				String output = null;
				StringBuilder sb = new StringBuilder();
				while ((output = br.readLine()) != null) {
					sb.append(output);
				}
				errRespMsg = conn.getResponseMessage() + " | " + conn.getResponseCode() + " | " + sb.toString();
				YMCAUtils.LOGGER.severe("CREATE REQUEST FAILED WITH RESPONSE BODY : " + sb.toString());
				throw new RuntimeException("ERROR CREATING PERSON: " + errRespMsg);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			StringBuilder sb = new StringBuilder();
			YMCAUtils.LOGGER.finest("Response from server: " + errRespMsg);
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			YMCAUtils.LOGGER.finest(output);
			YMCAUtils.LOGGER.info("CREATE REQUEST FOR PERSON SUCCESS!");
			YMCAUtils.CREATE_PASS_COUNT++;
			YMCAUtils.updateStatusMessage(Integer.valueOf(person.getpNumber()), YMCAUtils.MSG_DONE,
					YMCAUtils.ACTION_CODE_CREATE, YMCAUtils.MSG_SUCCESS);
			YMCAUtils.reportList.add(new Person(person.getpNumber(), YMCAUtils.ACTION_CODE_CREATE,
					YMCAUtils.getCurrentDateTime("yyyy-MM-dd"), "PASS", ""));

		} catch (Exception e) {
			YMCAUtils.LOGGER.severe(e.getMessage());
			YMCAUtils.updateStatusMessage(Integer.valueOf(person.getpNumber()), errRespMsg,
					YMCAUtils.ACTION_CODE_CREATE, YMCAUtils.MSG_FAILED);
			YMCAUtils.reportList.add(new Person(person.getpNumber(), YMCAUtils.ACTION_CODE_CREATE,
					YMCAUtils.getCurrentDateTime("yyyy-MM-dd"), "FAIL", errRespMsg));
		} finally {
			conn.disconnect();
		}
	}

	// PATCH Request - Update
	public static void updatePersonServiceForURL(String inputURL, String payLoad, int pNumber, Person p, String updateAction) {
		YMCAUtils.LOGGER.info("-------------------------------------------------");
		YMCAUtils.LOGGER.info("UPDATE request for person: " + pNumber);
		YMCAUtils.LOGGER.finest("UPDATE request payload: " + payLoad);
		YMCAUtils.LOGGER.finest("UPDATE request URL: " + inputURL);
		HttpsURLConnection conn = null;
		String errRespMsg = null;
		try {
			URL url = new URL(inputURL);
			conn = (HttpsURLConnection) url.openConnection();
			String encoded = YMCAUtils.getEncodedString();
			conn.setDoOutput(true);
			conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
			conn.setRequestMethod("POST");
			// conn.setRequestMethod("PATCH");
			conn.setRequestProperty("Effective-Of",
					"RangeMode=UPDATE;RangeStartDate=" + YMCAUtils.getCurrentDateTime("yyyy-MM-dd") + ";RangeEndDate=4712-12-31"); // FIXME:
																														// UPDATE
			conn.setRequestProperty("Authorization", "Basic " + encoded);
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStream os = conn.getOutputStream();
			os.write(payLoad.getBytes());
			os.flush();

			if (conn.getResponseCode() >= 400) {
				p.setUpdateActionFlag(p.getUpdateActionFlag() + 1);
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				String output = null;
				StringBuilder sb = new StringBuilder();
				while ((output = br.readLine()) != null) {
					sb.append(output);
				}
				errRespMsg = conn.getResponseMessage() + " | " + conn.getResponseCode() + " | " + sb.toString();
				YMCAUtils.LOGGER.severe("UPDATE REQUEST FAILED WITH RESPONSE BODY : " + sb.toString());
				throw new RuntimeException("ERROR UPDATING PERSON: " + errRespMsg);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			StringBuilder sb = new StringBuilder();
			YMCAUtils.LOGGER.info("Response from server: " + errRespMsg);
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			YMCAUtils.LOGGER.finest(output);
			YMCAUtils.LOGGER.info("UPDATE REQUEST FOR PERSON SUCCESS!");
			YMCAUtils.updateStatusMessage(pNumber, YMCAUtils.MSG_DONE, YMCAUtils.ACTION_CODE_UPDATE,
					YMCAUtils.MSG_SUCCESS);
			YMCAUtils.reportList.add(
					new Person(pNumber + "", YMCAUtils.ACTION_CODE_UPDATE + " | " + updateAction, YMCAUtils.getCurrentDateTime("yyyy-MM-dd"), "PASS", ""));
		} catch (Exception e) {
			YMCAUtils.LOGGER.severe(e.getMessage());
			YMCAUtils.updateStatusMessage(pNumber, errRespMsg, YMCAUtils.ACTION_CODE_UPDATE, YMCAUtils.MSG_FAILED);
			YMCAUtils.reportList.add(new Person(pNumber + "", YMCAUtils.ACTION_CODE_UPDATE + " | " + updateAction,
					YMCAUtils.getCurrentDateTime("yyyy-MM-dd"), "FAIL", errRespMsg));
		} finally {
			conn.disconnect();
		}
	}

	public static void updatePersonWorkRelService(Person person) {
		try {
			YMCAUtils.PROCESSED_ENTRIES_COUNT++;
			int pNumber = Integer.parseInt(person.getpNumber());
			String personQueryURL = YMCAUtils.BASE_URL + "workers?q=PersonNumber=" + person.getpNumber();
			String personJson = YMCAPersonServiceManager.getResponseFromGETRequest(personQueryURL);
			String namesURL = YMCAUtils.getLinkURLByType(personJson, "child/names");
			String namesHashJSON = getResponseFromGETRequest(namesURL);
			String namesHashURL = YMCAUtils.getFirstLinkHashURL(namesHashJSON);
			updatePersonServiceForURL(namesHashURL, YMCAUtils.getUpdatePersonNamesJSON(person), pNumber, person, "NAME");

			if (person.getUpdateActionFlag() == 0) {
				String emailURL = YMCAUtils.getLinkURLByType(personJson, "child/emails");
				String emailHashJSON = getResponseFromGETRequest(emailURL);
				String emailHashURL = YMCAUtils.getFirstLinkHashURL(emailHashJSON);
				updatePersonServiceForURL(emailHashURL, YMCAUtils.getUpdatePersonEMailJSON(person), pNumber, person, "EMAIL");
			}

			String assignHashJSON = null;
			if (person.getUpdateActionFlag() == 0) {
				String workRelURL = YMCAUtils.getLinkURLByType(personJson, "child/workRelationships");
				String workRelHashJson = getResponseFromGETRequest(workRelURL);
				String workRelHashURL = YMCAUtils.getFirstLinkHashURL(workRelHashJson);
				String workRelJson = getResponseFromGETRequest(workRelHashURL);
				String workRelAssignURL = YMCAUtils.getLinkURLByType(workRelJson, "child/assignments");
				assignHashJSON = getResponseFromGETRequest(workRelAssignURL);
				String assignmentHashURL = YMCAUtils.getFirstLinkHashURL(assignHashJSON);
				updatePersonServiceForURL(assignmentHashURL, YMCAUtils.getUpdatePersonWorkRelJSON(person), pNumber,
						person, "WORK RELATIONSHIP");
			}

			if (person.getUpdateActionFlag() == 0) {
				String managerURL = YMCAUtils.getLinkURLByType(assignHashJSON, "child/managers");
				String mgrHashURL = YMCAUtils.getFirstLinkHashURL(getResponseFromGETRequest(managerURL));
				updatePersonServiceForURL(mgrHashURL, YMCAUtils.getUpdatePersonMgrJSON(person), pNumber, person, "MANAGER");
			}

			if (person.getUpdateActionFlag() > 0) {
				YMCAUtils.UPDATE_FAIL_COUNT++;
			} else {
				YMCAUtils.UPDATE_PASS_COUNT++;
			}

		} catch (Exception ex) {
			YMCAUtils.LOGGER.info("ERROR OCCURED WHILE UPDATING PERSON RECORD | PERSON ID: " + person.getpNumber());
		}

	}

}
