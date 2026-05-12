package org.octri.fhir_sandbox.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.octri.authentication.server.security.entity.User;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.domain.SandboxStatus;
import org.octri.fhir_sandbox.domain.SmartClient;
import org.octri.fhir_sandbox.repository.SandboxRepository;
import org.octri.fhir_sandbox.repository.SmartClientRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Business logic for working with {@link Sandbox} entities.
 */
@Service
public class SandboxService {

	private static final Log log = LogFactory.getLog(SandboxService.class);

	private final SandboxRepository repository;
	private final SmartClientRepository sandboxClientRepository;
	private final PartitionService partitionService;
	private final SampleDataService sampleDataService;

	public SandboxService(SandboxRepository repository, SmartClientRepository sandboxClientRepository,
			PartitionService partitionService, SampleDataService sampleDataService) {
		this.repository = repository;
		this.sandboxClientRepository = sandboxClientRepository;
		this.partitionService = partitionService;
		this.sampleDataService = sampleDataService;
	}

	/**
	 * Finds all sandboxes, for all owners.
	 *
	 * TODO: Method security to ensure only admins can view all sandboxes.
	 *
	 * @return
	 */
	public Iterable<Sandbox> findAll() {
		return repository.findAll();
	}

	/**
	 * Finds sandboxes with the specified status (for all owners)
	 * 
	 * TODO: Method security to ensure only admins can view all sandboxes.
	 * 
	 * @param status
	 * @return
	 */
	public List<Sandbox> findByStatus(SandboxStatus status) {
		return repository.findByStatus(status);
	}

	/**
	 * Finds a sandbox by ID.
	 *
	 * @param id
	 * @return
	 */
	public Optional<Sandbox> findById(Long id) {
		return repository.findById(id);
	}

	/**
	 * Finds all sandboxes owned by the given user.
	 *
	 * TODO: Include sandboxes shared with the user, once sharing is implemented.
	 *
	 * @param user
	 * @return
	 */
	public List<Sandbox> getSandboxesForUser(User user) {
		return repository.findByOwner(user);
	}

	/**
	 * Finds all SMART on FHIR clients associated with the given sandbox.
	 *
	 * @param sandbox
	 * @return
	 */
	public List<SmartClient> getClientsForSandbox(Sandbox sandbox) {
		return sandboxClientRepository.findBySandboxId(sandbox.getId());
	}

	/**
	 * Saves a sandbox.
	 *
	 * @param sandbox
	 * @return
	 */
	public Sandbox save(Sandbox sandbox) {
		if (sandbox.getId() == null) {
			return createSandbox(sandbox);
		} else {
			return repository.save(sandbox);
		}
	}

	/**
	 * Creates a new sandbox, which includes creating a new partition on the FHIR server and saving the sandbox metadata
	 * in the database.
	 *
	 * @param sandbox
	 * @return
	 */
	public Sandbox createSandbox(Sandbox sandbox) {
		partitionService.createPartitionForSandbox(sandbox);
		sandbox.setStatus(SandboxStatus.INITIALIZING);
		Sandbox savedSandbox = repository.save(sandbox);
		return savedSandbox;
	}

	/**
	 * Deletes a sandbox, which includes deleting the partition on the FHIR server and removing the sandbox metadata
	 * from the database.
	 *
	 * TODO: Use method-level security to restrict deletion to admins or sandbox owner.
	 *
	 * @param sandbox
	 */
	public void delete(Sandbox sandbox) {
		partitionService.deletePartitionForSandbox(sandbox);
		repository.delete(sandbox);
	}

	/**
	 * Deletes a sandbox by ID. Finds the sandbox, then delegates to {@link #delete(Sandbox)}.
	 *
	 * @param id
	 */
	public void deleteById(Long id) {
		var sandbox = repository.findById(id).get();
		delete(sandbox);
	}

	/**
	 * Indicates whether the state of a sandbox permits deletion.
	 * 
	 * @param sandbox
	 * @return
	 */
	public Boolean isDeleteBlocked(Sandbox sandbox) {
		return !sandbox.getStatus().isDeletable();
	}

	/**
	 * Asynchronously performs setup tasks for sandboxes
	 * 
	 * First sets the status to INITIALIZING, then decides whether the load
	 * sample FHIR resources before setting the status to READY.
	 * 
	 * If loadSampleData throws an error, the status is instead updated to ERROR
	 * 
	 * @param sandbox
	 * @param importSampleData
	 */
	@Async
	public void initializeSandbox(Sandbox sandbox, Boolean importSampleData) {
		try {
			if (importSampleData) {
				sampleDataService.loadSampleData(getSandboxFhirUrl(sandbox));
			}
			sandbox.setStatus(SandboxStatus.READY);
		} catch (Exception e) {
			log.error("Error encountered loading sample resources", e);
			sandbox.setStatus(SandboxStatus.ERROR);
		}
		save(sandbox);
	}

	/**
	 * Constructs the FHIR server URL for the given sandbox, which is based on the base URL from the application
	 * properties and the partition name from the sandbox metadata.
	 *
	 * @param sandbox
	 * @return
	 */
	public String getSandboxFhirUrl(Sandbox sandbox) {
		return partitionService.getPartitionFhirUrl(sandbox.getServerPartitionName());
	}

}
