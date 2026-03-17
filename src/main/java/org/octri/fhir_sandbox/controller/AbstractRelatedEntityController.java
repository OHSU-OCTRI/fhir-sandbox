package org.octri.fhir_sandbox.controller;

import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityNotFoundException;

import org.octri.common.controller.AbstractBaseEntityController;
import org.octri.common.domain.AbstractEntity;
import org.octri.common.view.Labelled;
import org.octri.common.view.ViewUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Abstract controller with common functionality for child entities. RequestMapping routes should be defined in the
 * subclass. This class assumes that the parent path is contained within the route and that the placeholder value is
 * {parentId}.
 *
 * Ex. @RequestMapping("/data/scholar/{parentId}/scholar_institution")
 *
 * @author lawhead
 *
 * @param <P>
 *            - parent type
 * @param <T>
 *            - child type
 * @param <U>
 *            - child repository
 */
public abstract class AbstractRelatedEntityController<P extends AbstractEntity & Labelled, T extends AbstractEntity, U extends CrudRepository<T, Long>>
		extends AbstractBaseEntityController<T, U> {

	public abstract P findParent(Long parentId);

	public abstract List<T> findAllByParentId(Long parentId);

	/**
	 * Adds lists of selection options to the view model for use in rendering forms.
	 *
	 * @param model
	 * @param entity
	 * @return
	 */
	public abstract void addOptionLists(Map<String, Object> model, T entity, P parent);

	/**
	 * Used for tab interfaces to set the current tabName in the view model.
	 *
	 * @return
	 */
	public String tabName() {
		return null;
	};

	/**
	 * Adds common view attributes to the Model passed to the view template.
	 *
	 * @param model
	 */
	protected void addTemplateAttributes(Map<String, Object> model, P parent) {
		super.addTemplateAttributes(model);
		model.put("parentEntityName", this.parentEntityName(parent));
		model.put("baseRoute", this.baseRoute(parent.getId()));
	}

	/**
	 * List of children entities related to the current parent.
	 *
	 * @param model
	 * @param parentId
	 * @return
	 */
	@GetMapping("/")
	public String list(Map<String, Object> model, @PathVariable Long parentId) {
		P parent = findParent(parentId);
		this.addTemplateAttributes(model, parent);
		model.put("entity_list", this.findAllByParentId(parentId));
		model.put("entity", parent);
		model.put(this.tabName(), true);
		return template("list");
	}

	/**
	 * Displays details of a given child entity related to the current parent.
	 *
	 * @param model
	 * @param parentId
	 * @param request
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public String show(Map<String, Object> model, @PathVariable Long parentId, @PathVariable Long id) {
		checkLineage(parentId, id);
		String template = super.show(model, id);
		P parent = findParent(parentId);
		this.addTemplateAttributes(model, parent);
		model.put("cancelURL", this.listRoute(parentId));
		model.put("parentEntity", ViewUtils.entityWrapper(parent));
		model.put(this.tabName(), true);

		return template;
	}

	/**
	 * Displays the form for a new child entity related to the current parent.
	 *
	 * @param model
	 * @param id
	 * @return
	 */
	@GetMapping("/new")
	public String newEntity(Map<String, Object> model, @PathVariable Long parentId) {

		String template = super.newEntity(model);

		P parent = findParent(parentId);
		this.addTemplateAttributes(model, parent);
		addOptionLists(model, null, parent);

		model.put("cancelUrl", this.listRoute(parentId));
		model.put("parentEntity", ViewUtils.entityWrapper(parent));
		model.put(this.tabName(), true);

		return template;
	}

	/**
	 * Save a new child entity.
	 *
	 * @param model
	 * @param entity
	 * @param redirectAttributes
	 * @param parentId
	 * @return
	 */
	@PostMapping("/new")
	public String createEntity(Map<String, Object> model, @ModelAttribute("entity") T entity,
			BindingResult bindingResult, RedirectAttributes redirectAttributes, @PathVariable Long parentId) {
		super.create(model, entity, bindingResult, redirectAttributes);
		return listingRedirect(parentId);
	}

	/**
	 * Displays the form to edit an existing child entity related to the current parent.
	 *
	 * @param model
	 * @param parentId
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/{id}/edit")
	public String edit(Map<String, Object> model, @PathVariable Long parentId,
			@PathVariable Long id) {
		checkLineage(parentId, id);
		String template = super.edit(model, id);

		P parent = findParent(parentId);
		this.addTemplateAttributes(model, parent);
		addOptionLists(model, (T) model.get("entity"), parent);
		model.put("cancelUrl", this.listRoute(parentId));
		model.put("parentEntity", ViewUtils.entityWrapper(parent));
		model.put(this.tabName(), true);

		return template;
	}

	/**
	 * Updates the child entity related to the current parent.
	 *
	 * @param model
	 * @param entity
	 * @param redirectAttributes
	 * @param parentId
	 * @param id
	 * @return
	 */
	@PostMapping("/{id}/edit")
	public String update(Map<String, Object> model, @ModelAttribute("entity") T entity,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			@PathVariable Long parentId,
			@PathVariable Long id) {
		checkLineage(parentId, id);
		super.update(model, id, entity, bindingResult, redirectAttributes);
		return listingRedirect(parentId);
	}

	/**
	 * Deletes the given child entity.
	 *
	 * @param parentId
	 * @param id
	 * @param redirectAttributes
	 * @return
	 */
	@GetMapping("/{id}/delete")
	public String delete(@PathVariable Long parentId, @PathVariable Long id,
			RedirectAttributes redirectAttributes) {
		checkLineage(parentId, id);
		super.delete(id, redirectAttributes);
		return listingRedirect(parentId);
	}

	protected String baseRoute(Long parentId) {
		return this.baseRoute().replaceAll(parentIdPlaceholder(), parentId.toString());
	}

	protected String listRoute(Long parentId) {
		return this.baseRoute(parentId) + "/";
	}

	/**
	 * Regex for the route placeholder for the parent id.
	 *
	 * @return
	 */
	protected String parentIdPlaceholder() {
		return "\\{parentId\\}";
	}

	/**
	 *
	 * @return The string returned from a controller method that will redirect to the listing page.
	 */
	protected String listingRedirect(Long parentId) {
		return "redirect:" + listRoute(parentId);
	}

	/**
	 * Parent name displayed to the user in the UI.
	 *
	 * @return
	 */
	protected String parentEntityName(P parent) {
		return parent.getClass().getSimpleName();
	}

	/**
	 * Check that the child belongs to the parent and throw an {@link EntityNotFoundException} if not. This exception
	 * is handled by Controller Advice and will display a generic message to the user.
	 *
	 * @param parentId
	 * @param id
	 */
	protected void checkLineage(Long parentId, Long id) {
		if (findAllByParentId(parentId).stream().noneMatch(child -> child.getId().equals(id))) {
			throw new EntityNotFoundException();
		}
	}

}