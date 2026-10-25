<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import DataTable from '../../components/ui/DataTable.vue'
import ModalDialog from '../../components/ui/ModalDialog.vue'
import Toast from '../../components/ui/Toast.vue'
import KpiCard from '../../components/ui/KpiCard.vue'
import InlineAlert from '../../components/ui/InlineAlert.vue'

function formatDate(iso){ if(!iso)return '—'; return new Intl.DateTimeFormat('es-PE',{day:'2-digit',month:'short',year:'numeric'}).format(new Date(iso+'T12:00:00')).replace('.','').toUpperCase() }

const loans = ref([])
const kpiData = ref({ prestamosActivos: 0, prestamosVencidos: 0, prestamosHoy: 0 })
const loanSearch = ref('')
const selectedLoans = ref([])
const loanModalOpen = ref(false)
const loanFormError = ref('')
const isLoading = ref(false)
const isSaving = ref(false)
const loanForm = ref({ identificadorLector: '', codigoBarra: '', diasPrestamo: 7 })

const loanColumns = [
  { key: 'idPrestamo', label: 'ID préstamo', mono: true, sortable: true },
  { key: 'codigoBarra', label: 'Ejemplar / código', mono: true, sortable: true },
  { key: 'nombreLector', label: 'Lector', sortable: true },
  { key: 'fechaPrestamo', label: 'Fecha salida', mono: true, sortable: true },
  { key: 'fechaDevolucionPactada', label: 'Fecha límite', mono: true, sortable: true },
  { key: 'estado', label: 'Estado', sortable: true },
]

function mapRows(rows = []) {
  return rows.map((r) => ({
    ...r,
    fechaPrestamo: r.fechaPrestamo ? formatDate(r.fechaPrestamo) : '—',
    fechaDevolucionPactada: r.fechaDevolucionPactada ? formatDate(r.fechaDevolucionPactada) : '—'
  }))
}

async function fetchKpis() {
  try {
    const res = await fetch('http://localhost:8080/api/admin/prestamos/kpis')
    if (res.ok) kpiData.value = await res.json()
  } catch (e) {}
}

let timer = null
async function fetchLoans() {
  isLoading.value = true
  try {
    const params = new URLSearchParams()
    const q = loanSearch.value.trim()
    if (q) params.append('q', q)
    const url = params.toString() ? `http://localhost:8080/api/admin/prestamos?${params.toString()}` : 'http://localhost:8080/api/admin/prestamos'
    const res = await fetch(url)
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
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => fetchLoans(), 300)
})

onMounted(async () => {
  await Promise.all([fetchKpis(), fetchLoans()])
})

const loanKpis = computed(() => [
  { label: 'Préstamos activos', value: kpiData.value.prestamosActivos || 0, hint: 'Salidas vigentes' },
  { label: 'Entregas hoy', value: kpiData.value.prestamosHoy || 0, hint: 'Registros del día' },
  { label: 'Préstamos vencidos', value: kpiData.value.prestamosVencidos || 0, hint: 'Requieren seguimiento' },
])
function loanStateClass(s){
  if (s === 'VENCIDO') return 'border-red-200 bg-red-50 text-red-700'
  if (s === 'DEVUELTO') return 'border-neutral-200 bg-neutral-50 text-neutral-400'
  return 'border-neutral-300 bg-white text-neutral-700'
}
const toastState=ref({open:false,message:'',variant:'success'}); function showToast(m,v='success'){toastState.value={open:true,message:m,variant:v}} function hideToast(){toastState.value.open=false}
function resetLoanForm(){loanForm.value={identificadorLector:'',codigoBarra:'',diasPrestamo:7};loanFormError.value=''}
function openLoanModal(){resetLoanForm();loanModalOpen.value=true}
async function saveLoan(){
  loanFormError.value = ''
  const form = loanForm.value
  const identificadorLector = String(form.identificadorLector||'').trim()
  const codigoBarra = String(form.codigoBarra||'').trim()
  if (!identificadorLector || !codigoBarra){ loanFormError.value='Ingresa identificador del lector y código de barras del ejemplar.'; return }
  let idBibliotecario = null
  try {
    const raw = localStorage.getItem('usuario_sgbu')
    if (raw){
      const u = JSON.parse(raw)
      idBibliotecario = u.idUsuario || u.id || null
    }
  } catch(e){}
  const payload = { identificadorLector, codigoBarra, diasPrestamo: Number(form.diasPrestamo)||7, idBibliotecario }
  isSaving.value = true
  try {
    const res = await fetch('http://localhost:8080/api/admin/prestamos', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    if (res.status === 201 || res.ok){
      loanModalOpen.value = false
      resetLoanForm()
      await Promise.all([fetchKpis(), fetchLoans()])
      showToast('Salida registrada correctamente.')
    } else {
      const txt = await res.text().catch(()=> '')
      loanFormError.value = txt || 'No se pudo registrar el préstamo.'
    }
  } catch(e){
    loanFormError.value = 'Error de conexión con el servidor.'
  } finally {
    isSaving.value = false
  }
}
</script>
<template>
  <div>
    <header class="sticky top-0 z-20 flex flex-wrap items-center justify-between gap-3 border-b border-neutral-200 bg-white px-4 py-3 lg:px-6">
      <nav aria-label="Ruta actual"><ol class="flex items-center gap-2 font-mono text-[10px] uppercase tracking-wider"><li class="text-neutral-400">Gestión</li><li class="text-neutral-300">/</li><li class="text-neutral-900">Préstamos</li></ol></nav>
      <p class="font-mono text-[10px] uppercase tracking-wider text-neutral-400">Préstamos</p>
    </header>
    <div class="mx-auto max-w-[1600px] p-4 lg:p-6">
      <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div><h1 class="text-xl font-semibold tracking-tight text-neutral-900">Préstamos</h1><p class="mt-1 text-xs text-neutral-500">Salida física de ejemplares con registro formal del lector.</p></div>
        <p class="font-mono text-[10px] uppercase tracking-widest text-neutral-400">CUS-02 / Módulo operativo</p>
      </div>
      <div class="mb-4 grid gap-3 sm:grid-cols-3">
        <KpiCard v-for="kpi in loanKpis" :key="kpi.label" :label="kpi.label" :value="kpi.value" :hint="kpi.hint" />
      </div>
      <section class="border border-neutral-200 bg-white">
        <div class="flex flex-col gap-3 border-b border-neutral-200 p-3 lg:flex-row lg:items-center lg:justify-between">
          <input v-model="loanSearch" type="search" placeholder="Buscar lector, código, título o ID" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 text-sm sm:w-72" />
          <button type="button" class="inline-flex items-center justify-center border border-neutral-900 bg-neutral-900 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-white" @click="openLoanModal">+ Registrar salida</button>
        </div>
        <DataTable v-model:selected="selectedLoans" :columns="loanColumns" :rows="loans" selectable empty-message="Sin préstamos para la búsqueda ingresada.">
          <template #cell-codigoBarra="{row}"><div class="flex flex-col gap-0.5"><span class="font-mono text-xs text-neutral-700">{{row.codigoBarra}}</span><span class="max-w-56 truncate text-[11px] text-neutral-400">{{row.tituloLibro}}</span></div></template>
          <template #cell-nombreLector="{row}"><div class="flex flex-col gap-0.5"><span>{{row.nombreLector}}</span><span class="font-mono text-[10px] text-neutral-400">{{row.codigoEstudiante}}</span></div></template>
          <template #cell-estado="{value}"><span class="inline-flex border px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-wide" :class="loanStateClass(value)">{{value}}</span></template>
        </DataTable>
        <div class="flex flex-wrap items-center justify-between gap-2 border-t border-neutral-200 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-neutral-400">
          <span>{{ loans.length }} registros</span>
          <span>{{ kpiData.prestamosActivos || 0 }} préstamos vigentes</span>
        </div>
      </section>
    </div>
    <ModalDialog v-model:open="loanModalOpen" title="Registrar salida" description="CUS-02 / Préstamo de ejemplar">
      <form class="space-y-4" @submit.prevent="saveLoan">
        <InlineAlert v-if="loanFormError" variant="error" dismissible @dismiss="loanFormError=''">{{loanFormError}}</InlineAlert>
        <div><label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">Identificador del lector</label><input v-model="loanForm.identificadorLector" type="text" placeholder="DNI o código de estudiante" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-900" /></div>
        <div><label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">Código de barras del ejemplar</label><input v-model="loanForm.codigoBarra" type="text" placeholder="BC-000105" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-900" /></div>
        <div><label class="mb-1.5 block font-mono text-[10px] uppercase tracking-wider text-neutral-600">Plazo de devolución (días)</label><input v-model.number="loanForm.diasPrestamo" type="number" min="1" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 font-mono text-xs text-neutral-900" /><p class="mt-1.5 font-mono text-[10px] text-neutral-400">Por defecto 7 días.</p></div>
        <div class="flex flex-wrap justify-end gap-2 border-t border-neutral-200 pt-4"><button type="button" class="border border-neutral-200 bg-white px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-neutral-500" @click="loanModalOpen=false">Cancelar</button><button type="submit" :disabled="isSaving" class="border border-neutral-900 bg-neutral-900 px-3 py-2 font-mono text-[10px] uppercase tracking-wider text-white disabled:opacity-50">{{ isSaving ? 'Guardando...' : 'Formalizar préstamo' }}</button></div>
      </form>
    </ModalDialog>
    <Toast :open="toastState.open" :message="toastState.message" :variant="toastState.variant" @close="hideToast" />
  </div>
</template>
