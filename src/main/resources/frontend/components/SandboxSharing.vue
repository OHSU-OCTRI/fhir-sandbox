<script lang="ts" setup>
import { ref, onMounted } from 'vue';

const props = defineProps<{
  getEndpoint: string;
  postEndpoint: string;
  csrfToken: string;
}>();

interface SharedUser {
  id: number;
  label: string;
}

const shared = ref<SharedUser[]>([]);
const notShared = ref<SharedUser[]>([]);
const selectedUser = ref<SharedUser | undefined>(undefined);
const isLoading = ref(false);
const errorMessage = ref('');

// Shared fetch wrapper: sets loading/error state and syncs the response
const syncUsers = async (url: string, options: object = {}) => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const response = await fetch(url, options);
    if (!response.ok) {
      throw new Error(`HTTP error (status ${response.status})`);
    }
    const data = await response.json();

    // Sync local state from an API response
    shared.value = data.shared;
    notShared.value = data.notShared;
    selectedUser.value = undefined;
  } catch (error) {
    console.error('Shared users request failed:', error);
    errorMessage.value = 'Something went wrong. Please try again later.';
  } finally {
    isLoading.value = false;
  }
};

// POST a new shared-users list to the server
const postUsers = (users: SharedUser[]) => {
  syncUsers(props.postEndpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-TOKEN': props.csrfToken
    },
    body: JSON.stringify({ users })
  });
};

// Share the Sandbox with the selected user
const grantAccess = () => {
  if (!selectedUser.value) return;
  postUsers([selectedUser.value, ...shared.value]);
};

// Revoke Sandbox access from the provided user
const revokeAccess = (user: SharedUser) => {
  postUsers(shared.value.filter(u => u.id !== user.id));
};

onMounted(() => syncUsers(props.getEndpoint));
</script>

<template>
  <div v-if="errorMessage" class="alert alert-danger my-4" role="alert">
    {{ errorMessage }}
  </div>

  <div v-if="shared.length > 0">
    <h5>Authorized Users</h5>
    <div class="current-selections">
      <span v-for="user in shared" :key="user.id" class="badge me-1">
        <i class="fa-solid fa-x" @click="revokeAccess(user)" />
        {{ user.label }}
      </span>
    </div>
  </div>

  <h5>Available Users</h5>
  <div class="selection-controls">
    <select v-model="selectedUser" class="form-select">
      <option :value="undefined">---</option>
      <option v-for="user in notShared" :key="user.id" :value="user">
        {{ user.label }}
      </option>
    </select>
    <button
      type="button"
      class="btn btn-primary"
      :disabled="isLoading || selectedUser === undefined"
      @click="grantAccess"
    >
      Grant Access
    </button>
  </div>
</template>
