<template>
  <FeatherNavigationRail>
    <template v-slot:main>
      <FeatherRailItem :class="{ selected: isSelected('/') }" href="#/" :icon="Home" title="Home" />
    </template>
  </FeatherNavigationRail>
</template>
<script setup lang=ts>
import { useRoute } from 'vue-router'
import Home from '@featherds/icon/action/Home'
import {
  FeatherNavigationRail,
  FeatherRailItem,
} from '@featherds/navigation-rail'

const route = useRoute()
const isSelected = (path: string) => path === route.fullPath
</script>
