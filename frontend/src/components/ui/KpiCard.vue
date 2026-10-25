<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number],
    required: true,
  },
  hint: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <div class="flex flex-col justify-between border border-neutral-200 bg-white p-4 rounded-none">
    <p class="font-mono text-xs uppercase text-neutral-500">
      {{ label }}
    </p>
    <p class="mt-4 text-2xl font-semibold tracking-tight text-neutral-900 tabular-nums">
      {{ value }}
    </p>
    <p v-if="hint" class="mt-1 font-mono text-[10px] text-neutral-400">
      {{ hint }}
    </p>
  </div>
</template>
