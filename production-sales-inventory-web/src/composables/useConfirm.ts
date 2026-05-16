import { useConfirmStore, type ConfirmOptions } from '@/stores/confirm'

export function useConfirm() {
  const store = useConfirmStore()
  return (options: ConfirmOptions) => store.open(options)
}
