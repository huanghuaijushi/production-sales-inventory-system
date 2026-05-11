<template>
  <div class="goods-page">
    <div class="page-header">
      <div>
        <p class="page-eyebrow">商品管理</p>
        <h1>销售商品与匹配规则</h1>
        <p>销售商品负责对外售卖、渠道价格和库存组成；库存产品只负责仓库数量和成本。</p>
      </div>
      <div class="header-actions">
        <button v-if="activeTab === 'goods'" type="button" class="primary-button" @click="openGoodsModal()">新增销售商品</button>
        <button v-if="activeTab === 'mappings'" type="button" class="primary-button" @click="openMappingModal()">新增商品匹配</button>
      </div>
    </div>

    <nav class="tab-bar">
      <button type="button" :class="{ active: activeTab === 'goods' }" @click="activeTab = 'goods'">销售商品</button>
      <button type="button" :class="{ active: activeTab === 'mappings' }" @click="activeTab = 'mappings'">商品匹配</button>
    </nav>

    <p v-if="message" class="operation-message">{{ message }}</p>

    <template v-if="activeTab === 'goods'">
      <section class="filter-panel">
        <div class="toolbar">
          <label>
            <span>商品搜索</span>
            <input v-model="goodsQuery" type="search" placeholder="编码、名称、分类或规格" @keyup.enter="loadGoods(true)" />
          </label>
          <div class="filter-actions">
            <button type="button" class="primary-button" @click="loadGoods(true)">查询</button>
            <button type="button" class="secondary-button" @click="goodsQuery = ''; loadGoods(true)">重置</button>
          </div>
        </div>
      </section>

      <section class="card-section">
        <div class="list-header">
          <div>
            <h2>销售商品</h2>
            <p>共 {{ goodsPage.totalElements }} 条</p>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr><th>销售商品</th><th>默认价</th><th>库存组成</th><th>渠道价格</th><th>状态</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="goods in goodsList" :key="goods.id">
                <td>
                  <div class="strong-text">{{ goods.name }}</div>
                  <div class="muted-text">{{ goods.code }} · {{ goods.specification || '-' }} · {{ goods.unit }}</div>
                </td>
                <td class="money-cell">{{ formatMoney(goods.defaultPrice) }}</td>
                <td>
                  <div class="compact-lines">
                    <span v-for="component in goods.components" :key="component.id">{{ component.productName }} x {{ component.quantityPerUnit }}{{ component.productUnit }}</span>
                  </div>
                </td>
                <td>
                  <div class="compact-lines">
                    <span v-for="price in goods.channelPrices" :key="price.id">{{ price.channelName }} {{ formatMoney(price.price) }}</span>
                    <span v-if="goods.channelPrices.length === 0">-</span>
                  </div>
                </td>
                <td>{{ goods.enabled ? '启用' : '停用' }}</td>
                <td><button type="button" class="text-button" @click="openGoodsModal(goods)">编辑</button></td>
              </tr>
              <tr v-if="goodsList.length === 0"><td colspan="6" class="empty-cell">暂无销售商品</td></tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>

    <template v-else>
      <section class="filter-panel">
        <div class="toolbar">
          <label>
            <span>渠道筛选</span>
            <select v-model.number="mappingFilterChannelId" @change="loadMappings">
              <option :value="0">全部渠道</option>
              <option v-for="channel in channels" :key="channel.id" :value="channel.id">{{ channel.name }}</option>
            </select>
          </label>
          <div class="filter-actions">
            <button type="button" class="primary-button" @click="openMappingModal()">新增商品匹配</button>
          </div>
        </div>
      </section>

      <section class="card-section">
        <div class="list-header">
          <div>
            <h2>商品匹配规则</h2>
            <p>外部商品先匹配销售商品，再由销售商品展开扣减库存产品。</p>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead><tr><th>渠道</th><th>外部商品</th><th>销售商品</th><th>换算</th><th>默认成交价</th><th>匹配方式</th><th>状态</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="mapping in mappings" :key="mapping.id">
                <td>{{ mapping.channelName }}</td>
                <td><div class="strong-text">{{ mapping.externalProductName }}</div><div class="muted-text">{{ mapping.externalSpecName || '-' }}</div></td>
                <td><div class="strong-text">{{ mapping.salesGoodsName }}</div><div class="muted-text">{{ mapping.salesGoodsCode }} · {{ mapping.salesGoodsUnit }}</div></td>
                <td>{{ mapping.quantityMultiplier }}</td>
                <td>{{ mapping.defaultUnitPrice == null ? '-' : formatMoney(mapping.defaultUnitPrice) }}</td>
                <td>{{ mapping.matchType === 'EXACT' ? '精确匹配' : '包含匹配' }}</td>
                <td>{{ mapping.enabled ? '启用' : '停用' }}</td>
                <td><button type="button" class="text-button" @click="openMappingModal(mapping)">编辑</button></td>
              </tr>
              <tr v-if="mappings.length === 0"><td colspan="8" class="empty-cell">暂无商品匹配</td></tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>

    <div v-if="goodsModalOpen" class="modal-backdrop">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ goodsForm.id ? '编辑销售商品' : '新增销售商品' }}</h2>
          <button type="button" class="icon-button" @click="goodsModalOpen = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-grid">
            <label><span>商品编码</span><input v-model="goodsForm.code" type="text" /></label>
            <label><span>商品名称</span><input v-model="goodsForm.name" type="text" /></label>
            <label><span>分类</span><input v-model="goodsForm.category" type="text" /></label>
            <label><span>规格</span><input v-model="goodsForm.specification" type="text" /></label>
            <label><span>销售单位</span><input v-model="goodsForm.unit" type="text" placeholder="件、盒、套" /></label>
            <label><span>默认售价</span><input v-model.number="goodsForm.defaultPrice" type="number" min="0" step="0.01" /></label>
            <label><span>状态</span><select v-model="goodsForm.enabled"><option :value="true">启用</option><option :value="false">停用</option></select></label>
          </div>

          <div class="items-header">
            <h3>库存组成</h3>
            <button type="button" class="secondary-button compact-button" @click="addComponent">添加库存产品</button>
          </div>
          <div class="line-items">
            <div v-for="(component, index) in goodsForm.components" :key="index" class="line-item-row">
              <select v-model.number="component.productId">
                <option :value="0" disabled>选择库存产品</option>
                <option v-for="product in products" :key="product.id" :value="product.id">{{ product.sku }} · {{ product.name }} · {{ product.unit }}</option>
              </select>
              <input v-model.number="component.quantityPerUnit" type="number" min="0.0001" step="0.0001" placeholder="消耗数量" />
              <input v-model.number="component.lossRate" type="number" min="0" max="1" step="0.0001" placeholder="损耗率" />
              <button type="button" class="text-button danger-text" @click="removeComponent(index)">删除</button>
            </div>
          </div>

          <div class="items-header">
            <h3>渠道价格</h3>
            <button type="button" class="secondary-button compact-button" @click="addChannelPrice">添加渠道价</button>
          </div>
          <div class="line-items">
            <div v-for="(price, index) in goodsForm.channelPrices" :key="index" class="line-item-row price-row">
              <select v-model.number="price.channelId">
                <option :value="0" disabled>选择渠道</option>
                <option v-for="channel in channels" :key="channel.id" :value="channel.id">{{ channel.name }}</option>
              </select>
              <input v-model.number="price.price" type="number" min="0" step="0.01" placeholder="渠道价" />
              <select v-model="price.enabled"><option :value="true">启用</option><option :value="false">停用</option></select>
              <button type="button" class="text-button danger-text" @click="removeChannelPrice(index)">删除</button>
            </div>
          </div>

          <label class="full-field"><span>备注</span><textarea v-model="goodsForm.remark" rows="2"></textarea></label>
        </div>
        <div class="modal-footer">
          <span></span>
          <div class="footer-actions">
            <button type="button" class="secondary-button" @click="goodsModalOpen = false">取消</button>
            <button type="button" class="primary-button" :disabled="submitting" @click="submitGoods">{{ submitting ? '保存中...' : '保存商品' }}</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="mappingModalOpen" class="modal-backdrop">
      <div class="modal-content small-modal">
        <div class="modal-header"><h2>{{ mappingForm.id ? '编辑商品匹配' : '新增商品匹配' }}</h2><button type="button" class="icon-button" @click="mappingModalOpen = false">×</button></div>
        <div class="modal-body">
          <div class="form-grid">
            <label><span>渠道</span><select v-model.number="mappingForm.channelId"><option :value="0" disabled>选择渠道</option><option v-for="channel in channels" :key="channel.id" :value="channel.id">{{ channel.name }}</option></select></label>
            <label><span>销售商品</span><select v-model.number="mappingForm.salesGoodsId"><option :value="0" disabled>选择销售商品</option><option v-for="goods in enabledGoods" :key="goods.id" :value="goods.id">{{ goods.name }} · {{ goods.unit }}</option></select></label>
            <label><span>外部商品名称</span><input v-model="mappingForm.externalProductName" type="text" /></label>
            <label><span>外部规格</span><input v-model="mappingForm.externalSpecName" type="text" /></label>
            <label><span>外部SKU</span><input v-model="mappingForm.externalSkuCode" type="text" /></label>
            <label><span>数量换算倍数</span><input v-model.number="mappingForm.quantityMultiplier" type="number" min="0.0001" step="0.0001" /></label>
            <label><span>默认成交价</span><input v-model.number="mappingForm.defaultUnitPrice" type="number" min="0" step="0.01" /></label>
            <label><span>匹配方式</span><select v-model="mappingForm.matchType"><option value="EXACT">精确匹配</option><option value="CONTAINS">包含匹配</option></select></label>
            <label><span>优先级</span><input v-model.number="mappingForm.priority" type="number" step="1" /></label>
            <label><span>状态</span><select v-model="mappingForm.enabled"><option :value="true">启用</option><option :value="false">停用</option></select></label>
          </div>
          <label class="full-field"><span>备注</span><textarea v-model="mappingForm.remark" rows="2"></textarea></label>
        </div>
        <div class="modal-footer"><span></span><div class="footer-actions"><button type="button" class="secondary-button" @click="mappingModalOpen = false">取消</button><button type="button" class="primary-button" @click="submitMapping">保存匹配</button></div></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ApiError } from '@/api/http'
import { productApi, type Product } from '@/api/product'
import { salesApi, type ChannelProductMapping, type SalesChannelConfig } from '@/api/sales'
import { salesGoodsApi, type SalesGoods } from '@/api/salesGoods'

type TabKey = 'goods' | 'mappings'

const activeTab = ref<TabKey>('goods')
const goodsList = ref<SalesGoods[]>([])
const enabledGoods = ref<SalesGoods[]>([])
const products = ref<Product[]>([])
const channels = ref<SalesChannelConfig[]>([])
const mappings = ref<ChannelProductMapping[]>([])
const goodsQuery = ref('')
const mappingFilterChannelId = ref(0)
const goodsModalOpen = ref(false)
const mappingModalOpen = ref(false)
const submitting = ref(false)
const message = ref('')
const goodsPage = reactive({ totalElements: 0, totalPages: 0, size: 20, number: 0 })
const goodsForm = reactive({
  id: null as number | null,
  code: '',
  name: '',
  category: '',
  specification: '',
  unit: '件',
  defaultPrice: 0,
  enabled: true,
  remark: '',
  components: [] as Array<{ productId: number; quantityPerUnit: number; lossRate: number; remark?: string }>,
  channelPrices: [] as Array<{ channelId: number; price: number; enabled: boolean; remark?: string }>
})
const mappingForm = reactive({
  id: null as number | null,
  channelId: 0,
  externalProductName: '',
  externalSpecName: '',
  externalSkuCode: '',
  salesGoodsId: 0,
  quantityMultiplier: 1,
  defaultUnitPrice: null as number | null,
  matchType: 'EXACT' as 'EXACT' | 'CONTAINS',
  enabled: true,
  priority: 100,
  remark: ''
})
let messageTimer: number | undefined

onMounted(async () => {
  await Promise.all([loadProducts(), loadChannels(), loadGoods(true), loadEnabledGoods(), loadMappings()])
})

async function loadProducts() { try { products.value = (await productApi.getAllProducts(0, 500)).content } catch (error) { showMessage(getErrorMessage(error, '加载库存产品失败')) } }
async function loadChannels() { try { channels.value = await salesApi.getChannels(false) } catch (error) { showMessage(getErrorMessage(error, '加载渠道失败')) } }
async function loadEnabledGoods() { try { enabledGoods.value = await salesGoodsApi.getEnabledGoods() } catch (error) { showMessage(getErrorMessage(error, '加载销售商品失败')) } }
async function loadGoods(reset = false) { if (reset) goodsPage.number = 0; try { const result = await salesGoodsApi.getGoods(goodsPage.number, goodsPage.size, goodsQuery.value); goodsList.value = result.content; Object.assign(goodsPage, { totalElements: result.totalElements, totalPages: result.totalPages, size: result.size, number: result.number }) } catch (error) { showMessage(getErrorMessage(error, '加载销售商品失败')) } }
async function loadMappings() { try { mappings.value = await salesApi.getProductMappings(mappingFilterChannelId.value || undefined) } catch (error) { showMessage(getErrorMessage(error, '加载商品匹配失败')) } }

function openGoodsModal(goods?: SalesGoods) {
  Object.assign(goodsForm, { id: null, code: '', name: '', category: '', specification: '', unit: '件', defaultPrice: 0, enabled: true, remark: '', components: [], channelPrices: [] })
  if (goods) {
    Object.assign(goodsForm, {
      id: goods.id,
      code: goods.code,
      name: goods.name,
      category: goods.category || '',
      specification: goods.specification || '',
      unit: goods.unit,
      defaultPrice: goods.defaultPrice,
      enabled: goods.enabled,
      remark: goods.remark || '',
      components: goods.components.map(component => ({ productId: component.productId, quantityPerUnit: component.quantityPerUnit, lossRate: component.lossRate || 0, remark: component.remark || '' })),
      channelPrices: goods.channelPrices.map(price => ({ channelId: price.channelId, price: price.price, enabled: price.enabled, remark: price.remark || '' }))
    })
  } else {
    addComponent()
  }
  goodsModalOpen.value = true
}
function addComponent() { goodsForm.components.push({ productId: 0, quantityPerUnit: 1, lossRate: 0 }) }
function removeComponent(index: number) { goodsForm.components.splice(index, 1) }
function addChannelPrice() { goodsForm.channelPrices.push({ channelId: channels.value[0]?.id || 0, price: goodsForm.defaultPrice || 0, enabled: true }) }
function removeChannelPrice(index: number) { goodsForm.channelPrices.splice(index, 1) }
async function submitGoods() {
  if (!goodsForm.code.trim() || !goodsForm.name.trim() || !goodsForm.unit.trim() || goodsForm.components.some(component => component.productId <= 0 || component.quantityPerUnit <= 0)) {
    showMessage('请填写商品基础信息和库存组成。')
    return
  }
  submitting.value = true
  try {
    const payload = {
      code: goodsForm.code,
      name: goodsForm.name,
      category: goodsForm.category || undefined,
      specification: goodsForm.specification || undefined,
      unit: goodsForm.unit,
      defaultPrice: goodsForm.defaultPrice || 0,
      enabled: goodsForm.enabled,
      remark: goodsForm.remark || undefined,
      components: goodsForm.components.map(component => ({ productId: component.productId, quantityPerUnit: component.quantityPerUnit, lossRate: component.lossRate || 0, remark: component.remark || undefined })),
      channelPrices: goodsForm.channelPrices.filter(price => price.channelId > 0).map(price => ({ channelId: price.channelId, price: price.price || 0, enabled: price.enabled, remark: price.remark || undefined }))
    }
    if (goodsForm.id) await salesGoodsApi.updateGoods(goodsForm.id, payload)
    else await salesGoodsApi.createGoods(payload)
    goodsModalOpen.value = false
    showMessage('销售商品已保存。')
    await Promise.all([loadGoods(), loadEnabledGoods()])
  } catch (error) {
    showMessage(getErrorMessage(error, '保存销售商品失败'))
  } finally {
    submitting.value = false
  }
}

function openMappingModal(mapping?: ChannelProductMapping) {
  Object.assign(mappingForm, { id: null, channelId: mappingFilterChannelId.value || channels.value[0]?.id || 0, externalProductName: '', externalSpecName: '', externalSkuCode: '', salesGoodsId: 0, quantityMultiplier: 1, defaultUnitPrice: null, matchType: 'EXACT', enabled: true, priority: 100, remark: '' })
  if (mapping) Object.assign(mappingForm, { id: mapping.id, channelId: mapping.channelId, externalProductName: mapping.externalProductName, externalSpecName: mapping.externalSpecName || '', externalSkuCode: mapping.externalSkuCode || '', salesGoodsId: mapping.salesGoodsId, quantityMultiplier: mapping.quantityMultiplier, defaultUnitPrice: mapping.defaultUnitPrice ?? null, matchType: mapping.matchType, enabled: mapping.enabled, priority: mapping.priority, remark: mapping.remark || '' })
  mappingModalOpen.value = true
}
async function submitMapping() {
  if (!mappingForm.channelId || !mappingForm.salesGoodsId || !mappingForm.externalProductName.trim()) {
    showMessage('请填写渠道、外部商品和销售商品。')
    return
  }
  try {
    const payload = { channelId: mappingForm.channelId, externalProductName: mappingForm.externalProductName, externalSpecName: mappingForm.externalSpecName || undefined, externalSkuCode: mappingForm.externalSkuCode || undefined, salesGoodsId: mappingForm.salesGoodsId, quantityMultiplier: mappingForm.quantityMultiplier, defaultUnitPrice: mappingForm.defaultUnitPrice ?? undefined, matchType: mappingForm.matchType, enabled: mappingForm.enabled, priority: mappingForm.priority, remark: mappingForm.remark || undefined }
    if (mappingForm.id) await salesApi.updateProductMapping(mappingForm.id, payload)
    else await salesApi.createProductMapping(payload)
    mappingModalOpen.value = false
    showMessage('商品匹配已保存。')
    await loadMappings()
  } catch (error) {
    showMessage(getErrorMessage(error, '保存商品匹配失败'))
  }
}

function formatMoney(value: number) { return `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` }
function showMessage(text: string) { message.value = text; if (messageTimer) window.clearTimeout(messageTimer); messageTimer = window.setTimeout(() => { message.value = '' }, 3500) }
function getErrorMessage(error: unknown, fallback: string) { return error instanceof ApiError ? error.message : fallback }
</script>

<style scoped>
.goods-page{display:flex;flex-direction:column;gap:14px;min-height:calc(100vh - 120px);color:#0f172a}.page-header{display:flex;align-items:center;justify-content:space-between;gap:16px}.page-eyebrow{margin:0 0 5px;color:#64748b;font-size:13px;font-weight:800}.page-header h1{margin:0;color:#0f172a;font-size:22px}.page-header p:last-child{margin:7px 0 0;color:#64748b;font-size:13px;line-height:1.5}.header-actions,.filter-actions,.footer-actions{display:flex;align-items:center;gap:8px;flex-wrap:wrap}.tab-bar{display:flex;gap:8px;padding:6px;border:1px solid #e5edf7;border-radius:14px;background:#fff;box-shadow:0 6px 18px rgba(15,23,42,.04)}.tab-bar button{height:38px;padding:0 16px;border:none;border-radius:10px;background:transparent;color:#64748b;cursor:pointer;font-weight:900}.tab-bar button.active{background:#2563eb;color:#fff;box-shadow:0 8px 18px rgba(37,99,235,.18)}.filter-panel,.card-section{border:1px solid #e5edf7;border-radius:14px;background:#fff;box-shadow:0 6px 18px rgba(15,23,42,.04)}.filter-panel{padding:14px}.toolbar{display:grid;grid-template-columns:minmax(280px,420px) auto;align-items:end;gap:12px}.toolbar label,.form-grid label,.full-field{display:grid;gap:6px;color:#334155;font-size:13px;font-weight:800}.toolbar input,.toolbar select,.modal-body input,.modal-body select,.modal-body textarea,.line-item-row input,.line-item-row select{width:100%;border:1px solid #dbe3ef;border-radius:10px;background:#fff;color:#0f172a;font-size:14px;outline:none}.toolbar input,.toolbar select,.modal-body input,.modal-body select,.line-item-row input,.line-item-row select{height:40px;padding:0 12px}.modal-body textarea{min-height:76px;padding:10px 11px;resize:vertical}.operation-message{margin:0;padding:10px 12px;border:1px solid #bfdbfe;border-radius:10px;background:#eff6ff;color:#1d4ed8;font-size:13px;font-weight:800}.list-header{padding:16px 18px 12px}.list-header h2{margin:0;color:#0f172a;font-size:17px}.list-header p{margin:6px 0 0;color:#64748b;font-size:13px}.table-wrap{margin:0 14px 14px;border:1px solid #edf2f7;border-radius:12px;overflow:auto}.data-table{width:100%;min-width:980px;border-collapse:collapse}.data-table thead{background:#f8fafc}.data-table th,.data-table td{padding:12px 14px;border-bottom:1px solid #edf2f7;color:#334155;font-size:13px;text-align:left;vertical-align:middle}.data-table th{color:#475569;font-weight:800}.strong-text{color:#0f172a;font-weight:800}.muted-text{margin-top:4px;color:#64748b;font-size:12px}.money-cell{color:#0f172a;font-variant-numeric:tabular-nums;font-weight:800}.compact-lines{display:grid;gap:4px}.empty-cell{padding:32px 16px;color:#94a3b8;text-align:center}.primary-button,.secondary-button{display:inline-flex;align-items:center;justify-content:center;min-height:40px;border-radius:10px;cursor:pointer;font-size:14px;font-weight:800;padding:0 15px}.primary-button{border:1px solid #2563eb;background:#2563eb;color:#fff;box-shadow:0 8px 18px rgba(37,99,235,.18)}.secondary-button{border:1px solid #dbe3ef;background:#fff;color:#334155}.compact-button{min-height:34px;padding:0 12px;font-size:13px}.text-button{border:none;border-radius:999px;background:transparent;color:#475569;cursor:pointer;font-size:12px;font-weight:800;padding:6px 9px}.danger-text{color:#dc2626}.modal-backdrop{position:fixed;inset:0;z-index:1000;display:flex;align-items:center;justify-content:center;padding:20px;background:rgba(15,23,42,.45)}.modal-content{width:min(100%,900px);max-height:90vh;overflow:hidden;border-radius:14px;background:#fff;box-shadow:0 24px 60px rgba(15,23,42,.24);display:flex;flex-direction:column}.small-modal{width:min(100%,720px)}.modal-header,.modal-footer{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:16px 18px;border-bottom:1px solid #edf2f7}.modal-footer{border-top:1px solid #edf2f7;border-bottom:none}.modal-header h2{margin:0;color:#0f172a;font-size:18px}.icon-button{display:inline-flex;align-items:center;justify-content:center;width:34px;height:34px;border:none;border-radius:8px;background:#f8fafc;color:#64748b;cursor:pointer;font-size:22px;line-height:1}.modal-body{display:grid;gap:14px;padding:18px;overflow-y:auto}.form-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}.items-header{display:flex;align-items:center;justify-content:space-between;gap:12px}.items-header h3{margin:0;color:#0f172a;font-size:15px}.line-items{display:grid;gap:10px}.line-item-row{display:grid;grid-template-columns:minmax(260px,1fr) 130px 120px auto;align-items:center;gap:10px;padding:10px;border:1px solid #edf2f7;border-radius:12px;background:#f8fafc}.price-row{grid-template-columns:minmax(220px,1fr) 130px 120px auto}@media(max-width:768px){.page-header,.modal-header,.modal-footer{align-items:stretch;flex-direction:column}.toolbar,.form-grid,.line-item-row,.price-row{grid-template-columns:1fr}}
</style>
