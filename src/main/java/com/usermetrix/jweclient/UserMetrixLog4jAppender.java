package com.usermetrix.jweclient;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Level;
import org.slf4j.MDC;

public class UserMetrixLog4jAppender extends AppenderSkeleton {
	protected void append(LoggingEvent loggingEvent) {
		switch (loggingEvent.getLevel().toInt()) {
		case Level.ERROR_INT:
		case Level.WARN_INT:
			Class<?> sourceClass;
			try {
				sourceClass = Class.forName(loggingEvent.getLoggerName());
			} catch (ClassNotFoundException e) {
				// Unable to find the class that we are logging - so just use
				// the
				// base object as the source name.
				sourceClass = Object.class;
			}

			try {
				// If logging event contains details of something that has been
				// thrown - pump that into the UserMetrix logging event.
				String session = MDC.get("sessionId");
				if (loggingEvent.getThrowableInformation() != null) {					
					if (session != null) {
						UserMetrix.error(session, sourceClass, loggingEvent
								.getMessage().toString(), loggingEvent
								.getThrowableInformation().getThrowable());
					}

					// Nothing has been thrown - just do a vanilla error log
					// event.
				} else {
					if (session != null) {
						UserMetrix.error(session, sourceClass, loggingEvent
								.getMessage().toString());
					}
				}
			} catch (Exception e) {
				System.err.println("Warning: Unable capture information with UserMetrix: " + e);
			}

			break;

		default:
			// We ignore all other logging levels - we are only really
			// interested in
			// errors and warnings for this stuff.
			break;
		}
	}

	public void close() {
		UserMetrix.shutdownAll();
	}

	public boolean requiresLayout() {
		return false;
	}
}
