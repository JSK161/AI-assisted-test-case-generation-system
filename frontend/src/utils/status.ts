export const ADOPTION_STATUS_OPTIONS = [
  { label: '未处理', value: 'PENDING', type: 'info' },
  { label: '已采纳', value: 'ADOPTED', type: 'success' },
  { label: '需修改', value: 'NEEDS_REVISION', type: 'warning' },
  { label: '已拒绝', value: 'REJECTED', type: 'danger' }
] as const

export type AdoptionStatus = (typeof ADOPTION_STATUS_OPTIONS)[number]['value']

export function getAdoptionStatusMeta(status: string) {
  return ADOPTION_STATUS_OPTIONS.find((item) => item.value === status) || ADOPTION_STATUS_OPTIONS[0]
}
