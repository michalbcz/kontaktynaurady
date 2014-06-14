package cz.rhok.prague.osf.governmentcontacts.scraper;

import java.io.IOException;

public class UnableToConnectToServer extends RuntimeException implements CanBeRepeatedException {

	public UnableToConnectToServer(String message, Exception ex) {
		super(message, ex);
	}

	
}
