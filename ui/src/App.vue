  
<template>
  <FeatherAppLayout content-layout="full">
    <template v-slot:header>
      <TheMenubar />
    </template>

    <template v-slot:rail>
      <TheNavigationRail />
    </template>

    <TheSpinner />
    <TheSnackbar />
    <router-view />
  </FeatherAppLayout>
</template>
  
<script setup lang="ts">
import { FeatherAppLayout } from '@featherds/app-layout'
import TheMenubar from '@/components/Layout/TheMenubar.vue'
import TheSpinner from '@/components/Common/TheSpinner.vue'
import TheSnackbar from './components/Common/TheSnackbar.vue'
import TheNavigationRail from '@/components/Layout/TheNavigationRail.vue'
</script>
  
<style lang="scss">
@import "@featherds/styles/lib/grid";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/open-mixins";

a {
  text-decoration: none;
  color: var($primary);
}

.pointer {
  cursor: pointer;
}
</style>
