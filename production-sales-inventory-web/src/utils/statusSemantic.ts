export type StatusSemantic = 'active' | 'pending' | 'completed' | 'cancelled' | 'danger'

const semanticMap: Record<string, StatusSemantic> = {
  // Purchase
  DRAFT: 'pending',
  PENDING_INBOUND: 'active',
  INBOUNDED: 'completed',
  // Sales orders
  PENDING: 'pending',
  SHIPPED: 'active',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled',
  // Sales import batches
  PARSED: 'active',
  CONFIRMED: 'pending',
  // Production
  PLANNED: 'pending',
  IN_PROGRESS: 'active',
  WAIT_INBOUND: 'active',
  // Sales import batches (external order matching)
  WAIT_MATCH: 'pending',
  READY: 'active',
  CONVERTED: 'completed',
  SKIPPED: 'cancelled',
  ERROR: 'danger',
  // User
  ACTIVE: 'completed',
  DISABLED: 'cancelled',
  // Inventory stock
  NORMAL: 'completed',
  WARNING: 'danger',
  OUT: 'danger',
}

export function toStatusSemantic(status: string): StatusSemantic {
  return semanticMap[status.toUpperCase()] ?? 'pending'
}
