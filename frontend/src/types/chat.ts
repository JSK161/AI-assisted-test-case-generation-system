export type Sender = 'user' | 'assistant'

export type QuestionType = 'single' | 'multiple' | 'confirm'

export interface OptionItem {
  label: string
  value: string
  description?: string
}

export interface ClarificationQuestion {
  id: string
  title: string
  type: QuestionType
  options: OptionItem[]
  allowCustom?: boolean
}

export interface ChatAnswer {
  questionId: string
  values: string[]
  customText?: string
}

export interface ChatMessage {
  id: string
  sender: Sender
  content: string
  createdAt: string
}

export interface TestCaseItem {
  id: string
  title: string
  priority: 'P0' | 'P1' | 'P2'
  category: string
  precondition: string
  steps: string[]
  expectedResult: string
}

export interface GeneratedPlan {
  title: string
  summary: string
  scope: string[]
  risks: string[]
  testCases: TestCaseItem[]
}
