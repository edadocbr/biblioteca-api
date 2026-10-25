<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'warning',
    validator: (value) => ['warning', 'error', 'success', 'info'].includes(value),
  },
  title: {
    type: String,
    default: '',
  },
  actionLabel: {
    type: String,
    default: '',
  },
  dismissible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['action', 'dismiss'])

const variantClass = computed(() => {
  const variants = {
    warning: 'border-l-amber-500 bg-amber-50 text-amber-950',
    error: 'border-l-red-500 bg-red-50 text-red-800',
    success: 'border-l-emerald-600 bg-emerald-50 text-emerald-900',
    info: 'border-l-neutral-900 bg-neutral-50 text-neutral-700',
  }

  return variants[props.variant]
})

const role = computed(() => {
  return props.variant === 'error' || props.variant === 'warning' ? 'alert' : 'status'
})
</script>

<template>
  <div
    class="border border-neutral-200 border-l-2 p-3 font-mono text-xs"
    :class="variantClass"
    :role="role"
    aria-live="polite"
  >
    <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
      <div class="min-w-0 leading-relaxed">
        <p v-if="title" class="mb-1 font-semibold uppercase tracking-wider">
          {{ title }}
        </p>
        <slot />
      </div>

      <div
        v-if="$slots.actions || actionLabel || dismissible"
        class="flex shrink-0 items-center gap-3"
      >
        <button
          v-if="actionLabel"
          type="button"
          class="font-mono text-[10px] uppercase tracking-wider underline underline-offset-4 hover:text-neutral-900 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-2 focus-visible:outline-neutral-900"
          @click="emit('action')"
        >
          {{ actionLabel }}
        </button>
        <button
          v-if="dismissible"
          type="button"
          class="font-mono text-[10px] uppercase tracking-wider opacity-60 transition-opacity duration-150 hover:opacity-100 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-2 focus-visible:outline-neutral-900"
          aria-label="Descartar aviso"
          @click="emit('dismiss')"
        >
          Descartar
        </button>
      </div>
    </div>
  </div>
</template>
