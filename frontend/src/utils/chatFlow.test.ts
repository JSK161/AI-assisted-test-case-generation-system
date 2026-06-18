import { describe, expect, it } from 'vitest'
import { appendConfirmQuestion, buildAnswerSummary, createMockGeneratedPlan, getNextQuestion } from './chatFlow'
import type { ChatAnswer, ClarificationQuestion } from '@/types/chat'

describe('chatFlow', () => {
  const aiQuestions: ClarificationQuestion[] = appendConfirmQuestion([
    {
      id: 'target_platform',
      title: '这个支付模块主要运行在哪些端？',
      type: 'multiple',
      options: [
        { label: 'Web 收银台', value: 'Web 收银台', description: '关注浏览器兼容性和跳转回调' },
        { label: '移动端 App', value: '移动端 App', description: '关注拉起支付和中断恢复' }
      ],
      allowCustom: true
    },
    {
      id: 'payment_channels',
      title: '需要覆盖哪些支付渠道？',
      type: 'multiple',
      options: [
        { label: '微信支付', value: '微信支付' },
        { label: '支付宝', value: '支付宝' }
      ]
    }
  ])

  it('returns DeepSeek generated questions in order and stops after confirmation', () => {
    expect(getNextQuestion(aiQuestions, [])?.id).toBe('target_platform')

    const answers: ChatAnswer[] = [
      { questionId: 'target_platform', values: ['Web 收银台'] },
      { questionId: 'payment_channels', values: ['微信支付'] },
      { questionId: 'confirm', values: ['confirmed'] }
    ]

    expect(getNextQuestion(aiQuestions, answers)).toBeNull()
  })

  it('builds a readable summary from dynamic question titles and selected answers', () => {
    const summary = buildAnswerSummary(aiQuestions, [
      { questionId: 'target_platform', values: ['Web 收银台'], customText: '还要覆盖 H5 页面' },
      { questionId: 'payment_channels', values: ['微信支付', '支付宝'] }
    ])

    expect(summary).toContain('这个支付模块主要运行在哪些端？')
    expect(summary).toContain('Web 收银台')
    expect(summary).toContain('还要覆盖 H5 页面')
    expect(summary).toContain('需要覆盖哪些支付渠道？')
    expect(summary).toContain('微信支付')
  })

  it('creates a document-style mock plan from requirement and answers', () => {
    const plan = createMockGeneratedPlan('我想测试支付模块', aiQuestions, [
      { questionId: 'target_platform', values: ['Web 收银台'] },
      { questionId: 'payment_channels', values: ['微信支付'] }
    ])

    expect(plan.title).toContain('支付模块')
    expect(plan.testCases.length).toBeGreaterThanOrEqual(3)
    expect(plan.testCases[0].steps.length).toBeGreaterThan(1)
  })
})
