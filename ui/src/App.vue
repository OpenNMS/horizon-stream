  
<template>
  <FeatherAppLayout class="layout">
    <template v-slot:header>
      <Menubar />
    </template>

    <template v-slot:rail>
      <NavigationRail />
    </template>

    <Spinner />
    <Snackbar />
    <router-view />
  </FeatherAppLayout>
</template>
  
<script setup lang="ts">
</script>
  
<style lang="scss">
@import "@featherds/styles/lib/grid";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/open-mixins";

.layout {
  max-width: 1500px;
  margin: auto;
}

a {
  text-decoration: none;
  color: var($primary);
}

.pointer {
  cursor: pointer;
}
</style>
