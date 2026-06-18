import { describe, expect, it } from 'vitest'
import { calculateAdoptionRate } from './statistics'

describe('calculateAdoptionRate', () => {
  it('calculates adoption rate with one decimal place', () => {
    expect(calculateAdoptionRate(2, 3)).toBe(66.7)
  })

  it('returns zero when total count is zero', () => {
    expect(calculateAdoptionRate(0, 0)).toBe(0)
  })
})
