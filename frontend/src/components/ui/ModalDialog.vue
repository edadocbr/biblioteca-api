<script setup>
import { onMounted, ref, useId, watch } from 'vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:open'])

const dialogRef = ref(null)
const titleId = useId()
const descriptionId = useId()

function showDialog() {
  if (dialogRef.value && !dialogRef.value.open) {
    dialogRef.value.showModal()
  }
}

function closeDialog() {
  if (dialogRef.value?.open) {
    dialogRef.value.close()
  }
}

function handleNativeClose() {
  emit('update:open', false)
}

onMounted(() => {
  if (props.open) showDialog()
})

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      showDialog()
      return
    }

    closeDialog()
  },
)
</script>

<template>
  <dialog
    ref="dialogRef"
    :aria-labelledby="titleId"
    :aria-describedby="description ? descriptionId : undefined"
    class="m-auto w-[calc(100vw-2rem)] max-w-lg rounded-none border border-neutral-200 bg-white p-0 text-neutral-900 shadow-none backdrop:bg-neutral-900/40"
    @close="handleNativeClose"
    @click.self="closeDialog"
  >
    <div class="flex max-h-[85vh] flex-col">
      <header class="flex items-start justify-between gap-6 border-b border-neutral-200 px-5 py-4">
        <div class="min-w-0">
          <h2 :id="titleId" class="text-base font-semibold tracking-tight text-neutral-900">
            {{ title }}
          </h2>
          <p
            v-if="description"
            :id="descriptionId"
            class="mt-1 font-mono text-[10px] uppercase tracking-wider text-neutral-400"
          >
            {{ description }}
          </p>
        </div>
        <button
          type="button"
          class="shrink-0 font-mono text-[10px] uppercase tracking-wider text-neutral-400 hover:text-neutral-900 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-2 focus-visible:outline-neutral-900"
          @click="closeDialog"
        >
          Cerrar
        </button>
      </header>

      <div class="overflow-y-auto p-5">
        <slot />
      </div>

      <footer
        v-if="$slots.footer"
        class="border-t border-neutral-200 px-5 py-3"
      >
        <slot name="footer" />
      </footer>
    </div>
  </dialog>
</template>

<style scoped>
dialog {
  opacity: 0;
  transform: translateY(6px) scale(0.97);
  transition:
    opacity 180ms cubic-bezier(0.23, 1, 0.32, 1),
    transform 180ms cubic-bezier(0.23, 1, 0.32, 1);
}

dialog[open] {
  opacity: 1;
  transform: translateY(0) scale(1);
}

dialog::backdrop {
  background-color: rgb(23 23 23 / 0.4);
  opacity: 0;
  transition: opacity 180ms cubic-bezier(0.23, 1, 0.32, 1);
}

dialog[open]::backdrop {
  opacity: 1;
}

@starting-style {
  dialog[open] {
    opacity: 0;
    transform: translateY(6px) scale(0.97);
  }

  dialog[open]::backdrop {
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  dialog,
  dialog[open] {
    transform: none;
    transition: opacity 120ms cubic-bezier(0.23, 1, 0.32, 1);
  }

  dialog::backdrop {
    transition: opacity 120ms cubic-bezier(0.23, 1, 0.32, 1);
  }
}
</style>
