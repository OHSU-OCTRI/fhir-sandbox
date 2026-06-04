<script lang="ts" setup>
import { ref, computed, type Ref, onMounted } from 'vue';
import type Entity from '../types/Entity';

const props = defineProps<{
  getEndpoint: string;
  postEndpoint: string;
  csrfToken: string;
}>();

interface SharedUsersResponse {
  shared: Entity[];
  notShared: Entity[];
}

const errorMessage = ref('');
const shared: Ref<Entity[]> = ref([]);
const notShared: Ref<Entity[]> = ref([]);
const optionSelect = ref(undefined);
const addedSelections = ref(new Set<Entity>());
const removedSelections = ref(new Set<Entity>());

const selectedUsers = computed(() => {
  return [...shared.value, ...addedSelections.value].filter(
    (user: Entity) => !removedSelections.value.has(user)
  );
});

const availableUsers = computed(() => {
  return [...notShared.value, ...removedSelections.value].filter(
    (user: Entity) => !addedSelections.value.has(user)
  );
});

const selectionsUpdated = computed(() => {
  return addedSelections.value.size !== 0 || removedSelections.value.size !== 0;
});

/**
 * Selects the notShared user. If the entity was previously removed, it will be removed from
 * removedSelections. Otherwise, it will be added to addedSelections.
 *
 * Afterward, optionSelect will be reset to undefined.
 *
 * @param user
 */
const selectUser = (user: Entity | undefined) => {
  if (user === undefined) {
    console.warn('User selection is undefined');
    return;
  }
  if (removedSelections.value.has(user)) {
    removedSelections.value.delete(user);
  } else {
    addedSelections.value.add(user);
  }
  optionSelect.value = undefined;
};

/**
 * Deselects the shared user. If the user was previously added, it will be removed from
 * addedSelections. Otherwise, it will be added to removedSelections.
 *
 * Afterward, optionSelect will be reset to undefined.
 *
 * @param user
 */
const deselectUser = (user: Entity) => {
  if (addedSelections.value.has(user)) {
    addedSelections.value.delete(user);
  } else {
    removedSelections.value.add(user);
  }
  optionSelect.value = undefined;
};

/**
 * Processes GET and POST requests, parsing the responses to keep the component's state up-to-date
 *
 * @param uri
 * @param options
 * @param msg
 */
const processRequest = async (uri: string, options: object, errorMsg: string) => {
  try {
    const response = await fetch(uri, options);
    if (!response.ok) {
      throw new Error(`HTTP error (status ${response.status})`);
    }
    const sharedUsers = (await response.json()) as SharedUsersResponse;

    shared.value = sharedUsers.shared;
    notShared.value = sharedUsers.notShared;
    addedSelections.value.clear();
    removedSelections.value.clear();
    optionSelect.value = undefined;
  } catch (error) {
    console.error('Error requesting shared users:', error);
    errorMessage.value = errorMsg;
  }
};

/**
 * Uses the processRequest helper to update which users the sandbox is shared with
 */
const postChanges = async () => {
  processRequest(
    props.postEndpoint,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': props.csrfToken
      },
      body: JSON.stringify({ users: selectedUsers.value })
    },
    'Failed to post changes to shared users. Please try again later.'
  );
};

onMounted(async () => {
  processRequest(props.getEndpoint, {}, 'Failed to load data. Please try again later.');
});
</script>
<template>
  <div v-if="errorMessage !== ''" class="alert alert-danger my-4" role="alert">
    {{ errorMessage }}
  </div>
  <div>
    <!-- Visible Selection Widget -->
    <h5 v-if="selectedUsers.length > 0">Authorized Users</h5>
    <div class="current-selections">
      <span v-for="user in selectedUsers" :key="user.id" :value="user" class="badge me-1">
        <i @click="deselectUser(user)" class="fa-solid fa-x"></i>
        {{ user.label }}
      </span>
    </div>
    <h5>Available Users</h5>
    <div class="selection-controls">
      <select v-model="optionSelect" class="form-select">
        <option :value="undefined">---</option>
        <option v-for="user in availableUsers" :key="user.id" :value="user">
          {{ user.label }}
        </option>
      </select>
      <button
        @click="selectUser(optionSelect)"
        :disabled="optionSelect === undefined"
        type="button"
        class="btn btn-primary"
      >
        Grant Access
      </button>
      <button
        v-if="selectionsUpdated"
        type="button"
        class="btn btn-primary ms-2"
        data-bs-toggle="modal"
        data-bs-target="#confirmModal"
      >
        Save Changes
      </button>
    </div>

    <!-- Submission Confirmation Modal -->
    <div
      class="modal fade"
      id="confirmModal"
      tabindex="-1"
      aria-labelledby="confirmModalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="confirmModalLabel">Confirm Access Changes</h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"
            ></button>
          </div>
          <div class="modal-body">
            <h5 v-if="addedSelections.size > 0">Granting access to</h5>
            <ul>
              <li v-for="user in addedSelections" :key="user.id">
                {{ user.label }}
              </li>
            </ul>
            <h5 v-if="removedSelections.size > 0">Revoking access from</h5>
            <ul>
              <li v-for="user in removedSelections" :key="user.id">
                {{ user.label }}
              </li>
            </ul>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
              Cancel
            </button>
            <button
              @click="postChanges"
              type="button"
              class="btn btn-primary"
              data-bs-dismiss="modal"
            >
              Confirm
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
