  
<template>
  <FeatherAppLayout content-layout="full">
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

a {
  text-decoration: none;
  color: var($primary);
}

.pointer {
  cursor: pointer;
}
</style>
