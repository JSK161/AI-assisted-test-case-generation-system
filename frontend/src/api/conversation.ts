import request from '@/utils/request'

export interface ConversationListItem {
  id: number
  title: string
  requirement: string
  updatedAt: string
}

export interface MessageItem {
  id: number
  sender: string
  content: string
  createdAt: string
}

export interface ConversationDetail {
  id: number
  title: string
  requirement: string
  messages: MessageItem[]
  answers: any
  generatedPlan: any
  createdAt: string
  updatedAt: string
}

export function listConversations() {
  return request.get<ConversationListItem[], ConversationListItem[]>('/conversations')
}

export function getConversation(id: number) {
  return request.get<ConversationDetail, ConversationDetail>(`/conversations/${id}`)
}

export function createConversation(title: string, requirement: string) {
  return request.post<{ id: number }, { id: number }>('/conversations', { title, requirement })
}

export function addMessage(conversationId: number, sender: string, content: string) {
  return request.post(`/conversations/${conversationId}/messages`, { sender, content })
}

export function updateAnswers(conversationId: number, answers: any) {
  return request.put(`/conversations/${conversationId}/answers`, answers)
}

export function updatePlan(conversationId: number, plan: any) {
  return request.put(`/conversations/${conversationId}/plan`, plan)
}

export function deleteConversation(id: number) {
  return request.delete(`/conversations/${id}`)
}
