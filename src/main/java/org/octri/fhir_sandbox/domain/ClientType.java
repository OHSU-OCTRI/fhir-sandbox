package org.octri.fhir_sandbox.domain;

import org.octri.common.view.Labelled;

/**
 * Enum representing the type of an OAuth2 Client, either CONFIDENTIAL or PUBLIC.
 */
public enum ClientType implements Labelled {

	CONFIDENTIAL, PUBLIC;

	@Override
	public String getLabel() {
		return this.name();
	}

}
