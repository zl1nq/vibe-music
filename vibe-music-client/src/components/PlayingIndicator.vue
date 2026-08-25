<script setup lang="ts">
defineProps<{ paused?: boolean }>()
</script>

<template>
  <div class="flex items-end gap-[2px] h-3.5" aria-hidden="true">
    <span
      v-for="i in 4"
      :key="i"
      class="w-[3px] h-full rounded-full bg-primary origin-bottom animate-eq-bounce"
      :style="{
        animationDelay: `${(i - 1) * 0.15}s`,
        animationPlayState: paused ? 'paused' : 'running',
      }"
    />
  </div>
</template>
