<script setup>
import { computed } from 'vue'

const props = defineProps({
  steps: {
    type: Array,
    default: () => [],
  },
  activeStep: {
    type: Number,
    default: 0,
  },
})

const normalizedSteps = computed(() => {
  return props.steps.map((step) => {
    return typeof step === 'string' ? { label: step } : step
  })
})

function indicatorClass(index) {
  if (index < props.activeStep) {
    return 'border-neutral-900 bg-neutral-900 text-white'
  }

  if (index === props.activeStep) {
    return 'border-neutral-900 bg-white text-neutral-900'
  }

  return 'border-neutral-200 bg-white text-neutral-400'
}

function connectorClass(index) {
  return index < props.activeStep ? 'bg-neutral-900' : 'bg-neutral-200'
}

function labelClass(index) {
  if (index === props.activeStep) return 'text-neutral-900'
  if (index < props.activeStep) return 'text-neutral-600'
  return 'text-neutral-400'
}
</script>

<template>
  <ol class="flex w-full items-start">
    <li
      v-for="(step, index) in normalizedSteps"
      :key="step.label"
      class="flex min-w-0 flex-1 flex-col gap-2"
      :aria-current="index === activeStep ? 'step' : undefined"
    >
      <div class="flex w-full items-center">
        <span
          class="flex h-6 w-6 shrink-0 items-center justify-center rounded-none border font-mono text-[11px]"
          :class="indicatorClass(index)"
        >
          <span v-if="index < activeStep" aria-hidden="true">✓</span>
          <span v-else aria-hidden="true">{{ index + 1 }}</span>
          <span class="sr-only">
            {{ index < activeStep ? `Etapa ${index + 1} completada` : `Etapa ${index + 1}` }}
          </span>
        </span>
        <span
          v-if="index < normalizedSteps.length - 1"
          class="ml-2 h-px flex-1"
          :class="connectorClass(index)"
          aria-hidden="true"
        />
      </div>

      <div class="min-w-0 pr-2">
        <p class="font-mono text-[10px] uppercase tracking-wider" :class="labelClass(index)">
          {{ step.label }}
        </p>
        <p v-if="step.description" class="mt-1 text-[11px] leading-snug text-neutral-400">
          {{ step.description }}
        </p>
      </div>
    </li>
  </ol>
</template>
