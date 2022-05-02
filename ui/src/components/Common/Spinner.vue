<template>
  <transition name="fade">
    <div class="spinner" v-if="isActive">
      <FeatherSpinner />
    </div>
  </transition>
</template>

<script setup lang="ts">
import useSpinner from '@/composables/useSpinner'
const { isActive } = useSpinner()
</script>

<style scoped lang="scss">
.spinner {
  z-index: 2;
  position: absolute;
  width: 100%;
  background: transparent;
}
.spinner-container {
  height: 78vh;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>
