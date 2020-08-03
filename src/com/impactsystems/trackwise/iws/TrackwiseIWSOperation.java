package com.impactsystems.trackwise.iws;

import com.impactsystems.printWidget.common.LogUtil;
import com.impactsystems.printWidget.common.PropertyClass;
import com.spartasystems.model.enums.DateFormatEnum;
import com.spartasystems.model.enums.LoginAccountTypeEnum;
import com.spartasystems.model.pr.Pr;
import com.spartasystems.model.transport.response.Message;
import com.spartasystems.model.transport.response.authentication.LoginResponse;
import com.spartasystems.model.transport.response.pr.EditPrResponse;
import com.spartasystems.model.transport.request.authentication.LoginRequest;
import com.spartasystems.model.transport.request.pr.SavePrRequest;
import com.spartasystems.model.transport.response.pr.NewPrResponse;
import com.spartasystems.model.transport.response.pr.SavePrResponse;
import com.spartasystems.model.util.DateConversionUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class TrackwiseIWSOperation {
	
	private final static Logger logger = LogUtil.getLogger(TrackwiseIWSOperation.class.getName());
	
	public static void main(String[] args) {

		try {

			TrackwiseIWSOperation twOpr = new TrackwiseIWSOperation();

			String twsServicePath = PropertyClass.getPropertyLabel("twservicepath");
			String twuser = PropertyClass.getPropertyLabel("twuser");
			String twpass = PropertyClass.getPropertyLabel("twpass");
			String timeZone = PropertyClass.getPropertyLabel("timezone");

			WebResource resource = twOpr.getWebResource(twsServicePath);
			ObjectMapper mapper = new ObjectMapper();

			String jSessionId = twOpr.twLogin(resource, mapper, twuser, twpass, timeZone);
			if(jSessionId != null && !jSessionId.equals("")) {
				twOpr.viewExtPr(resource, mapper, jSessionId);
				Pr newPr = twOpr.createPr(resource, mapper, jSessionId);
				twOpr.savePr(resource, mapper, newPr, jSessionId);
			}
		
		} catch (Exception e) {
			logger.debug("EXCEPTION_OCCURED_IN_MAIN  : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	private void viewExtPr(WebResource resource, ObjectMapper mapper,String jSessionId) {
		
		try {
			
			  ClientResponse clientResponse = resource.path("/prs/view/" + 1002)
                      .accept(MediaType.APPLICATION_JSON)
                      .type(MediaType.APPLICATION_JSON)
                      .cookie(new Cookie("JSESSIONID", jSessionId))
                      .get(ClientResponse.class);
			
			if (clientResponse.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "+clientResponse.getStatus());
			}
			
			String viewPRResponseString = clientResponse.getEntity(String.class);
			System.out.print("viewPRResponseString---"+viewPRResponseString.trim());
			logger.debug("@@ViewPRResponseString : "+viewPRResponseString);
			
		}catch(Exception e) {
			System.out.println("error");
			logger.debug("@@EXCEPTION_OCCURED : "+e.getMessage());
		}
		
		
	}
		
	private Pr createPr(WebResource resource, ObjectMapper mapper,String jSessionId) throws Exception{
		Pr newPr = null;
		try {

			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("classname", "NewPrRequest");
			queryParams.add("projectId", "1");
			queryParams.add("divisionId", "1");
			
			String newPrResponseString = resource.path("/prs/new")
										.queryParams(queryParams)
										.accept(MediaType.APPLICATION_JSON)
					                    .type(MediaType.APPLICATION_JSON)
										.cookie(new Cookie("JSESSIONID", jSessionId))
										.get(String.class);
			
			NewPrResponse newPrResponse = (NewPrResponse)mapper.readValue(newPrResponseString, NewPrResponse.class);
			
			if(!newPrResponse.getSuccess()) {
				throw new Exception("Error Code : "+newPrResponse.getMessages());
			}else {
				System.out.println("New Pr Created successfully..");
			}
			
			newPr = newPrResponse.getPr();
			
			logger.debug("NewPrResponseString : "+newPrResponseString);
			
			
		} catch (Exception e) {
			logger.debug("EXCEPTION_OCCURED : " + e.getMessage());
		}
		
		return newPr;
	}
	
	/**
	 * trackWiseLogin : This method is for login to trackwise.
	 * @param resource
	 * @param mapper
	 * @param twUser
	 * @param twPass
	 * @param timeZone
	 * @return parsedjSessionId
	 * @throws Exception
	 */	
	private String twLogin(WebResource resource, ObjectMapper mapper, String twUser, String twPass, String timeZone) throws Exception {
		logger.debug("ENTER_IN_METHOD twLogin");
		String jSessionId = null;
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUserName(twUser);
		loginRequest.setPassword(twPass);
		loginRequest.setTimeZone(timeZone);
		loginRequest.setLanguageId(1);
		loginRequest.setForceLogin(Boolean.valueOf(true));
		loginRequest.setUserType(LoginAccountTypeEnum.WebServices);
		String isoDate = DateConversionUtil.convertDateTimeToISO8601String(new Date(), true);
		loginRequest.setDateTime(isoDate);
		loginRequest.setLastCacheUpdateDateTime(isoDate);
		
	  try {
			String loginRequestString = mapper.writeValueAsString(loginRequest);
		   
			ClientResponse clientResponse = resource.path("/login")
					.type(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.post(ClientResponse.class, loginRequestString);
			
		    if (clientResponse.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "+clientResponse.getStatus());
			}
			
		    String loginResponseString = (String)clientResponse.getEntity(String.class);
			System.out.println("@@LoginResponseString---->"+loginResponseString);
			logger.debug("@@LoginResponseString---->"+loginResponseString);
			
			LoginResponse loginResponse = mapper.readValue(loginResponseString, LoginResponse.class);
			if(loginResponse.getSuccess()) {
				jSessionId = getParsedLoginSessionID(clientResponse);
			}
			
			List<Message> loginMessages = loginResponse.getMessages();
			if (loginMessages != null) {
				for (Message message : loginMessages) {
					logger.log(Level.ERROR, "Code:" + message.getCode() + " Type:" + message.getType() + " Message:"
							+ message.getMessage() + " Developer Message:" + message.getDeveloperMessage());
				}

				logger.log(Level.DEBUG, "EXIT_FROM_METHOD trackWiseLogin");
				throw new RuntimeException("TrackWise Login Error");
			}
			else {
				logger.log(Level.DEBUG, "User " + twUser + "/" + twPass + " logged in at "
						+ DateConversionUtil.convertISO8601StringToDateTimeFormatted(
								isoDate,
								DateFormatEnum.MMddyyyy,
								true,
								TimeZone.getDefault()));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("Operation twLogin Fails at " + e.getMessage());
		} 
		logger.debug("EXIT_FROM_METHOD twLogin");
		return jSessionId;	
	}
	
	private WebResource getWebResource(String twServicePath) throws Exception {
		logger.debug("ENTER_IN_METHOD getWebResource");
		DefaultApacheHttpClientConfig cc = new DefaultApacheHttpClientConfig();
		cc.getProperties().put("com.sun.jersey.impl.client.httpclient.handleCookies", Boolean.valueOf(true));
		Client client = ApacheHttpClient.create(cc);
		WebResource resource = client.resource(twServicePath);
		logger.debug("EXIT_FROM_METHOD getWebResource");
		return resource;
		
	}
		
	/**
	 * getLoginSessionID() : This method for get jSessionId of loginResponse.
	 * @param clientResponse.
	 * @return parsedjSessionId to twLogin
	 * @throws Exception
	 */
	
	private String getParsedLoginSessionID(ClientResponse response) {
		String jSessionID = null;
		try {

				String value = response.getHeaders().toString();
				if (value.contains("JSESSIONID")) {
						int index = value.indexOf("JSESSIONID=");
						int endIndex = value.indexOf(";", index);
						String loginSessionID = value.substring(index + "JSESSIONID=".length(), endIndex);
						if (loginSessionID != null) {
							jSessionID = loginSessionID;
						}
				}
		} catch (Exception e) {
			logger.debug("EXCEPTION_OCCURED_IN_GET_JSESSIONID : " + e.getMessage());
		}
		return jSessionID;
	}
	
	/**
	 * savePr() : TrackWise savePr request to save updated information.
	 * @param resource
	 * @param mapper
	 * @param pr
	 * @param jSessionId 
	 * @return
	 * @throws Exception
	 */
	
	private void savePr(WebResource resource, ObjectMapper mapper, Pr pr, String jSessionId) throws Exception {

		SavePrRequest saveNewPrRequest = new SavePrRequest();
		saveNewPrRequest.setPr(pr);
		String saveNewPrResponseString;
		SavePrResponse saveNewPrResponse = null;
		try {
			saveNewPrResponseString = resource.path("/prs/save")
									.type(MediaType.APPLICATION_JSON)
									.accept(MediaType.APPLICATION_JSON)
									.cookie(new Cookie("JSESSIONID", jSessionId))
									.post(String.class, new ObjectMapper()
									.writeValueAsString(saveNewPrRequest));
			
			saveNewPrResponse = mapper.readValue(saveNewPrResponseString, SavePrResponse.class);
			
			if (saveNewPrResponse.getSuccess() == false) {
					if (saveNewPrResponse.getMessages() != null) {
						for (Message message : saveNewPrResponse.getMessages()) {
							logger.debug("    Code:" + message.getCode() + " Type:" + message.getType() + " Message:"
									+ message.getMessage() + " developerMessage:" + message.getDeveloperMessage());
						}
					}
			} else {
					logger.debug("New PR " + saveNewPrResponse.getPrId() + " Created Successfully into trackwise web.");
			}
		
		} catch (Exception e) {
			logger.debug("EXCEPTION_OCCURED_IN_SAVEPR_METHOD : " + e.getMessage());
		}
	}
	
}
