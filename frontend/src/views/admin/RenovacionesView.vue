<script setup>
import { computed, ref } from 'vue'
import DataTable from '../../components/ui/DataTable.vue'
import InlineAlert from '../../components/ui/InlineAlert.vue'
import Toast from '../../components/ui/Toast.vue'
function isoDate(o=0){const d=new Date();d.setHours(12,0,0,0);d.setDate(d.getDate()+o);return d.toISOString().slice(0,10)}
function addDays(iso,days){const d=new Date(iso+'T12:00:00');d.setDate(d.getDate()+days);return d.toISOString().slice(0,10)}
function formatDate(iso){if(!iso)return '—';return new Intl.DateTimeFormat('es-PE',{day:'2-digit',month:'short',year:'numeric'}).format(new Date(iso+'T12:00:00')).replace('.','').toUpperCase()}
function normalize(v){return String(v??'').trim().toLocaleLowerCase('es')}
const readers=ref([{id:'L-001',nombre:'Lucía Fernández'},{id:'L-002',nombre:'Mateo Rivas'},{id:'L-003',nombre:'Valentina Cruz'},{id:'L-004',nombre:'Santiago Paredes'},{id:'L-005',nombre:'Camila Ortega'}])
const inventory=ref([{id:'EJ-0001',barcode:'BC-000101',titulo:'Las Formas del Espacio'},{id:'EJ-0002',barcode:'BC-000102',titulo:'Teoría de la Forma'},{id:'EJ-0003',barcode:'BC-000103',titulo:'El Mundo como Voluntad y Representación'},{id:'EJ-0006',barcode:'BC-000106',titulo:'El Ensayo como Forma'}])
const loans=ref([{id:'PRE-1042',lectorId:'L-001',ejemplarId:'EJ-0002',fechaSalida:isoDate(-18),fechaLimite:isoDate(-3),estado:'Vencido',hasThirdPartyReservation:false,renovaciones:1},{id:'PRE-1043',lectorId:'L-002',ejemplarId:'EJ-0006',fechaSalida:isoDate(-10),fechaLimite:isoDate(5),estado:'Activo',hasThirdPartyReservation:true,renovaciones:0},{id:'PRE-1044',lectorId:'L-004',ejemplarId:'EJ-0001',fechaSalida:isoDate(-4),fechaLimite:isoDate(11),estado:'Activo',hasThirdPartyReservation:false,renovaciones:0},{id:'PRE-1045',lectorId:'L-005',ejemplarId:'EJ-0003',fechaSalida:isoDate(-1),fechaLimite:isoDate(14),estado:'Activo',hasThirdPartyReservation:true,renovaciones:0}])
const readersById=computed(()=>new Map(readers.value.map(r=>[r.id,r]))); const copiesById=computed(()=>new Map(inventory.value.map(c=>[c.id,c])))
const loanRows=computed(()=>loans.value.map(l=>({...l,lectorNombre:readersById.value.get(l.lectorId)?.nombre??'—',lectorCodigo:l.lectorId,ejemplarCodigo:copiesById.value.get(l.ejemplarId)?.barcode??l.ejemplarId,ejemplarTitulo:copiesById.value.get(l.ejemplarId)?.titulo??'—'})))
const renewalSearch=ref(''); const renewalAlert=ref('')
const renewalRows=computed(()=>{const term=normalize(renewalSearch.value); return loanRows.value.filter(l=>{if(l.estado==='Devuelto')return false; if(!term)return true; return [l.id,l.lectorNombre,l.lectorCodigo,l.ejemplarCodigo,l.ejemplarTitulo].some(v=>normalize(v).includes(term))})})
const renewalColumns=[{key:'id',label:'ID préstamo',mono:true,sortable:true},{key:'ejemplarCodigo',label:'Ejemplar / código',mono:true,sortable:true},{key:'lectorNombre',label:'Lector',sortable:true},{key:'fechaLimite',label:'Fecha límite',mono:true,sortable:true,sortType:'date'},{key:'hasThirdPartyReservation',label:'Reserva',sortable:true},{key:'acciones',label:'Acción'}]
const toastState=ref({open:false,message:'',variant:'success'}); function showToast(m,v='success'){toastState.value={open:true,message:m,variant:v}} function hideToast(){toastState.value.open=false}
function renewLoan(row){const loan=loans.value.find(i=>i.id===row.id); if(!loan)return; if(loan.hasThirdPartyReservation){renewalAlert.value='No es posible renovar: Ejemplar reservado por otro lector (CUS-01 pendiente de retiro).';return} loan.fechaLimite=addDays(loan.fechaLimite,7); loan.renovaciones+=1; renewalAlert.value=''; showToast(`Préstamo ${loan.id} renovado. Nueva fecha límite: ${formatDate(loan.fechaLimite)}.`)}
</script>
<template>
  <div>
    <header class="sticky top-0 z-20 flex flex-wrap items-center justify-between gap-3 border-b border-neutral-200 bg-white px-4 py-3 lg:px-6"><nav><ol class="flex items-center gap-2 font-mono text-[10px] uppercase tracking-wider"><li class="text-neutral-400">Gestión</li><li class="text-neutral-300">/</li><li class="text-neutral-900">Renovaciones</li></ol></nav><p class="font-mono text-[10px] uppercase tracking-wider text-neutral-400">Renovaciones</p></header>
    <div class="mx-auto max-w-[1600px] p-4 lg:p-6">
      <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between"><div><h1 class="text-xl font-semibold tracking-tight text-neutral-900">Renovaciones</h1><p class="mt-1 text-xs text-neutral-500">Ampliación de plazo sujeta a reservas de terceros.</p></div><p class="font-mono text-[10px] uppercase tracking-widest text-neutral-400">CUS-04 / Módulo operativo</p></div>
      <InlineAlert v-if="renewalAlert" variant="warning" title="Renovación bloqueada" dismissible class="mb-4" @dismiss="renewalAlert=''">{{renewalAlert}}</InlineAlert>
      <section class="border border-neutral-200 bg-white">
        <div class="flex flex-col gap-3 border-b border-neutral-200 p-3 lg:flex-row lg:items-center lg:justify-between"><input v-model="renewalSearch" type="search" placeholder="Buscar ID, lector, código o título" class="w-full rounded-none border border-neutral-200 bg-white px-3 py-2 text-sm sm:w-72" /><p class="font-mono text-[10px] uppercase tracking-wider text-neutral-400">{{renewalRows.length}} préstamos vigentes</p></div>
        <DataTable :columns="renewalColumns" :rows="renewalRows" empty-message="Sin préstamos vigentes para la búsqueda ingresada.">
          <template #cell-ejemplarCodigo="{row}"><div class="flex flex-col gap-0.5"><span class="font-mono text-xs text-neutral-700">{{row.ejemplarCodigo}}</span><span class="max-w-56 truncate text-[11px] text-neutral-400">{{row.ejemplarTitulo}}</span></div></template>
          <template #cell-lectorNombre="{row}"><div class="flex flex-col gap-0.5"><span>{{row.lectorNombre}}</span><span class="font-mono text-[10px] text-neutral-400">{{row.lectorCodigo}}</span></div></template>
          <template #cell-fechaLimite="{value}"><span class="font-mono text-xs">{{formatDate(value)}}</span></template>
          <template #cell-hasThirdPartyReservation="{value}"><span class="inline-flex border px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-wide" :class="value?'border-amber-200 bg-amber-50 text-amber-900':'border-neutral-200 bg-neutral-50 text-neutral-400'">{{value?'Tercero':'Libre'}}</span></template>
          <template #cell-acciones="{row}"><button type="button" class="border px-2.5 py-1.5 font-mono text-[10px] uppercase tracking-wider" :class="row.hasThirdPartyReservation?'cursor-not-allowed border-neutral-200 bg-neutral-50 text-neutral-400':'border-neutral-900 bg-white text-neutral-900 hover:bg-neutral-900 hover:text-white'" :aria-disabled="row.hasThirdPartyReservation" @click="renewLoan(row)">Renovar plazo (+7 días)</button></template>
        </DataTable>
      </section>
    </div>
    <Toast :open="toastState.open" :message="toastState.message" :variant="toastState.variant" @close="hideToast" />
  </div>
</template>
