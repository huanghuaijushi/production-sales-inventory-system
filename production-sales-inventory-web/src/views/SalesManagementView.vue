<template>
  <div class="sales-page">
    <div class="page-header">
      <div>
        <p class="page-eyebrow">销售管理</p>
        <h1>销售订单与导入中心</h1>
        <p>销售单继续负责锁库和发货；外部订单先进入导入中心，匹配商品后再确认生成正式销售单。</p>
      </div>
      <div class="header-search" v-if="activeTab === 'orders'">
        <input v-model="filters.query" type="search" placeholder="搜索订单号、客户或备注" @keyup.enter="loadOrders(true)" />
      </div>
      <div class="header-actions">
        <button v-if="activeTab === 'orders'" type="button" class="primary-button" @click="openOrderModal()">新增销售单</button>
        <button v-if="activeTab === 'imports'" type="button" class="primary-button" @click="importTextOrder">导入文本订单</button>
        <button v-if="activeTab === 'channels'" type="button" class="primary-button" @click="openChannelModal()">新增渠道</button>
      </div>
    </div>

    <nav class="tab-bar">
      <button type="button" :class="{ active: activeTab === 'orders' }" @click="activeTab = 'orders'">销售单</button>
      <button type="button" :class="{ active: activeTab === 'imports' }" @click="activeTab = 'imports'">订单导入</button>
      <button type="button" :class="{ active: activeTab === 'channels' }" @click="activeTab = 'channels'">渠道设置</button>
    </nav>

    <p v-if="message" class="operation-message">{{ message }}</p>

    <template v-if="activeTab === 'orders'">
      <section class="filter-panel">
        <div class="toolbar">
          <label>
            <span>销售搜索</span>
            <input v-model="filters.query" type="search" placeholder="订单号、客户、电话或备注" @keyup.enter="loadOrders(true)" />
          </label>
          <label>
            <span>状态</span>
            <select v-model="filters.status" @change="loadOrders(true)">
              <option value="all">全部状态</option>
              <option value="PENDING">待发货</option>
              <option value="SHIPPED">已发货</option>
              <option value="COMPLETED">已完成</option>
              <option value="CANCELLED">已取消</option>
            </select>
          </label>
          <div class="filter-actions">
            <button type="button" class="primary-button" @click="loadOrders(true)">查询</button>
            <button type="button" class="secondary-button" @click="resetFilters">重置</button>
          </div>
        </div>
      </section>

      <section class="card-section">
        <div class="list-header">
          <div>
            <h2>销售单列表</h2>
            <p>共 {{ orderPage.totalElements }} 条</p>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>销售单</th>
                <th>客户</th>
                <th>渠道/来源</th>
                <th>明细</th>
                <th>金额</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="ordersLoading"><td colspan="7" class="empty-cell">正在加载销售单...</td></tr>
              <tr v-else-if="orders.length === 0"><td colspan="7" class="empty-cell">暂无销售单</td></tr>
              <tr v-for="order in orders" v-else :key="order.id">
                <td>
                  <div class="strong-text">{{ order.orderNo }}</div>
                  <div class="muted-text">{{ formatDateTime(order.createdAt) }}</div>
                  <div v-if="order.externalOrderNo" class="muted-text">外部单号：{{ order.externalOrderNo }}</div>
                </td>
                <td>
                  <div class="strong-text">{{ order.customerName || '散客' }}</div>
                  <div class="muted-text">{{ order.customerPhone || '-' }}</div>
                </td>
                <td>
                  <div>{{ order.channelText }}</div>
                  <div class="muted-text">{{ sourceTypeText(order.sourceType) }}</div>
                </td>
                <td>
                  <div class="item-summary">
                    <span v-for="item in order.items.slice(0, 2)" :key="item.id">{{ item.salesSkuName }} x {{ item.quantity }}{{ item.salesGoodsUnit }}</span>
                    <span v-if="order.items.length > 2">等 {{ order.items.length }} 项</span>
                  </div>
                </td>
                <td class="money-cell">{{ formatMoney(order.totalAmount) }}</td>
                <td><span class="status-pill" :class="`status-pill--${order.status.toLowerCase()}`">{{ order.statusText }}</span></td>
                <td>
                  <div class="table-actions">
                    <button v-if="order.status === 'PENDING'" type="button" class="text-button" @click="openOrderModal(order)">编辑</button>
                    <button v-if="order.status === 'PENDING'" type="button" class="text-button primary-text" @click="shipOrder(order)">发货出库</button>
                    <button v-if="order.status === 'SHIPPED'" type="button" class="text-button primary-text" @click="completeOrder(order)">完成</button>
                    <button v-if="order.status === 'PENDING' || order.status === 'SHIPPED'" type="button" class="text-button danger-text" @click="cancelOrder(order)">取消</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination-bar">
          <span>共 {{ orderPage.totalElements }} 条销售单</span>
          <div class="pagination-actions">
            <button type="button" :disabled="orderPage.number <= 0" @click="changeOrderPage(orderPage.number - 1)">上一页</button>
            <span>{{ orderPage.number + 1 }} / {{ Math.max(orderPage.totalPages, 1) }}</span>
            <button type="button" :disabled="orderPage.number + 1 >= orderPage.totalPages" @click="changeOrderPage(orderPage.number + 1)">下一页</button>
          </div>
        </div>
      </section>
    </template>

    <template v-else-if="activeTab === 'imports'">
      <section class="import-layout">
        <div class="card-section import-input-card">
          <div class="list-header">
            <div>
              <h2>粘贴文本订单</h2>
              <p>支持微信、短信、客服聊天、表格展平文本等多种来源。系统会自动识别手机号、地址和商品行。</p>
            </div>
          </div>
          <div class="import-form">
            <label>
              <span>导入渠道</span>
              <select v-model.number="textImportForm.channelId">
                <option :value="0" disabled>{{ importChannelOptions.length ? '选择文本来源渠道' : '暂无文本渠道，请先到渠道设置新增来源类型为文本的渠道' }}</option>
                <option v-for="channel in importChannelOptions" :key="channel.id" :value="channel.id">{{ channel.name }} · {{ channel.code }} · {{ sourceTypeText(channel.sourceType) }}</option>
              </select>
            </label>
            <label>
              <span>订单文本</span>
              <textarea v-model="textImportForm.rawText" rows="12" placeholder="收货人：赵敬仪&#10;手机号码：15783213209&#10;收货地址：浙江省宁波市慈溪市龙山镇龙镇大道75号永乐公寓&#10;商品明细：蛋黄鲜肉粽 × 6个&#10;&#10;姓名:赵敬仪 | 电话:15783213209 | 地址:浙江省宁波市慈溪市龙山镇龙镇大道75号永乐公寓[4160] | 品名:蛋黄鲜肉粽 | 数量:6个"></textarea>
            </label>
            <div class="filter-actions">
              <button type="button" class="primary-button" :disabled="importSubmitting" @click="importTextOrder">{{ importSubmitting ? '识别中...' : '识别并预览' }}</button>
              <button type="button" class="secondary-button" @click="loadImportBatches(true)">刷新批次</button>
            </div>
          </div>
        </div>

        <div class="card-section">
          <div class="list-header">
            <div>
              <h2>导入批次</h2>
              <p>选择批次查看解析和商品匹配结果。</p>
            </div>
          </div>
          <div class="batch-list">
            <button v-for="batch in importBatches" :key="batch.id" type="button" class="batch-card" :class="{ active: selectedBatch?.id === batch.id }" @click="selectBatch(batch.id)">
              <div>
                <strong>{{ batch.batchNo }}</strong>
                <span>{{ batch.channelName }} · {{ sourceTypeText(batch.sourceType) }}</span>
              </div>
              <div class="batch-stats">
                <span>总 {{ batch.totalCount }}</span>
                <span>可确认 {{ batch.readyCount }}</span>
                <span>已转 {{ batch.convertedCount }}</span>
              </div>
              <em>{{ importStatusText(batch.status) }}</em>
            </button>
            <div v-if="importBatches.length === 0" class="empty-cell">暂无导入批次</div>
          </div>
        </div>
      </section>

      <section v-if="selectedBatch" class="card-section">
        <div class="list-header">
          <div>
            <h2>导入预览：{{ selectedBatch.batchNo }} <span class="match-progress">已匹配 {{ importPreviewStats.matched }} / {{ importPreviewStats.total }}</span></h2>
            <p>未匹配商品会标红，点击「快速匹配」创建映射，支持一键新建销售商品。已匹配的商品可点击「更改」修正。</p>
          </div>
          <div class="filter-actions">
            <button type="button" class="primary-button" :disabled="confirmSubmitting || selectedBatch.readyCount <= 0" @click="confirmSelectedBatch">{{ confirmSubmitting ? '生成中...' : '确认生成销售单' }}</button>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table import-preview-table">
            <thead>
              <tr>
                <th>外部订单</th>
                <th>客户</th>
                <th>商品匹配</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in selectedBatch.orders || []" :key="order.id">
                <td>
                  <div class="strong-text">{{ order.externalOrderNo }}</div>
                  <div class="muted-text">{{ formatDateTime(order.createdAt) }}</div>
                </td>
                <td>
                  <template v-if="editingExternalOrderId === order.id">
                    <div class="inline-edit-grid">
                      <input v-model="externalOrderForm.customerName" type="text" placeholder="客户名" />
                      <input v-model="externalOrderForm.customerPhone" type="text" placeholder="手机号" />
                      <textarea v-model="externalOrderForm.customerAddress" rows="2" placeholder="收货地址"></textarea>
                    </div>
                  </template>
                  <template v-else>
                    <div class="strong-text">{{ order.customerName || '未填写' }}</div>
                    <div class="muted-text">{{ order.customerPhone || '-' }}</div>
                    <div class="muted-text address-text">{{ order.customerAddress || '-' }}</div>
                  </template>
                </td>
                <td>
                  <div v-if="editingExternalOrderId === order.id" class="raw-items editable-raw-items">
                    <div v-for="item in externalOrderForm.items" :key="item.id" class="raw-item edit-raw-item">
                      <input v-model="item.externalProductName" type="text" placeholder="外部商品" />
                      <input v-model="item.externalSpecName" type="text" placeholder="外部规格，如 6个装" />
                      <input v-model.number="item.externalQuantity" type="number" min="0.0001" step="0.0001" placeholder="外部数量" />
                      <input v-model.number="item.externalUnitPrice" type="number" min="0" step="0.01" placeholder="成交价" />
                    </div>
                  </div>
                  <div v-else class="raw-items">
                    <div v-for="item in order.items" :key="item.id" class="raw-item" :class="{ danger: item.matchStatus !== 'MATCHED' }">
                      <div>
                        <strong>{{ item.externalProductName }}</strong>
                        <span>规格 {{ item.externalSpecName || '-' }} · 数量 {{ item.externalQuantity }} · 订单价 {{ (item.externalUnitPrice && item.externalUnitPrice > 0) ? formatMoney(item.externalUnitPrice) : '--' }}</span>
                      </div>
                      <div>
                        <span v-if="item.matchStatus === 'MATCHED'">
                          匹配：{{ item.matchedSkuName }} x {{ item.saleQuantity }}
                          · 预估单价 {{ formatMoney(item.resolvedUnitPrice || 0) }}
                          <template v-if="item.priceSource === 'SKU_PRICE'">（SKU定价）</template>
                          <template v-else-if="item.priceSource === 'IMPORTED'">（订单价）</template>
                        </span>
                        <span v-else>{{ item.matchMessage || '未匹配' }}</span>
                      </div>
                      <button v-if="item.matchStatus === 'MATCHED'" type="button" class="text-button" @click="openQuickMatch(order, item)">更改</button>
                    <button v-else type="button" class="text-button primary-text" @click="openQuickMatch(order, item)">快速匹配</button>
                    </div>
                  </div>
                </td>
                <td>
                  <div class="table-actions">
                    <span class="status-pill" :class="`external-status--${order.status.toLowerCase()}`">{{ externalStatusText(order.status) }}</span>
                    <button v-if="editingExternalOrderId !== order.id && order.status !== 'CONVERTED'" type="button" class="text-button" @click="startEditExternalOrder(order)">编辑</button>
                    <button v-if="editingExternalOrderId === order.id" type="button" class="text-button primary-text" :disabled="savingExternalOrder" @click="saveExternalOrder(order)">保存并重匹配</button>
                    <button v-if="editingExternalOrderId === order.id" type="button" class="text-button" @click="cancelEditExternalOrder">取消</button>
                  </div>
                </td>
              </tr>
              <tr v-if="(selectedBatch.orders || []).length === 0"><td colspan="4" class="empty-cell">该批次暂无订单</td></tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>

    <template v-else>
      <section class="card-section">
        <div class="list-header"><div><h2>渠道设置</h2><p>管理销售来源渠道，导入中心按渠道应用商品映射规则。</p></div></div>
        <div class="table-wrap">
          <table class="data-table">
            <thead><tr><th>编码</th><th>名称</th><th>来源类型</th><th>排序</th><th>状态</th><th>备注</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="channel in channels" :key="channel.id">
                <td>{{ channel.code }}</td><td>{{ channel.name }}</td><td>{{ sourceTypeText(channel.sourceType) }}</td><td>{{ channel.sortOrder }}</td><td>{{ channel.enabled ? '启用' : '停用' }}</td><td>{{ channel.remark || '-' }}</td>
                <td><button type="button" class="text-button" @click="openChannelModal(channel)">编辑</button></td>
              </tr>
              <tr v-if="channels.length === 0"><td colspan="7" class="empty-cell">暂无渠道配置</td></tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>

    <div v-if="orderModalOpen" class="modal-backdrop">
      <div class="modal-content purchase-modal">
        <div class="modal-header"><h2>{{ orderForm.id ? '编辑销售单' : '新增销售单' }}</h2><button type="button" class="icon-button" @click="closeOrderModal">×</button></div>
        <div class="modal-body">
          <div class="form-grid">
            <label><span>销售渠道</span><select v-model="orderForm.channel"><option value="OFFLINE">线下</option><option value="DOUYIN">抖音</option><option value="PINDUODUO">拼多多</option><option value="WECHAT_GROUP">微信群</option><option value="CONTRACT">合同</option></select></label>
            <label><span>客户名称</span><input v-model="orderForm.customerName" type="text" placeholder="散客可不填" /></label>
            <label><span>客户电话</span><input v-model="orderForm.customerPhone" type="text" placeholder="联系电话" /></label>
            <label><span>收货地址</span><input v-model="orderForm.customerAddress" type="text" placeholder="线下自提可不填" /></label>
          </div>
          <label class="full-field"><span>备注</span><textarea v-model="orderForm.remark" rows="2" placeholder="可填写销售说明"></textarea></label>
          <div class="items-header"><h3>销售明细</h3><button type="button" class="secondary-button compact-button" @click="addItem()">添加销售商品</button></div>
          <div class="purchase-items">
            <div v-for="(item, index) in orderForm.items" :key="index" class="purchase-item-row">
              <select v-model.number="item.salesSkuId" @change="syncSkuPrice(item)"><option :value="0" disabled>选择销售SKU</option><option v-for="sku in allSkus" :key="sku.id" :value="sku.id">{{ sku.code }} · {{ sku.name }} · {{ sku.specName || sku.unit }}</option></select>
              <input v-model.number="item.quantity" type="number" min="1" step="1" placeholder="数量" />
              <input v-model.number="item.unitPrice" type="number" min="0" step="0.01" placeholder="单价" />
              <strong>{{ formatMoney((item.quantity || 0) * (item.unitPrice || 0)) }}</strong>
              <button type="button" class="text-button danger-text" @click="removeItem(index)">删除</button>
            </div>
          </div>
        </div>
        <div class="modal-footer"><span>合计 {{ formatMoney(formTotalAmount) }}</span><div class="footer-actions"><button type="button" class="secondary-button" @click="closeOrderModal">取消</button><button type="button" class="primary-button" :disabled="submitting" @click="submitOrder">{{ submitting ? '保存中...' : '保存销售单并锁库' }}</button></div></div>
      </div>
    </div>

    <div v-if="channelModalOpen" class="modal-backdrop">
      <div class="modal-content small-modal">
        <div class="modal-header"><h2>{{ channelForm.id ? '编辑渠道' : '新增渠道' }}</h2><button type="button" class="icon-button" @click="channelModalOpen = false">×</button></div>
        <div class="modal-body">
          <div class="form-grid">
            <label><span>编码</span><input v-model="channelForm.code" type="text" placeholder="TEXT_RETAIL" /></label>
            <label><span>名称</span><input v-model="channelForm.name" type="text" placeholder="零售文本订单" /></label>
            <label><span>来源类型</span><select v-model="channelForm.sourceType"><option value="TEXT">文本</option><option value="EXCEL">Excel</option><option value="MANUAL">手工</option><option value="CONTRACT">合同</option><option value="API">API</option></select></label>
            <label><span>排序</span><input v-model.number="channelForm.sortOrder" type="number" /></label>
            <label><span>状态</span><select v-model="channelForm.enabled"><option :value="true">启用</option><option :value="false">停用</option></select></label>
          </div>
          <label class="full-field"><span>备注</span><textarea v-model="channelForm.remark" rows="2"></textarea></label>
        </div>
        <div class="modal-footer"><span></span><div class="footer-actions"><button type="button" class="secondary-button" @click="channelModalOpen = false">取消</button><button type="button" class="primary-button" @click="submitChannel">保存渠道</button></div></div>
      </div>
    </div>

    <div v-if="quickMatchModalOpen" class="modal-backdrop">
      <div class="modal-content small-modal">
        <div class="modal-header"><h2>{{ quickMatchForm.mappingId > 0 ? '更改匹配' : '快速匹配' }}</h2><button type="button" class="icon-button" @click="closeQuickMatch">×</button></div>
        <div class="modal-body">
          <div class="quick-match-info">订单商品：<strong>{{ quickMatchForm.externalProductName }}</strong><span v-if="quickMatchForm.externalSpecName">（{{ quickMatchForm.externalSpecName }}）</span></div>
          <div class="quick-match-preview">订单数量 {{ quickMatchForm.externalQuantity }}</div>
          <div class="quick-match-row">
            <label class="quick-match-label"><span>选择我方SKU</span>
              <select v-model.number="quickMatchForm.salesSkuId"><option :value="0" disabled>选择销售SKU</option><option v-for="sku in allSkus" :key="sku.id" :value="sku.id">{{ sku.name }} · {{ sku.code }}</option></select>
            </label>
            <button type="button" class="text-button primary-text" @click="quickMatchShowCreate = !quickMatchShowCreate">{{ quickMatchShowCreate ? '收起' : '新建商品' }}</button>
          </div>
          <div v-if="quickMatchShowCreate" class="quick-match-create">
            <div class="form-grid">
              <label><span>商品名称</span><input v-model="quickMatchCreateForm.name" type="text" placeholder="蛋黄鲜肉粽（非礼盒）" /></label>
              <label><span>单位</span><input v-model="quickMatchCreateForm.unit" type="text" placeholder="个" /></label>
              <label><span>默认售价</span><input v-model.number="quickMatchCreateForm.defaultPrice" type="number" min="0" step="0.01" /></label>
            </div>
            <button type="button" class="secondary-button compact-button" :disabled="quickMatchCreating" @click="submitQuickCreateGoods">{{ quickMatchCreating ? '创建中...' : '创建商品并匹配' }}</button>
          </div>
        </div>
        <div class="modal-footer"><span>创建后自动重新匹配</span><div class="footer-actions"><button type="button" class="secondary-button" @click="closeQuickMatch">取消</button><button type="button" class="primary-button" :disabled="quickMatchSubmitting" @click="submitQuickMatch">{{ quickMatchSubmitting ? '提交中...' : (quickMatchForm.mappingId > 0 ? '更新匹配并刷新' : '创建匹配并刷新') }}</button></div></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ApiError } from '@/api/http'
import { inventoryApi, type StockItem } from '@/api/inventory'
import { salesApi, type ExternalOrderItemRaw, type OrderImportBatch, type SalesChannelConfig, type SalesOrder } from '@/api/sales'
import { salesGoodsApi, type SalesGoods, type SalesSku } from '@/api/salesGoods'

interface OrderFormItem { salesSkuId: number; quantity: number; unitPrice: number }
type TabKey = 'orders' | 'imports' | 'channels'

const activeTab = ref<TabKey>('orders')
const orders = ref<SalesOrder[]>([])
const salesGoodsList = ref<SalesGoods[]>([])
const stocks = ref<StockItem[]>([])
const channels = ref<SalesChannelConfig[]>([])
const importBatches = ref<OrderImportBatch[]>([])
const selectedBatch = ref<OrderImportBatch | null>(null)
const ordersLoading = ref(false)
const submitting = ref(false)
const importSubmitting = ref(false)
const confirmSubmitting = ref(false)
const editingExternalOrderId = ref<number | null>(null)
const savingExternalOrder = ref(false)
const orderModalOpen = ref(false)
const channelModalOpen = ref(false)
const quickMatchModalOpen = ref(false)
const quickMatchSubmitting = ref(false)
const quickMatchShowCreate = ref(false)
const quickMatchCreating = ref(false)
const message = ref('')
const filters = reactive({ query: '', status: 'all' })
const orderPage = reactive({ totalElements: 0, totalPages: 0, size: 10, number: 0 })
const importPage = reactive({ totalElements: 0, totalPages: 0, size: 10, number: 0 })
const textImportForm = reactive({ channelId: 0, rawText: '' })
const orderForm = reactive({ id: null as number | null, channel: 'OFFLINE' as 'DOUYIN' | 'PINDUODUO' | 'OFFLINE' | 'WECHAT_GROUP' | 'CONTRACT', customerName: '', customerPhone: '', customerAddress: '', remark: '', items: [] as OrderFormItem[] })
const channelForm = reactive({ id: null as number | null, code: '', name: '', sourceType: 'TEXT' as 'EXCEL' | 'TEXT' | 'MANUAL' | 'CONTRACT' | 'API', enabled: true, sortOrder: 0, configJson: '', remark: '' })
const externalOrderForm = reactive({ customerName: '', customerPhone: '', customerAddress: '', buyerMessage: '', sellerRemark: '', items: [] as Array<{ id: number; externalProductName: string; externalSpecName: string; externalSkuCode: string; externalQuantity: number; externalUnitPrice: number }> })
const quickMatchForm = reactive({ channelId: 0, externalProductName: '', externalSpecName: '', externalQuantity: 1, salesSkuId: 0, matchType: 'CONTAINS' as 'EXACT' | 'CONTAINS', mappingId: 0 })
const quickMatchCreateForm = reactive({ name: '', unit: '个', defaultPrice: 0 })
let messageTimer: number | undefined

const formTotalAmount = computed(() => orderForm.items.reduce((sum, item) => sum + (Number(item.quantity) || 0) * (Number(item.unitPrice) || 0), 0))
const importChannelOptions = computed(() => channels.value.filter(channel => channel.enabled && channel.sourceType === 'TEXT'))
const allSkus = computed(() => salesGoodsList.value.flatMap(goods => goods.skus || []))
const importPreviewStats = computed(() => {
  if (!selectedBatch.value?.orders) return { matched: 0, total: 0 }
  let matched = 0; let total = 0
  for (const order of selectedBatch.value.orders) {
    for (const item of order.items || []) {
      total++
      if (item.matchStatus === 'MATCHED') matched++
    }
  }
  return { matched, total }
})

onMounted(async () => {
  await Promise.all([loadSalesGoods(), loadStocks(), loadChannels(), loadOrders(true), loadImportBatches(true)])
  const defaultTextChannel = importChannelOptions.value[0]
  if (defaultTextChannel) textImportForm.channelId = defaultTextChannel.id
})

async function loadOrders(reset = false) { if (reset) orderPage.number = 0; ordersLoading.value = true; try { const result = await salesApi.getOrders(orderPage.number, orderPage.size, filters.query, filters.status); orders.value = result.content; Object.assign(orderPage, { totalElements: result.totalElements, totalPages: result.totalPages, size: result.size, number: result.number }) } catch (error) { showMessage(getErrorMessage(error, '加载销售单失败')) } finally { ordersLoading.value = false } }
async function loadSalesGoods() { try { salesGoodsList.value = await salesGoodsApi.getEnabledGoods() } catch (error) { showMessage(getErrorMessage(error, '加载销售商品失败')) } }
async function loadStocks() { try { stocks.value = (await inventoryApi.getAllStocks(0, 300)).content } catch (error) { showMessage(getErrorMessage(error, '加载库存失败')) } }
async function loadChannels() { try { channels.value = await salesApi.getChannels(false) } catch (error) { showMessage(getErrorMessage(error, '加载渠道失败')) } }
async function loadImportBatches(reset = false) { if (reset) importPage.number = 0; try { const result = await salesApi.getImportBatches(importPage.number, importPage.size); importBatches.value = result.content; Object.assign(importPage, { totalElements: result.totalElements, totalPages: result.totalPages, size: result.size, number: result.number }) } catch (error) { showMessage(getErrorMessage(error, '加载导入批次失败')) } }
async function selectBatch(batchId: number) { try { selectedBatch.value = await salesApi.getImportBatch(batchId) } catch (error) { showMessage(getErrorMessage(error, '加载导入预览失败')) } }
async function importTextOrder() { if (!textImportForm.channelId && importChannelOptions.value[0]) textImportForm.channelId = importChannelOptions.value[0].id; if (!textImportForm.channelId) { showMessage('没有可用文本渠道，请先到“渠道设置”新增一个来源类型为“文本”的渠道。'); return } if (!textImportForm.rawText.trim()) { showMessage('请先粘贴收货和商品信息。'); return } importSubmitting.value = true; try { selectedBatch.value = await salesApi.importText({ channelId: textImportForm.channelId, rawText: textImportForm.rawText }); showMessage('文本订单已识别，请在下方查看预览结果。'); await loadImportBatches(true) } catch (error) { showMessage(getErrorMessage(error, '文本订单识别失败')) } finally { importSubmitting.value = false } }
async function rematchSelectedBatch() { if (!selectedBatch.value) return; try { selectedBatch.value = await salesApi.parseImportBatch(selectedBatch.value.id); const stats = importPreviewStats.value; showMessage(`已重新匹配：${stats.matched} 条成功 / ${stats.total} 条总计`); await loadImportBatches(true) } catch (error) { showMessage(getErrorMessage(error, '重新匹配失败')) } }
async function confirmSelectedBatch() { if (!selectedBatch.value || !window.confirm('确认将可确认订单生成正式销售单？')) return; confirmSubmitting.value = true; try { selectedBatch.value = await salesApi.confirmImportBatch(selectedBatch.value.id); showMessage('已生成正式销售单。'); await Promise.all([loadImportBatches(true), loadOrders(true), loadStocks()]) } catch (error) { showMessage(getErrorMessage(error, '确认生成销售单失败')) } finally { confirmSubmitting.value = false } }

function startEditExternalOrder(order: { id: number; customerName?: string | undefined; customerPhone?: string | undefined; customerAddress?: string | undefined; buyerMessage?: string | undefined; sellerRemark?: string | undefined; items: ExternalOrderItemRaw[] }) { editingExternalOrderId.value = order.id; externalOrderForm.customerName = order.customerName || ''; externalOrderForm.customerPhone = order.customerPhone || ''; externalOrderForm.customerAddress = order.customerAddress || ''; externalOrderForm.buyerMessage = order.buyerMessage || ''; externalOrderForm.sellerRemark = order.sellerRemark || ''; externalOrderForm.items = order.items.map(item => ({ id: item.id, externalProductName: item.externalProductName, externalSpecName: item.externalSpecName || '', externalSkuCode: item.externalSkuCode || '', externalQuantity: item.externalQuantity, externalUnitPrice: item.externalUnitPrice })) }
function cancelEditExternalOrder() { editingExternalOrderId.value = null; externalOrderForm.customerName = ''; externalOrderForm.customerPhone = ''; externalOrderForm.customerAddress = ''; externalOrderForm.buyerMessage = ''; externalOrderForm.sellerRemark = ''; externalOrderForm.items = [] }
async function saveExternalOrder(order: { id: number }) { if (!selectedBatch.value) return; savingExternalOrder.value = true; try { selectedBatch.value = await salesApi.updateExternalOrder(selectedBatch.value.id, order.id, { customerName: externalOrderForm.customerName || undefined, customerPhone: externalOrderForm.customerPhone || undefined, customerAddress: externalOrderForm.customerAddress || undefined, buyerMessage: externalOrderForm.buyerMessage || undefined, sellerRemark: externalOrderForm.sellerRemark || undefined, items: externalOrderForm.items.map(item => ({ id: item.id, externalProductName: item.externalProductName, externalSpecName: item.externalSpecName || undefined, externalSkuCode: item.externalSkuCode || undefined, externalQuantity: item.externalQuantity, externalUnitPrice: item.externalUnitPrice })) }); showMessage('预览订单已保存并重新匹配。'); cancelEditExternalOrder(); await loadImportBatches(true) } catch (error) { showMessage(getErrorMessage(error, '保存预览订单失败')) } finally { savingExternalOrder.value = false } }

function openOrderModal(order?: SalesOrder) { resetForm(); if (order) { orderForm.id = order.id; orderForm.channel = order.channel; orderForm.customerName = order.customerName || ''; orderForm.customerPhone = order.customerPhone || ''; orderForm.customerAddress = order.customerAddress || ''; orderForm.remark = order.remark || ''; orderForm.items = order.items.map(item => ({ salesSkuId: item.salesSkuId, quantity: item.quantity, unitPrice: item.unitPrice })) } else addItem(); orderModalOpen.value = true }
function closeOrderModal() { orderModalOpen.value = false }
function resetForm() { orderForm.id = null; orderForm.channel = 'OFFLINE'; orderForm.customerName = ''; orderForm.customerPhone = ''; orderForm.customerAddress = ''; orderForm.remark = ''; orderForm.items = [] }
function addItem() { orderForm.items.push({ salesSkuId: 0, quantity: 1, unitPrice: 0 }) }
function removeItem(index: number) { orderForm.items.splice(index, 1) }
function syncSkuPrice(item: OrderFormItem) { const sku = allSkus.value.find(candidate => candidate.id === item.salesSkuId); item.unitPrice = sku?.perSkuPrice || 0 }
async function submitOrder() { if (orderForm.items.length === 0 || orderForm.items.some(item => item.salesSkuId <= 0 || item.quantity <= 0 || item.unitPrice < 0)) { showMessage('请填写完整的销售明细。'); return } submitting.value = true; try { const payload = { channel: orderForm.channel, customerName: orderForm.customerName || undefined, customerPhone: orderForm.customerPhone || undefined, customerAddress: orderForm.customerAddress || undefined, remark: orderForm.remark || undefined, items: orderForm.items.map(item => ({ salesSkuId: item.salesSkuId, quantity: item.quantity, unitPrice: item.unitPrice })) }; if (orderForm.id) { await salesApi.updateOrder(orderForm.id, payload); showMessage('销售单已更新并重新锁库。') } else { await salesApi.createOrder(payload); showMessage('销售单已创建并锁定库存。') } closeOrderModal(); await Promise.all([loadOrders(), loadStocks()]) } catch (error) { showMessage(getErrorMessage(error, '保存销售单失败')) } finally { submitting.value = false } }
async function shipOrder(order: SalesOrder) { if (!window.confirm(`确认发货出库销售单 ${order.orderNo}？系统会自动扣减成品批次库存。`)) return; try { await salesApi.shipOrder(order.id); showMessage('销售单已发货出库。'); await Promise.all([loadOrders(), loadStocks()]) } catch (error) { showMessage(getErrorMessage(error, '发货出库失败')) } }
async function completeOrder(order: SalesOrder) { try { await salesApi.completeOrder(order.id); showMessage('销售单已完成。'); await loadOrders() } catch (error) { showMessage(getErrorMessage(error, '完成销售单失败')) } }
async function cancelOrder(order: SalesOrder) { if (!window.confirm(`确认取消销售单 ${order.orderNo}？待发货订单会释放锁定库存。`)) return; try { await salesApi.cancelOrder(order.id); showMessage('销售单已取消。'); await Promise.all([loadOrders(), loadStocks()]) } catch (error) { showMessage(getErrorMessage(error, '取消销售单失败')) } }

function openChannelModal(channel?: SalesChannelConfig) { Object.assign(channelForm, { id: null, code: '', name: '', sourceType: 'TEXT', enabled: true, sortOrder: 0, configJson: '', remark: '' }); if (channel) Object.assign(channelForm, { id: channel.id, code: channel.code, name: channel.name, sourceType: channel.sourceType, enabled: channel.enabled, sortOrder: channel.sortOrder, configJson: channel.configJson || '', remark: channel.remark || '' }); channelModalOpen.value = true }
async function submitChannel() { if (!channelForm.code.trim() || !channelForm.name.trim()) { showMessage('请填写渠道编码和名称。'); return } try { const payload = { code: channelForm.code, name: channelForm.name, sourceType: channelForm.sourceType, enabled: channelForm.enabled, sortOrder: channelForm.sortOrder, configJson: channelForm.configJson || undefined, remark: channelForm.remark || undefined }; if (channelForm.id) await salesApi.updateChannel(channelForm.id, payload); else await salesApi.createChannel(payload); channelModalOpen.value = false; showMessage('渠道已保存。'); await loadChannels() } catch (error) { showMessage(getErrorMessage(error, '保存渠道失败')) } }

function openQuickMatch(order: { channelId: number; channelName?: string }, item: ExternalOrderItemRaw) {
  const isEdit = item.matchStatus === 'MATCHED'
  quickMatchForm.channelId = order.channelId
  quickMatchForm.externalProductName = item.externalProductName
  quickMatchForm.externalSpecName = item.externalSpecName || ''
  quickMatchForm.externalQuantity = item.externalQuantity
  quickMatchForm.salesSkuId = isEdit ? (item.matchedSalesSkuId || 0) : 0
  quickMatchForm.matchType = 'CONTAINS'
  quickMatchForm.mappingId = isEdit ? (item.mappingId || 0) : 0
  quickMatchShowCreate.value = false
  quickMatchCreateForm.name = item.externalProductName
  quickMatchCreateForm.unit = '个'
  quickMatchCreateForm.defaultPrice = 0
  quickMatchModalOpen.value = true
}
function closeQuickMatch() { quickMatchModalOpen.value = false; quickMatchShowCreate.value = false }
async function submitQuickMatch() {
  if (!quickMatchForm.salesSkuId || quickMatchForm.salesSkuId <= 0) { showMessage('请选择销售SKU。'); return }
  quickMatchSubmitting.value = true
  try {
    const payload = {
      channelId: quickMatchForm.channelId,
      externalProductName: quickMatchForm.externalProductName,
      externalSpecName: quickMatchForm.externalSpecName || undefined,
      salesSkuId: quickMatchForm.salesSkuId,
      matchType: quickMatchForm.matchType,
      enabled: true
    }
    if (quickMatchForm.mappingId > 0) {
      await salesApi.updateProductMapping(quickMatchForm.mappingId, payload)
    } else {
      await salesApi.createProductMapping(payload)
    }
    showMessage('匹配规则已更新，正在重新匹配...')
    quickMatchModalOpen.value = false
    await rematchSelectedBatch()
  } catch (error) {
    showMessage(getErrorMessage(error, '更新匹配规则失败'))
  } finally {
    quickMatchSubmitting.value = false
  }
}

async function submitQuickCreateGoods() {
  if (!quickMatchCreateForm.name.trim()) { showMessage('请填写商品名称。'); return }
  quickMatchCreating.value = true
  try {
    const code = 'KJ' + Date.now().toString(36).toUpperCase()
    const created = await salesGoodsApi.createGoods({
      code,
      name: quickMatchCreateForm.name.trim(),
      unit: quickMatchCreateForm.unit.trim() || '个',
      defaultPrice: quickMatchCreateForm.defaultPrice || 0,
      enabled: true,
      components: [],
      channelPrices: []
    })
    const sku = await salesGoodsApi.createSku(created.id, {
      code: code + '-1',
      name: quickMatchCreateForm.name.trim(),
      specName: undefined,
      unit: quickMatchCreateForm.unit.trim() || '个',
      perSkuPrice: quickMatchCreateForm.defaultPrice || 0,
      enabled: true,
      salesGoodsId: created.id,
      components: []
    })
    showMessage('商品已创建。')
    await loadSalesGoods()
    quickMatchForm.salesSkuId = sku.id
    quickMatchShowCreate.value = false
  } catch (error) {
    showMessage(getErrorMessage(error, '创建商品失败'))
  } finally {
    quickMatchCreating.value = false
  }
}

function resetFilters() { filters.query = ''; filters.status = 'all'; loadOrders(true) }
function changeOrderPage(page: number) { orderPage.number = page; loadOrders() }
function formatMoney(value: number) { return `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` }
function formatDateTime(value?: string) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-' }
function sourceTypeText(value?: string) { return ({ EXCEL: 'Excel', TEXT: '文本', MANUAL: '手工', CONTRACT: '合同', API: 'API' } as Record<string, string>)[value || ''] || '-' }
function importStatusText(value: string) { return ({ DRAFT: '草稿', PARSED: '已解析', CONFIRMED: '已确认', CANCELLED: '已取消' } as Record<string, string>)[value] || value }
function externalStatusText(value: string) { return ({ WAIT_MATCH: '待匹配', READY: '可确认', ERROR: '异常', CONVERTED: '已转单', SKIPPED: '已跳过' } as Record<string, string>)[value] || value }
function showMessage(text: string) { message.value = text; if (messageTimer) window.clearTimeout(messageTimer); messageTimer = window.setTimeout(() => { message.value = '' }, 3500) }
function getErrorMessage(error: unknown, fallback: string) { return error instanceof ApiError ? error.message : fallback }
</script>

<style scoped>
.sales-page{display:flex;flex-direction:column;gap:14px;min-height:calc(100vh - 120px);color:#0f172a}.page-header{display:grid;grid-template-columns:minmax(0,1fr) minmax(280px,420px) auto;align-items:center;gap:16px}.page-eyebrow{margin:0 0 5px;color:#64748b;font-size:13px;font-weight:800}.page-header h1{margin:0;color:#0f172a;font-size:22px}.page-header p:last-child{margin:7px 0 0;color:#64748b;font-size:13px;line-height:1.5}.header-search input{width:100%;height:40px;padding:0 14px;border:1px solid #dbe3ef;border-radius:999px;background:#fff;color:#0f172a;font-size:13px;outline:none}.header-actions,.filter-actions,.table-actions,.pagination-actions,.footer-actions{display:flex;align-items:center;gap:8px;flex-wrap:wrap}.tab-bar{display:flex;gap:8px;padding:6px;border:1px solid #e5edf7;border-radius:14px;background:#fff;box-shadow:0 6px 18px rgba(15,23,42,.04)}.tab-bar button{height:38px;padding:0 16px;border:none;border-radius:10px;background:transparent;color:#64748b;cursor:pointer;font-weight:900}.tab-bar button.active{background:#2563eb;color:#fff;box-shadow:0 8px 18px rgba(37,99,235,.18)}.filter-panel,.card-section{border:1px solid #e5edf7;border-radius:14px;background:#fff;box-shadow:0 6px 18px rgba(15,23,42,.04)}.filter-panel{padding:14px}.toolbar{display:grid;grid-template-columns:minmax(280px,1fr) minmax(150px,190px) auto;align-items:end;gap:12px}.toolbar.two-cols{grid-template-columns:minmax(240px,340px) auto}.toolbar label,.form-grid label,.full-field,.import-form label{display:grid;gap:6px;color:#334155;font-size:13px;font-weight:800}.toolbar input,.toolbar select,.modal-body input,.modal-body select,.modal-body textarea,.purchase-item-row input,.purchase-item-row select,.import-form textarea,.import-form select{width:100%;border:1px solid #dbe3ef;border-radius:10px;background:#fff;color:#0f172a;font-size:14px;outline:none}.toolbar input,.toolbar select,.modal-body input,.modal-body select,.import-form select{height:40px;padding:0 12px}.modal-body textarea,.import-form textarea{min-height:76px;padding:10px 11px;resize:vertical}.operation-message{margin:0;padding:10px 12px;border:1px solid #bfdbfe;border-radius:10px;background:#eff6ff;color:#1d4ed8;font-size:13px;font-weight:800}.list-header{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:16px 18px 12px}.list-header h2{margin:0;color:#0f172a;font-size:17px}.list-header p{margin:6px 0 0;color:#64748b;font-size:13px}.table-wrap{margin:0 14px 14px;border:1px solid #edf2f7;border-radius:12px;overflow:auto}.data-table{width:100%;min-width:980px;border-collapse:collapse}.data-table thead{background:#f8fafc}.data-table th,.data-table td{padding:12px 14px;border-bottom:1px solid #edf2f7;color:#334155;font-size:13px;text-align:left;vertical-align:middle}.data-table th{color:#475569;font-weight:800}.data-table tbody tr:hover{background:#f8fbff}.strong-text{color:#0f172a;font-weight:800}.muted-text{margin-top:4px;color:#64748b;font-size:12px}.address-text{max-width:260px;white-space:normal}.item-summary{display:grid;gap:3px;line-height:1.45}.money-cell{color:#0f172a;font-variant-numeric:tabular-nums;font-weight:800;white-space:nowrap}.status-pill{display:inline-flex;align-items:center;justify-content:center;min-width:62px;height:26px;padding:0 10px;border-radius:999px;font-size:12px;font-weight:800;white-space:nowrap}.status-pill--pending{color:#d97706;background:#fff7ed}.status-pill--shipped{color:#2563eb;background:#eff6ff}.status-pill--completed,.external-status--ready{color:#047857;background:#ecfdf5}.status-pill--cancelled,.external-status--skipped{color:#64748b;background:#f1f5f9}.external-status--wait_match{color:#d97706;background:#fff7ed}.external-status--error{color:#dc2626;background:#fef2f2}.external-status--converted{color:#2563eb;background:#eff6ff}.text-button{border:none;border-radius:999px;background:transparent;color:#475569;cursor:pointer;font-size:12px;font-weight:800;padding:6px 9px}.text-button:hover{background:#f1f5f9;color:#0f172a}.primary-text{color:#2563eb}.danger-text{color:#dc2626}.empty-cell{padding:32px 16px;color:#94a3b8;text-align:center}.pagination-bar{display:flex;align-items:center;justify-content:space-between;gap:12px;margin:14px;padding:12px 14px;border:1px solid #edf2f7;border-radius:12px;background:#fff;color:#64748b;font-size:13px}.pagination-actions button{height:34px;padding:0 12px;border:1px solid #dbe3ef;border-radius:8px;background:#fff;color:#334155;cursor:pointer;font-weight:800}.pagination-actions button:disabled{opacity:.45;cursor:not-allowed}.primary-button,.secondary-button{display:inline-flex;align-items:center;justify-content:center;min-height:40px;border-radius:10px;cursor:pointer;font-size:14px;font-weight:800;padding:0 15px}.primary-button{border:1px solid #2563eb;background:#2563eb;color:#fff;box-shadow:0 8px 18px rgba(37,99,235,.18)}.primary-button:hover:not(:disabled){background:#1d4ed8;border-color:#1d4ed8}.secondary-button{border:1px solid #dbe3ef;background:#fff;color:#334155}.secondary-button:hover:not(:disabled){border-color:#bfdbfe;background:#eff6ff;color:#2563eb}.primary-button:disabled,.secondary-button:disabled{opacity:.55;cursor:not-allowed;box-shadow:none}.compact-button{min-height:34px;padding:0 12px;font-size:13px}.import-layout{display:grid;grid-template-columns:minmax(380px,1fr) minmax(340px,430px);gap:14px}.import-form{display:grid;gap:12px;padding:0 18px 18px}.batch-list{display:grid;gap:10px;padding:0 14px 14px}.batch-card{display:grid;gap:8px;padding:12px;border:1px solid #edf2f7;border-radius:12px;background:#fff;text-align:left;cursor:pointer}.batch-card.active{border-color:#93c5fd;background:#eff6ff}.batch-card strong{display:block;color:#0f172a}.batch-card span{color:#64748b;font-size:12px}.batch-card em{justify-self:start;padding:4px 8px;border-radius:999px;background:#f1f5f9;color:#475569;font-style:normal;font-size:12px;font-weight:800}.batch-stats{display:flex;gap:8px;flex-wrap:wrap}.raw-items{display:grid;gap:8px}.raw-item{display:grid;grid-template-columns:minmax(220px,1fr) minmax(180px,1fr) auto;gap:8px;align-items:center;padding:9px;border:1px solid #dcfce7;border-radius:10px;background:#f0fdf4}.raw-item.danger{border-color:#fecaca;background:#fef2f2}.raw-item strong,.raw-item span{display:block}.raw-item span{margin-top:3px;color:#64748b;font-size:12px}.modal-backdrop{position:fixed;inset:0;z-index:1000;display:flex;align-items:center;justify-content:center;padding:20px;background:rgba(15,23,42,.45)}.modal-content{width:min(100%,860px);max-height:90vh;overflow:hidden;border-radius:14px;background:#fff;box-shadow:0 24px 60px rgba(15,23,42,.24)}.small-modal{width:min(100%,720px)}.purchase-modal{display:flex;flex-direction:column}.modal-header,.modal-footer{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:16px 18px;border-bottom:1px solid #edf2f7}.modal-header h2{margin:0;color:#0f172a;font-size:18px}.icon-button{display:inline-flex;align-items:center;justify-content:center;width:34px;height:34px;border:none;border-radius:8px;background:#f8fafc;color:#64748b;cursor:pointer;font-size:22px;line-height:1}.modal-body{display:grid;gap:14px;padding:18px;overflow-y:auto}.form-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}.items-header{display:flex;align-items:center;justify-content:space-between;gap:12px}.items-header h3{margin:0;color:#0f172a;font-size:15px}.purchase-items{display:grid;gap:10px}.purchase-item-row{display:grid;grid-template-columns:minmax(260px,1fr) 88px 108px 110px auto;align-items:center;gap:10px;padding:10px;border:1px solid #edf2f7;border-radius:12px;background:#f8fafc}.purchase-item-row input,.purchase-item-row select{height:38px;padding:0 10px}.purchase-item-row strong{text-align:right;white-space:nowrap}.modal-footer{border-top:1px solid #edf2f7;border-bottom:none}.modal-footer>span{font-size:15px;font-weight:900}@media(max-width:1180px){.page-header,.import-layout{grid-template-columns:1fr}.toolbar{grid-template-columns:1fr 180px}.filter-actions{grid-column:1/-1}}@media(max-width:768px){.toolbar,.form-grid,.purchase-item-row,.raw-item{grid-template-columns:1fr}.pagination-bar,.modal-header,.modal-footer{align-items:stretch;flex-direction:column}.footer-actions button{flex:1}}.quick-match-info{padding:10px 12px;border:1px solid #e5edf7;border-radius:10px;background:#f8fafc;color:#334155;font-size:14px}.quick-match-info strong{color:#0f172a;font-weight:800}.quick-match-label{display:grid;gap:6px;color:#334155;font-size:13px;font-weight:800}.quick-match-label select{width:100%;height:40px;padding:0 12px;border:1px solid #dbe3ef;border-radius:10px;background:#fff;color:#0f172a;font-size:14px;outline:none}.quick-match-preview{display:flex;align-items:center;gap:8px;padding:10px 12px;border:1px solid #e5edf7;border-radius:10px;background:#f8fafc;color:#334155;font-size:14px;margin-bottom:12px}.inline-number{width:60px;height:32px;padding:0 6px;border:1px solid #dbe3ef;border-radius:6px;text-align:center;font-size:14px;color:#0f172a}.quick-match-row{display:flex;align-items:flex-end;gap:8px}.quick-match-row .quick-match-label{flex:1}.quick-match-row .text-button{white-space:nowrap;margin-bottom:4px}.quick-match-create{margin-top:12px;padding:12px;border:1px dashed #dbe3ef;border-radius:10px;background:#f8fafc}.quick-match-create .form-grid{margin-bottom:8px}.compact-button{width:100%;justify-content:center;margin-top:4px}
</style>
