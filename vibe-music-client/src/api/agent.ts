/**
 * VibeAgent 接口
 * 聊天接口是 POST + SSE 流式输出，用 fetch 手动解析，不走 axios（避免响应拦截器干扰）
 */
const BASE_URL = import.meta.env.VITE_APP_BASE_API || 'http://localhost:8080'

/**
 * 流式调用 agent 聊天接口
 * @param conversationId 会话 id，服务端据此维持对话记忆
 * @param query 用户消息
 * @param onDelta 每收到一段文本增量时回调
 * @param signal 用于中断请求
 */
export const streamAgentChat = async (
  conversationId: string,
  query: string,
  onDelta: (delta: string) => void,
  signal?: AbortSignal
): Promise<void> => {
  const response = await fetch(`${BASE_URL}/agent/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
    },
    body: JSON.stringify({ conversationId, query }),
    signal,
  })

  if (!response.ok || !response.body) {
    throw new Error(`请求失败：${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    // SSE 事件之间以空行分隔，按事件逐段解析
    buffer += decoder.decode(value, { stream: true })
    const events = buffer.split(/\r?\n\r?\n/)
    buffer = events.pop() ?? ''

    for (const event of events) {
      // 一个 SSE 事件内的多行 data: 拼接为一段完整内容（保留换行）
      const data = event
        .split(/\r?\n/)
        .filter((line) => line.startsWith('data:'))
        .map((line) => line.slice(5))
        .join('\n')
      if (data.trim() && data !== '[DONE]') {
        onDelta(data)
      }
    }
  }
}
