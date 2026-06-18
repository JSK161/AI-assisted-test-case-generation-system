import { describe, expect, it } from 'vitest'
import { getAdoptionStatusMeta } from './status'

describe('getAdoptionStatusMeta', () => {
  it('returns readable metadata for adopted status', () => {
    const result = getAdoptionStatusMeta('ADOPTED')

    expect(result.label).toBe('已采纳')
    expect(result.type).toBe('success')
  })

  it('falls back to pending metadata for unknown status', () => {
    const result = getAdoptionStatusMeta('UNKNOWN')

    expect(result.label).toBe('未处理')
    expect(result.type).toBe('info')
  })
})
