<script lang="ts" setup>
import { ref, onMounted } from 'vue';

const props = defineProps<{
  endpoint: string;
  csrfToken: string;
}>();

interface SandboxUser {
  id: number;
  label: string;
}

const shared = ref<SandboxUser[]>([]);
const notShared = ref<SandboxUser[]>([]);
const selectedUser = ref<SandboxUser | undefined>(undefined);
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
const postUsers = async (users: SandboxUser[]) => {
  await syncUsers(props.endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-TOKEN': props.csrfToken
    },
    body: JSON.stringify({ users })
  });
};

// Share the Sandbox with the selected user
const grantAccess = async () => {
  if (!selectedUser.value) return;
  // Post the change and check that it persisted
  const user = selectedUser.value;
  await postUsers([selectedUser.value, ...shared.value]);
  if (notShared.value.includes(user) && !errorMessage.value) {
    errorMessage.value = `Failed to share sandox with ${user.label}`;
  }
};

// Revoke Sandbox access from the provided user
const revokeAccess = async (user: SandboxUser) => {
  await postUsers(shared.value.filter(u => u.id !== user.id));
  // Check that the change was persisted
  if (shared.value.includes(user) && !errorMessage.value) {
    errorMessage.value = `Failed to revoke access from ${user.label}`;
  }
};

onMounted(() => syncUsers(props.endpoint));
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
