<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  message: {
    type: String,
    default: '',
  },
  variant: {
    type: String,
    default: 'success',
    validator: (value) => ['success', 'error', 'warning', 'info'].includes(value),
  },
  duration: {
    type: Number,
    default: 4200,
  },
})

const emit = defineEmits(['close'])

let timer = null
let startedAt = 0
let remaining = props.duration

const variantClass = computed(() => {
  const variants = {
    success: 'border-l-emerald-600',
    error: 'border-l-red-500',
    warning: 'border-l-amber-500',
    info: 'border-l-neutral-900',
  }

  return variants[props.variant]
})

const eyebrow = computed(() => {
  const labels = {
    success: 'Operación completada',
    error: 'Error de operación',
    warning: 'Aviso del sistema',
    info: 'Información',
  }

  return labels[props.variant]
})

function clearTimer() {
  if (!timer) return

  clearTimeout(timer)
  timer = null
}

function startTimer() {
  clearTimer()
  startedAt = Date.now()
  timer = setTimeout(() => {
    timer = null
    emit('close')
  }, remaining)
}

function pauseTimer() {
  if (!timer) return

  clearTimer()
  remaining = Math.max(0, remaining - (Date.now() - startedAt))
}

function resumeTimer() {
  if (!props.open || timer) return

  if (remaining <= 0) {
    emit('close')
    return
  }

  startTimer()
}

watch(
  () => props.open,
  (isOpen) => {
    if (!isOpen) {
      clearTimer()
      return
    }

    remaining = props.duration
    startTimer()
  },
  { immediate: true },
)

watch(
  () => props.message,
  () => {
    if (!props.open) return

    remaining = props.duration
    startTimer()
  },
)

onBeforeUnmount(clearTimer)
</script>

<template>
  <Teleport to="body">
    <div class="pointer-events-none fixed bottom-4 left-4 right-4 z-[60] sm:left-auto sm:w-80">
      <Transition name="toast">
        <div
          v-if="open"
          class="pointer-events-auto flex items-start gap-3 border border-neutral-200 border-l-2 bg-white p-3"
          :class="variantClass"
          role="status"
          aria-live="polite"
          @mouseenter="pauseTimer"
          @mouseleave="resumeTimer"
          @focusin="pauseTimer"
          @focusout="resumeTimer"
        >
          <div class="min-w-0 flex-1">
            <p class="font-mono text-[10px] uppercase tracking-widest text-neutral-400">
              {{ eyebrow }}
            </p>
            <p class="mt-1 text-xs leading-relaxed text-neutral-900">
              {{ message }}
            </p>
          </div>
          <button
            type="button"
            class="shrink-0 font-mono text-[10px] uppercase tracking-wider text-neutral-400 hover:text-neutral-900 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-2 focus-visible:outline-neutral-900"
            aria-label="Cerrar notificación"
            @click="emit('close')"
          >
            Cerrar
          </button>
        </div>
      </Transition>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition:
    opacity 160ms cubic-bezier(0.23, 1, 0.32, 1),
    transform 160ms cubic-bezier(0.23, 1, 0.32, 1);
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

@media (prefers-reduced-motion: reduce) {
  .toast-enter-active,
  .toast-leave-active {
    transition: opacity 120ms cubic-bezier(0.23, 1, 0.32, 1);
  }

  .toast-enter-from,
  .toast-leave-to {
    transform: none;
  }
}
</style>
