/**
 * Confidential Property of Impact System, Inc.
 * (c) Copyright Impact System, Inc. 2008.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by an Impact System, Inc. corporate officer.
 * 
 * The class is associated with loading a property file and reading values from it.
 * 
 * @author girish
 */

package com.impactsystems.printWidget.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;


/**
 * The Class PropertyClass.
 */
public class PropertyClass {

	/** The config properties constants. */
	private static Properties configPropertiesConstants = null;

	/**
	 * This method is used to retrieve the value of property from Config.properties file
	 * @param propertyName the property name
	 * @return the config messages property
	 */
	public static String getConfigMessagesProperty( String propertyName ) {

		String propertyMessage = "";

		try {
			if( configPropertiesConstants == null ) {
				configPropertiesConstants = new Properties();
				String propFileName = "Config.properties";

				InputStream inputStream = PropertyClass.class.getClassLoader().getResourceAsStream( propFileName );

				configPropertiesConstants.load( inputStream );
				if( inputStream == null ) {
					throw new FileNotFoundException( "property file '" + propFileName + "' not found in the classpath" );
				}

			}
			propertyMessage = configPropertiesConstants.getProperty( propertyName );

		} catch ( IOException ioe ) {
			//DfLogger.error( null, "Unable to load Config.properties file", null, ioe );

		}

		return propertyMessage;
	}

	/**
	 * Gets the property CPW message.
	 *
	 * @param propertyName the property name
	 * @param cpwMessageConstantsArguments the cpw message constants arguments
	 * @return the property CPW message
	 */
	public static String getPropertyCPWMessage( String propertyName, Object[] cpwMessageConstantsArguments ) {
		String format = MessageFormat.format( configPropertiesConstants.getProperty( propertyName ), cpwMessageConstantsArguments );
		return format;
	}

	
	/*This method is used for return property variable value.*/
	public static String getPropertyLabel(String propertyName) {
		String propValue = getConfigMessagesProperty(propertyName);
		return propValue;
	}
	
}
