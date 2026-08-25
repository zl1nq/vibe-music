<template>
  <div class="splash-screen">
    <div class="flex flex-col items-center">
      <img src="/logo.svg" alt="logo" class="splash-logo w-16 h-16" />
      <h1 class="splash-title font-serif-display">Vibe Music</h1>
      <p class="splash-tagline">LET THE MUSIC FLOW</p>
    </div>
  </div>
</template>

<style scoped>
/* 全屏开场页：Logo + 标题渐显上浮，约 1.6s 后整体淡出 */
.splash-screen {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #faf7f2;
  pointer-events: none;
  animation: splash-out 0.4s cubic-bezier(0.22, 0.61, 0.36, 1) 1.1s forwards;
}

.splash-logo {
  animation: splash-rise 0.5s cubic-bezier(0.22, 0.61, 0.36, 1) 0.05s both;
}

.splash-title {
  margin-top: 20px;
  font-size: 44px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #26221c;
  animation: splash-rise 0.5s cubic-bezier(0.22, 0.61, 0.36, 1) 0.2s both;
}

.splash-tagline {
  margin-top: 12px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.35em;
  color: var(--primary);
  animation: splash-rise 0.5s cubic-bezier(0.22, 0.61, 0.36, 1) 0.35s both;
}

@keyframes splash-rise {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes splash-out {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
    visibility: hidden;
  }
}

@media (prefers-reduced-motion: reduce) {
  .splash-screen,
  .splash-logo,
  .splash-title,
  .splash-tagline {
    animation-duration: 0.01s;
    animation-delay: 0s;
  }
}
</style>
