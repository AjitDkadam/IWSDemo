
/**
 * Confidential Property of Impact System, Inc.
 * (c) Copyright Impact System, Inc. 2008.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by an Impact System, Inc. corporate officer.
 * 
 * LogUtil.java
 * This class is used to get Logger instance by passing calling class name as parameter to getLogger method.
 * @author girish
 */

package com.impactsystems.printWidget.common;

import org.apache.log4j.Logger;

/**
 * The Class LogUtil.
 */
public class LogUtil {

	/**
	 * method gets the calling class name and returns Logger instance to the calling class.
	 *
	 * @return Logger
	 */
	public static Logger getInstance() {
		String callingClassName = Thread.currentThread().getStackTrace()[ 2 ].getClass().getCanonicalName();
		return Logger.getLogger( callingClassName );
	}

	/**
	 * takes calling class name as parameter and creates Logger instance.
	 *
	 * @param logName
	 *            the log name
	 * @return Logger
	 */

	public static Logger getLogger( String logName ) {
		Logger logger = Logger.getLogger( logName );
		return logger;
	}
}
