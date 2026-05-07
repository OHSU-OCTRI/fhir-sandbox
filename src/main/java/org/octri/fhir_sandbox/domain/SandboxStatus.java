package org.octri.fhir_sandbox.domain;

import org.octri.common.view.Labelled;

public enum SandboxStatus implements Labelled {

	INITIALIZING, READY, ERROR;

	@Override
	public String getLabel() {
		return name();
	}
}
