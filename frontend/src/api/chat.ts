import request from '@/utils/request'
import type { ChatAnswer, ClarificationQuestion, GeneratedPlan } from '@/types/chat'

export interface QuestionGenerateRequest {
  requirement: string
  referenceUrl?: string
}

export interface QuestionGenerateResponse {
  questions: ClarificationQuestion[]
  usedModel: string
}

export interface ChatGenerateRequest {
  requirement: string
  answers: ChatAnswer[]
  referenceUrl?: string
}

export interface ChatGenerateResponse extends GeneratedPlan {
  usedModel: string
}

export function generateChatPlan(data: ChatGenerateRequest) {
  return request.post<ChatGenerateResponse, ChatGenerateResponse>('/chat/generate', data, {
    headers: {
      'X-Silent-Error': 'true'
    }
  })
}

export function generateClarifyingQuestions(data: QuestionGenerateRequest) {
  return request.post<QuestionGenerateResponse, QuestionGenerateResponse>('/chat/questions', data, {
    headers: {
      'X-Silent-Error': 'true'
    }
  })
}
