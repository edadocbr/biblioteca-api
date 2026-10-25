<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import DataTable from '../../components/ui/DataTable.vue'
import KpiCard from '../../components/ui/KpiCard.vue'
import ModalDialog from '../../components/ui/ModalDialog.vue'
import InlineAlert from '../../components/ui/InlineAlert.vue'
import Toast from '../../components/ui/Toast.vue'

const DAILY_FINE = 2.5
function formatDate(iso) {
  if (!iso) return '—'
  return new Intl.DateTimeFormat('es-PE', { day: '2-digit', month: 'short', year: 'numeric' })
    .format(new Date(iso + 'T12:00:00'))
    .replace('.', '')
    .toUpperCase()
}
function formatMoney(v) {
  return `S/ ${Number(v).toFixed(2)}`
}

const returns = ref([])
const kpiData = ref({ devolucionesHoy: 0, devolucionesEnFecha: 0, devolucionesConRetraso: 0 })
const returnSearch = ref('')
const selectedReturns = ref([])
const returnModalOpen = ref(false)
const returnFormError = ref('')
const isLoading = ref(false)
const isSaving = ref(false)

const returnForm = ref({
  codigoBarra: '',
  estadoConservacion: 'BUENO',
  observaciones: ''
})

const returnColumns = [
  { key: 'idPrestamo', label: 'ID Préstamo', mono: true, sortable: true },
  { key: 'codigoBarra', label: 'Ejemplar / Código', mono: true, sortable: true },
  { key: 'nombreLector', label: 'Lector', sortable: true },
  { key: 'fechaDevolucionPactada', label: 'Fecha límite', mono: true, sortable: true },
  { key: 'fechaDevolucionReal', label: 'Fecha devolución', mono: true, sortable: true },
  { key: 'diasRetraso', label: 'Retraso / Mora', mono: true, sortable: true, align: 'right' },
  { key: 'estado', label: 'Estado', sortable: true },
]

function mapRows(rows = []) {
  return rows.map((r) => ({
    ...r,
    id: r.idPrestamo,
    fechaPrestamoFormatted: r.fechaPrestamo ? formatDate(r.fechaPrestamo) : '—',
    fechaDevolucionPactadaFormatted: r.fechaDevolucionPactada ? formatDate(r.fechaDevolucionPactada) : '—',
    fechaDevolucionRealFormatted: r.fechaDevolucionReal ? formatDate(r.fechaDevolucionReal) : '—',
  }))
}

async function fetchKpis() {
  try {
    const res = await fetch('http://localhost:8080/api/admin/devoluciones/kpis')
    if (res.ok) kpiData.value = await res.json()
  } catch (e) {}
}

let searchTimer = null
async function fetchReturns() {
  isLoading.value = true
  try {
    const params = new URLSearchParams()
    const q = returnSearch.value.trim()
    if (q) params.append('q', q)
    const url = params.toString()
      ? `http://localhost:8080/api/admin/devoluciones?${params.toString()}`
      : 'http://localhost:8080/api/admin/devoluciones'
    const res = await fetch(url)
    if (res.ok) {
      const data = await res.json()
      returns.value = mapRows(Array.isArray(data) ? data : [])
    } else {
      returns.value = []
    }
  } catch (e) {
    returns.value = []
  } finally {
    isLoading.value = false
  }
}

watch(returnSearch, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => fetchReturns(), 300)
})

onMounted(async () => {
  await Promise.all([fetchKpis(), fetchReturns()])
})

const returnKpis = computed(() => [
  { label: 'Devoluciones hoy', value: kpiData.value.devolucionesHoy || 0, hint: 'Cierres del día' },
  { label: 'Devueltos a tiempo', value: kpiData.value.devolucionesEnFecha || 0, hint: 'Sin mora generada' },
  { label: 'Con retraso', value: kpiData.value.devolucionesConRetraso || 0, hint: 'Con penalidad / mora' },
])

function returnStateClass(state) {
  if (state === 'CON_RETRASO') return 'border-red-200 bg-red-50 text-red-700'
  return 'border-neutral-300 bg-white text-neutral-700'
}

const toastState = ref({ open: false, message: '', variant: 'success' })
function showToast(message, variant = 'success') {
  toastState.value = { open: true, message, variant }
}
function hideToast() {
  toastState.value.open = false
}

function resetReturnForm() {
  returnForm.value = {
    codigoBarra: '',
    estadoConservacion: 'BUENO',
    observaciones: ''
  }
  returnFormError.value = ''
}

function openReturnModal() {
  resetReturnForm()
  returnModalOpen.value = true
}

async function saveReturn() {
  returnFormError.value = ''
  const form = returnForm.value
  const codigoBarra = String(form.codigoBarra || '').trim()
  if (!codigoBarra) {
    returnFormError.value = 'Ingresa el código de barras o tag del ejemplar.'
    return
  }

  let idBibliotecario = null
  try {
    const raw = localStorage.getItem('sgbu_admin_user')
    if (raw) {
      const u = JSON.parse(raw)
      idBibliotecario = u.idUsuario || u.id || null
    }
  } catch (e) {}

  const payload = {
    codigoBarra,
    idBibliotecario,
    estadoConservacion: form.estadoConservacion,
    observaciones: form.observaciones ? form.observaciones.trim() : null
  }

  isSaving.value = true
  try {
    const res = await fetch('http://localhost:8080/api/admin/devoluciones', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (res.ok) {
      const result = await res.json()
      returnModalOpen.value = false
      resetReturnForm()
      await Promise.all([fetchKpis(), fetchReturns()])

      if (result.diasRetraso > 0) {
        const mora = result.diasRetraso * DAILY_FINE
        showToast(`Devolución registrada con ${result.diasRetraso} d de retraso. Multa generada: ${formatMoney(mora)}.`, 'warning')
      } else {
        showToast('Devolución registrada exitosamente. Ejemplar liberado sin mora.')
      }
    } else {
      const txt = await res.text().catch(() => '')
      try {
        const errObj = JSON.parse(txt)
        returnFormError.value = errObj.message || errObj.error || 'No se pudo procesar la devolución.'
      } catch (_) {
        returnFormError.value = txt || 'No se pudo procesar la devolución.'
      }
    }
  } catch (e) {
    returnFormError.value = 'Error de conexión con el servidor.'
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
          <li class="text-neutral-300">/</li>
          <li class="text-neutral-900">Devoluciones</li>
        </ol>
      </nav>
      <p class="font-mono text-[10px] uppercase tracking-wider text-neutral-400">Devoluciones</p>
    </header>

    <div class="mx-auto max-w-[1600px] p-4 lg:p-6">
      <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 class="text-xl font-semibold tracking-tight text-neutral-900">Devoluciones</h1>
          <p class="mt-1 text-xs text-neutral-500">Inspección del ejemplar, cálculo de mora y liberación de inventario.</p>
        </div>
        <p class="font-mono text-[10px] uppercase tracking-widest text-neutral-400">CUS-03 / Módulo operativo</p>
      </div>

      <div class="mb-4 grid gap-3 sm:grid-cols-3">
        <KpiCard v-for="kpi in returnKpis" :key="kpi.label" :label="kpi.label" :value="kpi.value" :hint="kpi.hint" />
      </div>

      <section class="border border-neutral-200 bg-white">
        <div class="flex flex-col gap-3 border-b border-neutral-200 p-3 lg:flex-row lg:items-center lg:justify-between">
          <input
            v-model="returnSearch"
            type="search"
            placeholder="Buscar código, lector, título o ID"
            class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 text-sm sm:w-72"
          />
          <button
            type="button"
            class="inline-flex items-center justify-center border border-neutral-900 bg-neutral-900 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-white"
            @click="openReturnModal"
          >
            + Registrar devolución
          </button>
        </div>

        <DataTable
          v-model:selected="selectedReturns"
          :columns="returnColumns"
          :rows="returns"
          selectable
          empty-message="Sin devoluciones registradas para los filtros aplicados."
        >
          <template #cell-idPrestamo="{ value }">
            <span class="font-mono text-xs text-neutral-700">PRE-{{ value }}</span>
          </template>
          <template #cell-codigoBarra="{ row }">
            <div class="flex flex-col gap-0.5">
              <span class="font-mono text-xs text-neutral-700">{{ row.codigoBarra }}</span>
              <span class="max-w-56 truncate text-[11px] text-neutral-400">{{ row.tituloLibro }}</span>
            </div>
          </template>
          <template #cell-nombreLector="{ row }">
            <div class="flex flex-col gap-0.5">
              <span>{{ row.nombreLector }}</span>
              <span class="font-mono text-[10px] text-neutral-400">{{ row.codigoEstudiante }}</span>
            </div>
          </template>
          <template #cell-fechaDevolucionPactada="{ row }">
            <span class="font-mono text-xs">{{ row.fechaDevolucionPactadaFormatted }}</span>
          </template>
          <template #cell-fechaDevolucionReal="{ row }">
            <span class="font-mono text-xs">{{ row.fechaDevolucionRealFormatted }}</span>
          </template>
          <template #cell-diasRetraso="{ row }">
            <span v-if="row.diasRetraso > 0" class="font-mono text-xs font-semibold text-red-700">
              +{{ row.diasRetraso }} d ({{ formatMoney(row.diasRetraso * DAILY_FINE) }})
            </span>
            <span v-else class="font-mono text-xs text-neutral-400">Sin retraso</span>
          </template>
          <template #cell-estado="{ value }">
            <span
              class="inline-flex border px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-wide"
              :class="returnStateClass(value)"
            >
              {{ value === 'CON_RETRASO' ? 'Con retraso' : 'A tiempo' }}
            </span>
          </template>
        </DataTable>

        <div class="flex flex-wrap items-center justify-between gap-2 border-t border-neutral-200 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-neutral-400">
          <span>{{ returns.length }} devoluciones registradas</span>
          <span>Liquidación automática de mora: S/ 2.50 / día</span>
        </div>
      </section>
    </div>

    <ModalDialog v-model:open="returnModalOpen" title="Registrar devolución" description="CUS-03 / Retorno y control de ejemplar">
      <form class="space-y-4" @submit.prevent="saveReturn">
        <InlineAlert v-if="returnFormError" variant="error" dismissible @dismiss="returnFormError = ''">
          {{ returnFormError }}
        </InlineAlert>

        <div>
          <label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">
            Código de barras o RFID del ejemplar
          </label>
          <input
            v-model="returnForm.codigoBarra"
            type="text"
            placeholder="BC-000104 o Tag RFID"
            class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-900 uppercase"
            required
          />
          <p class="mt-1 font-mono text-[10px] text-neutral-400">
            También puedes ingresar el ID de préstamo (ej. PRE-1041 o 1041).
          </p>
        </div>

        <div>
          <label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">
            Estado de conservación físico
          </label>
          <select
            v-model="returnForm.estadoConservacion"
            class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-900"
          >
            <option value="BUENO">Buen estado (Reintegrar a estante / Disponible)</option>
            <option value="DANADO">Dañado / Deteriorado (Enviar a Baja / Reparación)</option>
          </select>
        </div>

        <div>
          <label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">
            Observaciones (opcional)
          </label>
          <input
            v-model="returnForm.observaciones"
            type="text"
            placeholder="Anotaciones sobre el estado del material devuelto"
            class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 text-xs text-neutral-900"
          />
        </div>

        <div class="flex flex-wrap justify-end gap-2 border-t border-neutral-200 pt-4">
          <button
            type="button"
            class="border border-neutral-200 bg-white px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-neutral-500"
            @click="returnModalOpen = false"
          >
            Cancelar
          </button>
          <button
            type="submit"
            :disabled="isSaving"
            class="border border-neutral-900 bg-neutral-900 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-white disabled:opacity-50"
          >
            {{ isSaving ? 'Procesando...' : 'Confirmar devolución' }}
          </button>
        </div>
      </form>
    </ModalDialog>

    <Toast :open="toastState.open" :message="toastState.message" :variant="toastState.variant" @close="hideToast" />
  </div>
</template>
