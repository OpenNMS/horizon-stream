<template>
  <FeatherNavigationRail v-if="false">
    <template v-slot:main>
      <FeatherRailItem :class="{ selected: isSelected('/') }" href="#/" :icon="Home" title="Dashboard" />
    </template>
  </FeatherNavigationRail>
</template>
<script setup lang=ts>
import Home from '@featherds/icon/action/Home'

const route = useRoute()
const isSelected = (path: string) => path === route.fullPath
</script>
