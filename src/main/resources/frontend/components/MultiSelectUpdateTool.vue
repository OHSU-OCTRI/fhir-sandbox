<template>
  <h5>{{ getTranslation('selectedHeader') }}</h5>
  <div class="current-selections">
    <span
      v-for="entity in selectedEntities"
      :key="entity.id"
      :value="entity"
      class="badge me-1"
    >
      <i @click="deselectEntity(entity)" class="fa-solid fa-x"></i>
      {{ entity.label }}
    </span>
  </div>
  <h5>{{ getTranslation('availableHeader') }}</h5>
  <div class="selection-controls">
    <select v-model="optionSelect" class="form-select">
      <option :value="undefined">---</option>
      <option v-for="entity in availableEntities" :key="entity.id" :value="entity">
        {{ entity.label }}
      </option>
    </select>
    <button
      @click="selectEntity(optionSelect)"
      :disabled="optionSelect === undefined"
      type="button"
      class="btn btn-primary"
    >
      {{ getTranslation('addSelectionButton') }}
    </button>
    <button
      v-if="selectionsUpdated"
      type="button"
      class="btn btn-primary"
      data-bs-toggle="modal"
      data-bs-target="#exampleModal"
    >
      {{ getTranslation('submitSelectionButton') }}
    </button>
  </div>

  <!-- Modal -->
  <div
    class="modal fade"
    id="exampleModal"
    tabindex="-1"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="exampleModalLabel">
            {{ getTranslation('confirmModalHeader') }}
          </h5>
          <button
            type="button"
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <h5>{{ getTranslation('confirmAddedItemsHeader') }}</h5>
          <ul>
            <li v-for="entity in addedSelections" :key="entity.id">{{ entity.label }}</li>
          </ul>
          <h5>{{ getTranslation('confirmRemovedItemsHeader') }}</h5>
          <ul>
            <li v-for="entity in removedSelections" :key="entity.id">
              {{ entity.label }}
            </li>
          </ul>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
            {{ getTranslation('cancelChangesButton') }}
          </button>
          <button @click="saveChanges" type="button" class="btn btn-primary">
            {{ getTranslation('confirmChangesButton') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import type { PropType } from 'vue';
import type Entity from '../types/Entity';
import type EntitiesProp from '../types/EntitiesProp';

const FALLBACK_TRANSLATIONS: Record<string, string> = {
  selectedHeader: 'Selected',
  availableHeader: 'Available',
  addSelectionButton: 'Add selection',
  submitSelectionButton: 'Save Changes',
  confirmModalHeader: 'Confirm Selection Changes',
  confirmAddedItemsHeader: 'Add these selections',
  confirmRemovedItemsHeader: 'Remove these selections',
  cancelChangesButton: 'Cancel',
  confirmChangesButton: 'Confirm and Submit'
} as const;

const props = defineProps({
  entities: {
    type: Object as () => EntitiesProp,
    required: true
  },
  translations: {
    type: Object as () => Record<string, string>
  },
  saveSelectionCallback: {
    type: Function as PropType<(added: Entity[], removed: Entity[]) => void>,
    required: true
  }
});

const optionSelect = ref(undefined);
const addedSelections = ref(new Set<Entity>());
const removedSelections = ref(new Set<Entity>());

const selectionsUpdated = computed(() => {
  return addedSelections.value.size !== 0 || removedSelections.value.size !== 0;
});

const selectedEntities = computed(() => {
  return [...props.entities.selected, ...addedSelections.value].filter(
    (entity: Entity) => !removedSelections.value.has(entity)
  );
});

const availableEntities = computed(() => {
  return [...props.entities.available, ...removedSelections.value].filter(
    (entity: Entity) => !addedSelections.value.has(entity)
  );
});

/**
 * Selects the available entity. If the entity was previously removed, it will be removed from
 * {@link removedSelections}. Otherwise, it will be added to {@link addedSelections}.
 *
 * Afterward, {@link optionSelect} will be reset to undefined.
 *
 * @param entity
 */
const selectEntity = (entity: Entity | undefined) => {
  if (entity === undefined) {
    console.warn('Selected entity is undefined - failed to add selection');
    return;
  }
  if (removedSelections.value.has(entity)) {
    removedSelections.value.delete(entity);
  } else {
    addedSelections.value.add(entity);
  }
  optionSelect.value = undefined;
};

/**
 * Deselects the selected entity. If the entity was previously added, it will be removed from
 * {@link addedSelections}. Otherwise, it will be added to {@link removedSelections}.
 *
 * Afterward, {@link optionSelect} will be reset to undefined.
 *
 * @param entity
 */
const deselectEntity = (entity: Entity) => {
  if (addedSelections.value.has(entity)) {
    addedSelections.value.delete(entity);
  } else {
    removedSelections.value.add(entity);
  }
  optionSelect.value = undefined;
};

/**
 * Invokes the callback method to handle the saved selection changes
 */
const saveChanges = () => {
  props.saveSelectionCallback([...addedSelections.value], [...removedSelections.value]);
};

/**
 * Processes a translation key and returns the message string.
 *
 * Attempts to resolve the translation using the {@link props.translations} dictionary, before deferring to
 * {@link FALLBACK_TRANSLATIONS}. If the key cannot be found, it is used as the return value.
 *
 * @param key
 */
const getTranslation = (key: string) => {
  if (!key) {
    console.warn('No key provided');
    return undefined;
  }
  if (props.translations && key in props.translations) {
    return props.translations[key];
  }
  if (key in FALLBACK_TRANSLATIONS) {
    return FALLBACK_TRANSLATIONS[key];
  }
  console.warn(`Key ${key} not in fallback translations`);
  return key;
};
</script>
