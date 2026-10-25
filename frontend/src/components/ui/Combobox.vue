<script setup>
import { computed, onBeforeUnmount, onMounted, ref, useId, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: null,
  },
  options: {
    type: Array,
    default: () => [],
  },
  id: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: 'Buscar…',
  },
  emptyMessage: {
    type: String,
    default: 'Sin resultados.',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const generatedId = useId()
const inputId = computed(() => props.id || `combobox-${generatedId}`)
const listboxId = computed(() => `${inputId.value}-listbox`)

const rootRef = ref(null)
const inputRef = ref(null)
const isOpen = ref(false)
const activeIndex = ref(-1)
const query = ref('')

const selectedOption = computed(() => {
  return props.options.find((option) => option.value === props.modelValue) ?? null
})

const filteredOptions = computed(() => {
  const term = query.value.trim().toLocaleLowerCase('es')
  const selectedLabel = selectedOption.value?.label

  if (!term || query.value === selectedLabel) {
    return props.options
  }

  return props.options.filter((option) => {
    const searchable = `${option.label ?? ''} ${option.meta ?? ''}`.toLocaleLowerCase('es')
    return searchable.includes(term)
  })
})

const activeDescendant = computed(() => {
  if (!isOpen.value || activeIndex.value < 0) return undefined
  return `${listboxId.value}-option-${activeIndex.value}`
})

watch(
  () => props.modelValue,
  () => {
    query.value = selectedOption.value?.label ?? ''
  },
  { immediate: true },
)

watch(
  () => props.options,
  () => {
    if (activeIndex.value >= filteredOptions.value.length) {
      activeIndex.value = Math.max(0, filteredOptions.value.length - 1)
    }
  },
)

function openPanel() {
  if (props.disabled) return

  isOpen.value = true
  const currentIndex = filteredOptions.value.findIndex(
    (option) => option.value === props.modelValue,
  )
  activeIndex.value = currentIndex >= 0 ? currentIndex : 0
}

function closePanel() {
  isOpen.value = false
  activeIndex.value = -1
  query.value = selectedOption.value?.label ?? ''
}

function handleFocus(event) {
  event.target.select()
  openPanel()
}

function handleInput() {
  isOpen.value = true
  activeIndex.value = 0
}

function selectOption(option) {
  emit('update:modelValue', option.value)
  emit('change', option)
  query.value = option.label
  isOpen.value = false
  activeIndex.value = -1
}

function move(direction) {
  if (!isOpen.value) {
    openPanel()
    return
  }

  const total = filteredOptions.value.length
  if (total === 0) return

  activeIndex.value = (activeIndex.value + direction + total) % total
}

function handleKeydown(event) {
  if (props.disabled) return

  if (event.key === 'ArrowDown') {
    event.preventDefault()
    move(1)
    return
  }

  if (event.key === 'ArrowUp') {
    event.preventDefault()
    move(-1)
    return
  }

  if (event.key === 'Enter') {
    if (!isOpen.value) return

    event.preventDefault()
    const option = filteredOptions.value[activeIndex.value]
    if (option) selectOption(option)
    return
  }

  if (event.key === 'Escape') {
    if (!isOpen.value) return

    event.preventDefault()
    event.stopPropagation()
    closePanel()
    return
  }

  if (event.key === 'Tab') {
    closePanel()
  }
}

function handleOutsidePointer(event) {
  if (!rootRef.value?.contains(event.target)) {
    closePanel()
  }
}

onMounted(() => {
  document.addEventListener('pointerdown', handleOutsidePointer)
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', handleOutsidePointer)
})
</script>

<template>
  <div ref="rootRef" class="relative">
    <input
      :id="inputId"
      ref="inputRef"
      v-model="query"
      type="text"
      role="combobox"
      autocomplete="off"
      spellcheck="false"
      :placeholder="placeholder"
      :disabled="disabled"
      aria-autocomplete="list"
      :aria-expanded="isOpen"
      :aria-controls="listboxId"
      :aria-activedescendant="activeDescendant"
      class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 text-sm text-neutral-900 outline-none focus:border-neutral-900 disabled:cursor-not-allowed disabled:bg-neutral-50 disabled:text-neutral-400"
      @focus="handleFocus"
      @input="handleInput"
      @keydown="handleKeydown"
    />

    <Transition name="combo">
      <div
        v-if="isOpen && !disabled"
        class="absolute left-0 right-0 z-30 mt-1 max-h-56 overflow-y-auto border border-neutral-200 bg-white"
      >
        <ul :id="listboxId" role="listbox" class="m-0 list-none p-0">
          <li v-if="filteredOptions.length === 0">
            <p class="px-3 py-3 font-mono text-xs text-neutral-400">
              {{ emptyMessage }}
            </p>
          </li>
          <li
            v-for="(option, index) in filteredOptions"
            v-else
            :key="option.value"
            :id="`${listboxId}-option-${index}`"
            role="option"
            :aria-selected="option.value === modelValue"
            class="cursor-pointer border-b border-neutral-100 px-3 py-2 last:border-b-0"
            :class="[
              index === activeIndex ? 'bg-neutral-100' : 'bg-white',
              option.value === modelValue ? 'font-medium text-neutral-900' : 'text-neutral-700',
            ]"
            @mousedown.prevent
            @click="selectOption(option)"
            @mousemove="activeIndex = index"
          >
            <span class="block text-xs">{{ option.label }}</span>
            <span v-if="option.meta" class="mt-0.5 block font-mono text-[10px] text-neutral-400">
              {{ option.meta }}
            </span>
          </li>
        </ul>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.combo-enter-active,
.combo-leave-active {
  transform-origin: top;
  transition:
    opacity 140ms cubic-bezier(0.23, 1, 0.32, 1),
    transform 140ms cubic-bezier(0.23, 1, 0.32, 1);
}

.combo-enter-from,
.combo-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.97);
}

@media (prefers-reduced-motion: reduce) {
  .combo-enter-active,
  .combo-leave-active {
    transition: opacity 120ms cubic-bezier(0.23, 1, 0.32, 1);
  }

  .combo-enter-from,
  .combo-leave-to {
    transform: none;
  }
}
</style>
