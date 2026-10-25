<script setup>
import { apiFetch } from '../../api/client.js'
import { computed, onMounted, ref, watch } from 'vue'
import DataTable from '../../components/ui/DataTable.vue'
import KpiCard from '../../components/ui/KpiCard.vue'
import ModalDialog from '../../components/ui/ModalDialog.vue'
import InlineAlert from '../../components/ui/InlineAlert.vue'
import Toast from '../../components/ui/Toast.vue'

function formatDate(iso) {
  if (!iso) return '—'
  return new Intl.DateTimeFormat('es-PE', { day: '2-digit', month: 'short', year: 'numeric' })
    .format(new Date(iso + 'T12:00:00'))
    .replace('.', '')
    .toUpperCase()
}

const loans = ref([])
const kpiData = ref({ renovacionesHoy: 0, prestamosPorVencer: 0, prestamosRenovados: 0 })
const loanSearch = ref('')
const loanModalOpen = ref(false)
const loanFormError = ref('')
const isLoading = ref(false)
const isSaving = ref(false)

const loanForm = ref({
  codigoBarra: '',
  diasExtension: 7
})

const loanColumns = [
  { key: 'idPrestamo', label: 'ID préstamo', mono: true, sortable: true },
  { key: 'codigoBarra', label: 'Ejemplar / código', mono: true, sortable: true },
  { key: 'nombreLector', label: 'Lector', sortable: true },
  { key: 'fechaDevolucionPactada', label: 'Fecha límite', mono: true, sortable: true },
  { key: 'renovacionesRealizadas', label: 'Renovaciones', mono: true, sortable: true, align: 'center' },
  { key: 'tieneReservaTerceros', label: 'Reserva', sortable: true },
  { key: 'acciones', label: 'Acción' }
]

function mapRows(rows = []) {
  return rows.map((r) => ({
    ...r,
    id: r.idPrestamo,
    fechaDevolucionPactadaFormatted: r.fechaDevolucionPactada ? formatDate(r.fechaDevolucionPactada) : '—',
    tieneReservaLabel: r.tieneReservaTerceros ? 'Tercero' : 'Libre',
    tieneReservaClass: r.tieneReservaTerceros ? 'border-amber-200 bg-amber-50 text-amber-900' : 'border-neutral-200 bg-neutral-50 text-neutral-400',
    renovable: r.esRenovable
  }))
}

async function fetchKpis() {
  try {
    const res = await apiFetch('/admin/renovaciones/kpis')
    if (res.ok) kpiData.value = await res.json()
  } catch (e) {}
}

let searchTimer = null
async function fetchLoans() {
  isLoading.value = true
  try {
    const params = new URLSearchParams()
    const q = loanSearch.value.trim()
    if (q) params.append('q', q)
    const url = params.toString()
      ? `/admin/renovaciones?${params.toString()}`
      : '/admin/renovaciones'
    const res = await apiFetch(url)
    if (res.ok) {
      const data = await res.json()
      loans.value = mapRows(Array.isArray(data) ? data : [])
    } else {
      loans.value = []
    }
  } catch (e) {
    loans.value = []
  } finally {
    isLoading.value = false
  }
}

watch(loanSearch, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => fetchLoans(), 300)
})

onMounted(async () => {
  await Promise.all([fetchKpis(), fetchLoans()])
})

const loanKpis = computed(() => [
  { label: 'Renovaciones hoy', value: kpiData.value.renovacionesHoy || 0, hint: 'Préstamos renovados en el día' },
  { label: 'Por vencer', value: kpiData.value.prestamosPorVencer || 0, hint: 'Vencen en los próximos 3 días' },
  { label: 'Renovados', value: kpiData.value.prestamosRenovados || 0, hint: 'Extendidos al menos una vez' }
])

const toastState = ref({ open: false, message: '', variant: 'success' })
function showToast(message, variant = 'success') {
  toastState.value = { open: true, message, variant }
}
function hideToast() {
  toastState.value.open = false
}

function resetLoanForm() {
  loanForm.value = { codigoBarra: '', diasExtension: 7 }
  loanFormError.value = ''
}

function openLoanModal() {
  resetLoanForm()
  loanModalOpen.value = true
}

async function saveRenewal() {
  loanFormError.value = ''
  const codigoBarra = String(loanForm.value.codigoBarra || '').trim()
  if (!codigoBarra) {
    loanFormError.value = 'Ingresa el código de barras o ID de préstamo.'
    return
  }

  const payload = {
    codigoBarra,
    diasExtension: Number(loanForm.value.diasExtension) || 7
  }

  isSaving.value = true
  try {
    const res = await apiFetch('/admin/renovaciones', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (res.ok) {
      const result = await res.json()
      loanModalOpen.value = false
      resetLoanForm()
      await Promise.all([fetchKpis(), fetchLoans()])
      const fecha = result.fechaDevolucionPactada ? formatDate(result.fechaDevolucionPactada) : 'nueva fecha'
      showToast(`Préstamo ${result.idPrestamo} renovado. Nueva fecha límite: ${fecha}.`)
    } else {
      const txt = await res.text().catch(() => '')
      try {
        const errObj = JSON.parse(txt)
        loanFormError.value = errObj.message || errObj.error || 'No se pudo procesar la renovación.'
      } catch (_) {
        loanFormError.value = txt || 'No se pudo procesar la renovación.'
      }
    }
  } catch (e) {
    loanFormError.value = 'Error de conexión con el servidor.'
  } finally {
    isSaving.value = false
  }
}

async function renovarFila(row) {
  loanFormError.value = ''
  const payload = {
    codigoBarra: row.codigoBarra,
    diasExtension: 7
  }

  isSaving.value = true
  try {
    const res = await apiFetch('/admin/renovaciones', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (res.ok) {
      const result = await res.json()
      await Promise.all([fetchKpis(), fetchLoans()])
      const fecha = result.fechaDevolucionPactada ? formatDate(result.fechaDevolucionPactada) : 'nueva fecha'
      showToast(`Préstamo ${result.idPrestamo} renovado. Nueva fecha límite: ${fecha}.`)
    } else {
      const txt = await res.text().catch(() => '')
      try {
        const errObj = JSON.parse(txt)
        loanFormError.value = errObj.message || errObj.error || 'No se pudo procesar la renovación.'
      } catch (_) {
        loanFormError.value = txt || 'No se pudo procesar la renovación.'
      }
    }
  } catch (e) {
    loanFormError.value = 'Error de conexión con el servidor.'
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
          <li class="text-neutral-900">Renovaciones</li>
        </ol>
      </nav>
      <p class="font-mono text-[10px] uppercase tracking-wider text-neutral-400">Renovaciones</p>
    </header>

    <div class="mx-auto max-w-[1600px] p-4 lg:p-6">
      <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 class="text-xl font-semibold tracking-tight text-neutral-900">Renovaciones</h1>
          <p class="mt-1 text-xs text-neutral-500">Ampliación de plazo sujeta a reservas de terceros.</p>
        </div>
        <p class="font-mono text-[10px] uppercase tracking-widest text-neutral-400">CUS-04 / Módulo operativo</p>
      </div>

      <div class="mb-4 grid gap-3 sm:grid-cols-3">
        <KpiCard v-for="kpi in loanKpis" :key="kpi.label" :label="kpi.label" :value="kpi.value" :hint="kpi.hint" />
      </div>

      <InlineAlert v-if="loanFormError" variant="warning" title="Renovación bloqueada" dismissible @dismiss="loanFormError = ''">
        {{ loanFormError }}
      </InlineAlert>

      <section class="border border-neutral-200 bg-white">
        <div class="flex flex-col gap-3 border-b border-neutral-200 p-3 lg:flex-row lg:items-center lg:justify-between">
          <input
            v-model="loanSearch"
            type="search"
            placeholder="Buscar ID, lector, código o título"
            class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 text-sm sm:w-72"
          />
          <button
            type="button"
            class="inline-flex items-center justify-center border border-neutral-900 bg-neutral-900 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-white"
            @click="openLoanModal"
          >
            + Solicitar renovación
          </button>
        </div>

        <DataTable
          :columns="loanColumns"
          :rows="loans"
          empty-message="Sin préstamos vigentes para la búsqueda ingresada."
        >
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
          <template #cell-renovacionesRealizadas="{ value }">
            <span class="font-mono text-xs">{{ value }}</span>
          </template>
          <template #cell-tieneReservaTerceros="{ row }">
            <span class="inline-flex border px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-wide" :class="row.tieneReservaClass">
              {{ row.tieneReservaLabel }}
            </span>
          </template>
          <template #cell-acciones="{ row }">
            <button
              type="button"
              class="border px-2.5 py-1.5 font-mono text-[10px] uppercase tracking-wider"
              :class="
                row.renovable && !row.tieneReservaTerceros
                  ? 'border-neutral-900 bg-white text-neutral-900 hover:bg-neutral-900 hover:text-white'
                  : 'cursor-not-allowed border-neutral-200 bg-neutral-50 text-neutral-400'
              "
              :aria-disabled="!row.renovable || row.tieneReservaTerceros"
              @click="row.renovable && !row.tieneReservaTerceros ? renovarFila(row) : null"
            >
              {{ row.renovable ? 'Renovar plazo (+7 días)' : 'No renovable' }}
            </button>
          </template>
        </DataTable>

        <div class="flex flex-wrap items-center justify-between gap-2 border-t border-neutral-200 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-neutral-400">
          <span>{{ loans.length }} préstamos vigentes</span>
        </div>
      </section>
    </div>

    <ModalDialog v-model:open="loanModalOpen" title="Solicitar renovación" description="CUS-04 / Ampliación de plazo de préstamo">
      <form class="space-y-4" @submit.prevent="saveRenewal">
        <InlineAlert v-if="loanFormError" variant="error" dismissible @dismiss="loanFormError = ''">
          {{ loanFormError }}
        </InlineAlert>

        <div>
          <label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">
            Código de barras del ejemplar o ID de préstamo
          </label>
          <input
            v-model="loanForm.codigoBarra"
            type="text"
            placeholder="BC-000104 o PRE-1042"
            class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-900 uppercase"
            required
          />
          <p class="mt-1 font-mono text-[10px] text-neutral-400">
            También puedes ingresar el ID de préstamo (ej. PRE-1041 o 1041).
          </p>
        </div>

        <div>
          <label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">
            Días de extensión
          </label>
          <input
            v-model.number="loanForm.diasExtension"
            type="number"
            min="1"
            placeholder="7"
            class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-900"
          />
          <p class="mt-1 font-mono text-[10px] text-neutral-400">Por defecto se amplían 7 días adicionales.</p>
        </div>

        <div class="flex flex-wrap justify-end gap-2 border-t border-neutral-200 pt-4">
          <button
            type="button"
            class="border border-neutral-200 bg-white px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-neutral-500"
            @click="loanModalOpen = false"
          >
            Cancelar
          </button>
          <button
            type="submit"
            :disabled="isSaving"
            class="border border-neutral-900 bg-neutral-900 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-white disabled:opacity-50"
          >
            {{ isSaving ? 'Procesando...' : 'Confirmar renovación' }}
          </button>
        </div>
      </form>
    </ModalDialog>

    <Toast :open="toastState.open" :message="toastState.message" :variant="toastState.variant" @close="hideToast" />
  </div>
</template>
