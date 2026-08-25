import type { Directive } from 'vue'

/**
 * v-reveal：进入视口时 fade-up 渐入
 * 用法：v-reveal 或 v-reveal="120"（延迟毫秒数，用于子项交错）
 */
const reveal: Directive<HTMLElement, number | undefined> = {
  mounted(el, binding) {
    el.classList.add('v-reveal')
    if (typeof binding.value === 'number') {
      el.style.setProperty('--reveal-delay', `${binding.value}ms`)
    }
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            el.classList.add('v-reveal--visible')
            observer.unobserve(el)
          }
        })
      },
      { threshold: 0.1 }
    )
    observer.observe(el)
  },
}

export default reveal
