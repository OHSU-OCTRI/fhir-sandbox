<template>
  <h1>Hello, world</h1>
  <div>
    <span
      v-for="entity in selectedEntities"
      :key="entity.id"
      :value="entity.id"
      class="me-1 badge mesh-badge"
    >
      <i @click="removeSelection(entity.id)" class="fa-solid fa-x"></i>
      {{ entity.label }}
    </span>
  </div>
  <div>
    <select id="selection" v-model="selectedOption">
      <option value="" disabled>---</option>
      <option v-for="entity in selectableEntities" :key="entity.id" :value="entity.id">
        {{ entity.label }}
      </option>
    </select>
    <button @click="addSelection(selectedOption)" type="button" class="btn btn-primary">
      Add selection
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';

const props = defineProps({
  entities: {
    type: Array,
    required: true
  },
  selectionChangeCallback: {
    type: Function,
    required: true
  }
});

const selectedOption = ref('');
const selectedEntities = ref(new Set());
const selectableEntities = computed(() => {
  return props.entities
    .filter(entity => !selectedEntities.value.has(entity.id))
    .map(entity => entity.id);
});

const addSelection = entity => {
  if (entity) {
    selectedEntities.value.add(entity.id);
  }
};

const removeSelection = entity => {
  if (entity) {
    selectableEntities.value.remove(entity.id);
  }
};

watch(selectedEntities, newSelectedEntities => {
  props.selectionChangeCallback(newSelectedEntities);
});

onMounted(() => {
  props.entities.forEach(entity => addSelection(entity));
});
</script>
