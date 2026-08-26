<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'

const props = defineProps<{ content: string }>()

const md = new MarkdownIt({
  html: false, // 关闭原始 HTML，防止 AI 输出注入
  linkify: true, // 裸链接自动转 <a>
  breaks: true, // 聊天输出单换行即换行
})

// 拦截危险链接协议（javascript: 等），其余按默认规则校验
const defaultValidateLink = md.validateLink
md.validateLink = (url: string) => {
  const trimmed = url.trim().toLowerCase()
  if (
    trimmed.startsWith('javascript:') ||
    trimmed.startsWith('data:text/html') ||
    trimmed.startsWith('vbscript:')
  ) {
    return false
  }
  return defaultValidateLink(url)
}

const render = computed(() => md.render(props.content))
</script>

<template>
  <div class="vibe-md" v-html="render"></div>
</template>

<style scoped lang="scss">
.vibe-md {
  font-size: 0.875rem;
  line-height: 1.7;
  word-break: break-word;

  :deep(p) {
    margin: 0.35em 0;
    &:first-child {
      margin-top: 0;
    }
    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(h1),
  :deep(h2),
  :deep(h3),
  :deep(h4) {
    font-weight: 600;
    margin: 0.7em 0 0.35em;
    line-height: 1.4;

    &:first-child {
      margin-top: 0;
    }
  }

  :deep(ul),
  :deep(ol) {
    padding-left: 1.25em;
    margin: 0.35em 0;
  }

  :deep(li) {
    margin: 0.15em 0;
  }

  :deep(a) {
    color: var(--primary);
    text-decoration: underline;
    text-underline-offset: 2px;
  }

  :deep(strong) {
    font-weight: 600;
  }

  :deep(blockquote) {
    margin: 0.5em 0;
    padding: 0.2em 0.9em;
    border-left: 3px solid var(--primary);
    color: hsl(var(--muted-foreground));
    background: hsl(var(--hover-menu-bg) / 0.6);
    border-radius: 0 8px 8px 0;
  }

  :deep(code) {
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
    font-size: 0.8em;
    background: hsl(var(--hover-menu-bg));
    padding: 0.15em 0.4em;
    border-radius: 4px;
  }

  :deep(pre) {
    background: #26221c;
    color: #faf7f2;
    padding: 0.75em 1em;
    border-radius: 10px;
    overflow-x: auto;
    margin: 0.5em 0;

    code {
      background: transparent;
      padding: 0;
      font-size: 0.8em;
      color: inherit;
    }
  }

  :deep(table) {
    border-collapse: collapse;
    margin: 0.5em 0;
    font-size: 0.85em;

    th,
    td {
      border: 1px solid hsl(var(--border));
      padding: 0.35em 0.7em;
    }

    th {
      background: hsl(var(--hover-menu-bg));
    }
  }

  :deep(img) {
    max-width: 100%;
    border-radius: 8px;
  }

  :deep(hr) {
    border: none;
    border-top: 1px solid hsl(var(--border));
    margin: 0.8em 0;
  }
}
</style>
