<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import DataTable from '../../components/ui/DataTable.vue'
import KpiCard from '../../components/ui/KpiCard.vue'
import ModalDialog from '../../components/ui/ModalDialog.vue'
import InlineAlert from '../../components/ui/InlineAlert.vue'
import Toast from '../../components/ui/Toast.vue'

const toastState = ref({ open: false, message: '', variant: 'success' })
function showToast(message, variant = 'success') { toastState.value = { open: true, message, variant } }
function hideToast() { toastState.value.open = false }

const inventory = ref([])
const inventoryColumns = [
  { key: 'isbn', label: 'ISBN', mono: true, sortable: true },
  { key: 'codigoBarra', label: 'Cód. barras', mono: true, sortable: true },
  { key: 'tagRfid', label: 'Tag RFID', mono: true, sortable: true },
  { key: 'titulo', label: 'Título', sortable: true },
  { key: 'autor', label: 'Autor', sortable: true },
  { key: 'anioPublicacion', label: 'Año', mono: true, sortable: true, align: 'right', sortType: 'number' },
  { key: 'estado', label: 'Estado', sortable: true },
]

const inventorySearch = ref('')
const inventoryState = ref('Todos')
const selectedInventory = ref([])
const inventoryModalOpen = ref(false)
const inventoryFormError = ref('')
const isLoading = ref(false)
const isSaving = ref(false)

const inventoryForm = ref({ titulo: '', autor: '', isbn: '', anioPublicacion: '', cantidad: 1, items: [{ codigoBarra: '', tagRfid: '' }] })

const kpiData = ref({ totalTitulos: 0, ejemplaresFisicos: 0, nuevasAdquisiciones: 0 })

function mapToTable(rows = []) {
  return rows.map((item, idx) => ({
    id: item.codigoBarra || `EJ-${idx + 1}`,
    isbn: item.isbn || '',
    codigoBarra: item.codigoBarra || '',
    tagRfid: item.tagRfid || '',
    titulo: item.titulo || '',
    autor: item.autor || '',
    anioPublicacion: item.anioPublicacion || '',
    estado: item.estado || 'Disponible'
  }))
}

async function fetchKpis() {
  try {
    const res = await fetch('http://localhost:8080/api/admin/ejemplares/kpis')
    if (res.ok) {
      const data = await res.json()
      kpiData.value = data
    }
  } catch (e) {
    // ignore
  }
}

let searchTimer = null
async function fetchInventory() {
  isLoading.value = true
  try {
    const params = new URLSearchParams()
    const q = inventorySearch.value.trim()
    if (q) params.append('q', q)
    if (inventoryState.value && inventoryState.value !== 'Todos') {
      params.append('estado', inventoryState.value)
    }
    const url = params.toString() ? `http://localhost:8080/api/admin/ejemplares?${params.toString()}` : 'http://localhost:8080/api/admin/ejemplares'
    const res = await fetch(url)
    if (res.ok) {
      const data = await res.json()
      inventory.value = mapToTable(Array.isArray(data) ? data : [])
    } else {
      inventory.value = []
    }
  } catch (e) {
    inventory.value = []
  } finally {
    isLoading.value = false
  }
}

watch(inventorySearch, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => fetchInventory(), 300)
})
watch(inventoryState, () => fetchInventory())

onMounted(async () => {
  await Promise.all([fetchKpis(), fetchInventory()])
})

const inventoryKpis = computed(() => [
  { label: 'Total títulos', value: kpiData.value.totalTitulos || 0, hint: 'Registros bibliográficos' },
  { label: 'Ejemplares físicos', value: kpiData.value.ejemplaresFisicos || 0, hint: 'Unidades inventariadas' },
  { label: 'Nuevas adquisiciones', value: kpiData.value.nuevasAdquisiciones || 0, hint: 'Ingresos del periodo' },
])
function inventoryStateClass(state) {
  if (state === 'Disponible') return 'border-neutral-300 bg-white text-neutral-700'
  if (state === 'En Préstamo') return 'border-neutral-900 bg-neutral-900 text-white'
  return 'border-neutral-200 bg-neutral-50 text-neutral-400'
}
function resetInventoryForm() {
  inventoryForm.value = { titulo: '', autor: '', isbn: '', anioPublicacion: '', cantidad: 1, items: [{ codigoBarra: '', tagRfid: '' }] }
  inventoryFormError.value = ''
}
function adjustItems() {
  const qty = Number(inventoryForm.value.cantidad) || 1
  const items = inventoryForm.value.items
  if (qty < 1) inventoryForm.value.cantidad = 1
  const target = Number(inventoryForm.value.cantidad)
  if (target <= items.length) {
    inventoryForm.value.items = items.slice(0, target)
  } else {
    for (let i = items.length; i < target; i++) {
      inventoryForm.value.items.push({ codigoBarra: '', tagRfid: '' })
    }
  }
}
function openAcquisition() { resetInventoryForm(); inventoryModalOpen.value = true }
async function saveAcquisition() {
  inventoryFormError.value = ''
  const form = inventoryForm.value
  const titulo = String(form.titulo || '').trim()
  const autor = String(form.autor || '').trim()
  const isbn = String(form.isbn || '').trim()
  const anio = form.anioPublicacion === '' || form.anioPublicacion === null ? null : Number(form.anioPublicacion)
  adjustItems()
  const items = (form.items || []).filter(i => i && i.codigoBarra && String(i.codigoBarra).trim())
  if (!titulo || !autor || !isbn) { inventoryFormError.value = 'Completa título, autor e ISBN.'; return }
  if (items.length === 0) { inventoryFormError.value = 'Asigna al menos un código de barras.'; return }
  for (const it of items) {
    const rfid = it.tagRfid ? String(it.tagRfid).trim().toUpperCase() : ''
    if (rfid && !/^[0-9A-F]{8,}$/.test(rfid)) { inventoryFormError.value = 'El tag RFID debe ser hexadecimal y tener al menos 8 caracteres.'; return }
  }
  const payload = {
    isbn,
    titulo,
    autor,
    anioPublicacion: anio,
    codigosBarra: items.map(i => String(i.codigoBarra).trim()),
    tagsRfid: items.map(i => i.tagRfid ? String(i.tagRfid).trim().toUpperCase() : null)
  }
  isSaving.value = true
  try {
    const res = await fetch('http://localhost:8080/api/admin/ejemplares/adquirir', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    if (res.status === 201 || res.ok) {
      inventoryModalOpen.value = false
      resetInventoryForm()
      await Promise.all([fetchKpis(), fetchInventory()])
      showToast('Material inventariado correctamente.')
    } else {
      const txt = await res.text().catch(() => '')
      inventoryFormError.value = txt || 'Error al registrar material.'
    }
  } catch (e) {
    inventoryFormError.value = 'Error de conexión con el servidor.'
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <div>
    <header class="sticky top-0 z-20 flex flex-wrap items-center justify-between gap-3 border-b border-neutral-200 bg-white px-4 py-3 lg:px-6">
      <nav aria-label="Ruta actual">
        <ol class="flex items-center gap-2 font-mono text-[10px] uppercase tracking-wider">
          <li class="text-neutral-400">Gestión</li>
          <li class="text-neutral-300" aria-hidden="true">/</li>
          <li class="text-neutral-900" aria-current="page">Inventario</li>
        </ol>
      </nav>
      <p class="font-mono text-[10px] uppercase tracking-wider text-neutral-400">Inventario</p>
    </header>

    <div class="mx-auto max-w-[1600px] p-4 lg:p-6">
      <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 class="text-xl font-semibold tracking-tight text-neutral-900">Inventario</h1>
          <p class="mt-1 text-xs text-neutral-500">Adquisición, codificación y control del material bibliográfico.</p>
        </div>
        <p class="font-mono text-[10px] uppercase tracking-widest text-neutral-400">CUS-05 / Módulo operativo</p>
      </div>

      <div class="mb-4 grid gap-3 sm:grid-cols-3">
        <KpiCard v-for="kpi in inventoryKpis" :key="kpi.label" :label="kpi.label" :value="kpi.value" :hint="kpi.hint" />
      </div>

      <section class="border border-neutral-200 bg-white">
        <div class="flex flex-col gap-3 border-b border-neutral-200 p-3 xl:flex-row xl:items-center xl:justify-between">
          <div class="flex flex-1 flex-wrap items-center gap-2">
            <input v-model="inventorySearch" type="search" placeholder="Buscar ISBN, código, RFID o título" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 text-sm text-neutral-900 sm:w-64" />
            <select v-model="inventoryState" class="rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-700">
              <option value="Todos">Estado: todos</option>
              <option value="Disponible">Disponible</option>
              <option value="En Préstamo">En Préstamo</option>
              <option value="Baja">Baja</option>
            </select>
          </div>
          <div class="flex items-center gap-3">
            <p v-if="selectedInventory.length" class="font-mono text-[10px] uppercase tracking-wider text-neutral-400">{{ selectedInventory.length }} seleccionados</p>
            <button type="button" class="inline-flex items-center justify-center border border-neutral-900 bg-neutral-900 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-white" @click="openAcquisition">+ Adquirir material</button>
          </div>
        </div>
        <DataTable v-model:selected="selectedInventory" :columns="inventoryColumns" :rows="inventory" selectable empty-message="Sin ejemplares para los filtros aplicados.">
          <template #cell-estado="{ value }">
            <span class="inline-flex border px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-wide" :class="inventoryStateClass(value)">{{ value }}</span>
          </template>
        </DataTable>
        <div class="flex flex-wrap items-center justify-between gap-2 border-t border-neutral-200 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-neutral-400">
          <span>{{ inventory.length }} registros</span>
          <span>Inventario general: {{ inventoryKpis[1].value }} ejemplares</span>
        </div>
      </section>
    </div>

    <ModalDialog v-model:open="inventoryModalOpen" title="Adquirir material" description="CUS-05 / Registro de inventario">
      <form class="space-y-4" @submit.prevent="saveAcquisition">
        <InlineAlert v-if="inventoryFormError" variant="error" dismissible @dismiss="inventoryFormError = ''">{{ inventoryFormError }}</InlineAlert>
        <div><label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">Título</label><input v-model="inventoryForm.titulo" type="text" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 text-sm text-neutral-900" /></div>
        <div><label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">Autor</label><input v-model="inventoryForm.autor" type="text" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 text-sm text-neutral-900" /></div>
        <div class="grid gap-3 sm:grid-cols-2">
          <div><label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">ISBN</label><input v-model="inventoryForm.isbn" type="text" placeholder="978-84-000000-00-0" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-900" /></div>
          <div><label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">Año de publicación</label><input v-model="inventoryForm.anioPublicacion" type="number" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-900" /></div>
        </div>
        <div>
          <div class="mb-2 flex items-center justify-between">
            <label class="block font-mono text-[10px] uppercase tracking-wider text-neutral-600">Ejemplares físicos</label>
            <div class="flex items-center gap-2">
              <span class="font-mono text-[10px] text-neutral-400">Cantidad</span>
              <input v-model.number="inventoryForm.cantidad" type="number" min="1" class="w-16 rounded-none border border-neutral-200 bg-white px-2 py-1 text-center font-mono text-xs" @change="adjustItems" />
            </div>
          </div>
          <div class="space-y-2">
            <div v-for="(item, idx) in inventoryForm.items" :key="idx" class="grid gap-2 sm:grid-cols-2">
              <input v-model="item.codigoBarra" type="text" placeholder="BC-000111" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-900" />
              <input v-model="item.tagRfid" type="text" placeholder="A1B2C3D4" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs uppercase text-neutral-900" />
            </div>
          </div>
          <p class="mt-1.5 font-mono text-[10px] text-neutral-400">Tag RFID opcional, debe ser hexadecimal mínimo 8 caracteres si se completa.</p>
        </div>
        <div class="flex flex-wrap justify-end gap-2 border-t border-neutral-200 pt-4">
          <button type="button" class="border border-neutral-200 bg-white px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-neutral-500" @click="inventoryModalOpen = false">Cancelar</button>
          <button type="submit" :disabled="isSaving" class="border border-neutral-900 bg-neutral-900 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-white disabled:opacity-50">{{ isSaving ? 'Guardando...' : 'Registrar material' }}</button>
        </div>
      </form>
    </ModalDialog>

    <Toast :open="toastState.open" :message="toastState.message" :variant="toastState.variant" @close="hideToast" />
  </div>
</template>
