<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import type { Ref } from 'vue';
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

const shared: Ref<Entity[]> = ref([]);
const notShared: Ref<Entity[]> = ref([]);
const optionSelect = ref(undefined);

const isLoading = ref(true);
const errorMessage = ref('');

/**
 * Posts the list of users who have access to the sandbox, plus the selected user
 *
 * @param user
 */
const grantAccess = (user: Entity | undefined) => {
  if (user === undefined) {
    console.warn('User selection is undefined');
    return;
  }
  postUsers([user, ...shared.value]);
};

/**
 * Posts the list of users who have access to the sandbox, minus the selected user
 *
 * @param user
 */
const revokeAccess = (user: Entity) => {
  postUsers(shared.value.filter(u => user.id !== u.id));
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
    optionSelect.value = undefined;
  } catch (error) {
    console.error('Error requesting shared users:', error);
    errorMessage.value = errorMsg;
  }
};

/**
 * Uses the processRequest helper to update which users the sandbox is shared with
 */
const postUsers = async (users: Entity[]) => {
  processRequest(
    props.postEndpoint,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': props.csrfToken
      },
      body: JSON.stringify({ users: users })
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
    <h5 v-if="shared.length > 0">Authorized Users</h5>
    <div class="current-selections">
      <span v-for="user in shared" :key="user.id" :value="user" class="badge me-1">
        <i @click="revokeAccess(user)" class="fa-solid fa-x"></i>
        {{ user.label }}
      </span>
    </div>
    <h5>Available Users</h5>
    <div class="selection-controls">
      <select v-model="optionSelect" class="form-select">
        <option :value="undefined">---</option>
        <option v-for="user in notShared" :key="user.id" :value="user">
          {{ user.label }}
        </option>
      </select>
      <button
        @click="grantAccess(optionSelect)"
        :disabled="!isLoading && optionSelect === undefined"
        type="button"
        class="btn btn-primary"
      >
        Grant Access
      </button>
    </div>
  </div>
</template>
