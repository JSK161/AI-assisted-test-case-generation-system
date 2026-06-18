export function calculateAdoptionRate(adoptedCount: number, totalCount: number): number {
  if (!totalCount) {
    return 0
  }
  return Number(((adoptedCount / totalCount) * 100).toFixed(1))
}
