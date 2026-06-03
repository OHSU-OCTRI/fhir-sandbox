package org.octri.fhir_sandbox.domain;

import org.octri.common.view.Labelled;

public enum SandboxStatus implements Labelled {

	INITIALIZING, READY, ERROR;

	@Override
	public String getLabel() {
		return name();
	}

	public Boolean isReady() {
		return SandboxStatus.READY.equals(this);
	}

	public Boolean isDeletable() {
		return !SandboxStatus.INITIALIZING.equals(this);
	}
}
