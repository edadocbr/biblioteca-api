<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  columns: {
    type: Array,
    required: true,
  },
  rows: {
    type: Array,
    default: () => [],
  },
  rowKey: {
    type: String,
    default: 'id',
  },
  selected: {
    type: Array,
    default: () => [],
  },
  selectable: {
    type: Boolean,
    default: false,
  },
  emptyMessage: {
    type: String,
    default: 'Sin registros para mostrar.',
  },
})

const emit = defineEmits(['update:selected'])

const sortKey = ref('')
const sortDirection = ref('asc')

const sortedRows = computed(() => {
  const column = props.columns.find((item) => item.key === sortKey.value)

  if (!column) {
    return props.rows
  }

  const direction = sortDirection.value === 'asc' ? 1 : -1

  return [...props.rows].sort((a, b) => {
    const left = a[column.key]
    const right = b[column.key]

    if (left === right) return 0
    if (left === null || left === undefined) return 1
    if (right === null || right === undefined) return -1

    if (column.sortType === 'number') {
      return (Number(left) - Number(right)) * direction
    }

    return String(left).localeCompare(String(right), 'es', {
      numeric: true,
      sensitivity: 'base',
    }) * direction
  })
})

const selectedSet = computed(() => new Set(props.selected))
const allSelected = computed(() => {
  return props.rows.length > 0
    && props.rows.every((row) => selectedSet.value.has(row[props.rowKey]))
})
const someSelected = computed(() => {
  return props.rows.some((row) => selectedSet.value.has(row[props.rowKey]))
    && !allSelected.value
})

function toggleSort(column) {
  if (!column.sortable) return

  if (sortKey.value === column.key) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
    return
  }

  sortKey.value = column.key
  sortDirection.value = 'asc'
}

function indicator(column) {
  if (sortKey.value !== column.key) return '↕'
  return sortDirection.value === 'asc' ? '↑' : '↓'
}

function ariaSort(column) {
  if (sortKey.value !== column.key) return 'none'
  return sortDirection.value === 'asc' ? 'ascending' : 'descending'
}

function toggleAll(event) {
  if (event.target.checked) {
    emit('update:selected', props.rows.map((row) => row[props.rowKey]))
    return
  }

  emit('update:selected', [])
}

function toggleRow(row) {
  const id = row[props.rowKey]
  const next = selectedSet.value.has(id)
    ? props.selected.filter((item) => item !== id)
    : [...props.selected, id]

  emit('update:selected', next)
}

function isSelected(row) {
  return selectedSet.value.has(row[props.rowKey])
}

function headerClass(column) {
  return [
    'px-3 py-2.5 font-mono text-[10px] font-medium uppercase tracking-wider text-neutral-500',
    column.align === 'right' ? 'text-right' : 'text-left',
  ]
}

function cellClass(column) {
  return [
    'px-3 py-2.5 align-middle whitespace-nowrap',
    column.align === 'right' ? 'text-right tabular-nums' : 'text-left',
    column.mono ? 'font-mono text-xs text-neutral-700' : 'text-sm text-neutral-700',
  ]
}
</script>

<template>
  <div class="w-full overflow-x-auto">
    <table class="w-full min-w-full border-collapse text-left">
      <thead class="bg-neutral-50">
        <tr class="border-b border-neutral-200">
          <th v-if="selectable" scope="col" class="w-10 px-3 py-2.5">
            <input
              type="checkbox"
              class="h-3.5 w-3.5 cursor-pointer accent-neutral-900"
              :checked="allSelected"
              :indeterminate.prop="someSelected"
              aria-label="Seleccionar todas las filas"
              @change="toggleAll"
            />
          </th>
          <th
            v-for="column in columns"
            :key="column.key"
            scope="col"
            :class="headerClass(column)"
            :aria-sort="column.sortable ? ariaSort(column) : undefined"
          >
            <button
              v-if="column.sortable"
              type="button"
              class="flex w-full items-center gap-1.5 rounded-none text-inherit hover:text-neutral-900 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-2 focus-visible:outline-neutral-900"
              :class="column.align === 'right' ? 'justify-end' : 'justify-start'"
              @click="toggleSort(column)"
            >
              <span>{{ column.label }}</span>
              <span class="text-[10px] text-neutral-400" aria-hidden="true">
                {{ indicator(column) }}
              </span>
            </button>
            <span v-else>{{ column.label }}</span>
          </th>
        </tr>
      </thead>
      <tbody>
        <template v-if="rows.length === 0">
          <tr>
            <td
              :colspan="columns.length + (selectable ? 1 : 0)"
              class="px-3 py-10 text-center font-mono text-xs text-neutral-400"
            >
              {{ emptyMessage }}
            </td>
          </tr>
        </template>
        <template v-else>
          <tr
            v-for="row in sortedRows"
            :key="row[rowKey]"
            class="border-b border-neutral-200 last:border-b-0 hover:bg-neutral-50/80"
          >
            <td v-if="selectable" class="px-3 py-2.5">
              <input
                type="checkbox"
                class="h-3.5 w-3.5 cursor-pointer accent-neutral-900"
                :checked="isSelected(row)"
                :aria-label="`Seleccionar fila ${row[rowKey]}`"
                @change="toggleRow(row)"
              />
            </td>
            <td v-for="column in columns" :key="column.key" :class="cellClass(column)">
              <slot :name="`cell-${column.key}`" :row="row" :value="row[column.key]">
                {{ row[column.key] ?? '—' }}
              </slot>
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>
