  
<template>
  <FeatherAppLayout class="feather-styles layout">
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
import { useAuthStore } from './store/authStore'
const authStore = useAuthStore()
authStore.getUserInfo()
</script>
  
<style lang="scss">
@import "@featherds/styles/lib/grid";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/open-mixins";

html {
  height: 100%;
  overflow: hidden;
}

@keyframes gradient {
  to { opacity: 1; }
  0% { -webkit-filter: blur(90px);}
  50% { -webkit-filter: blur(100px);}
  100% { -webkit-filter: blur(90px);}
  from { transform : scale(0.8);}
  to { transform : scale(1); }
}

.app-content-container {
  position: relative;
}

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

table {
  width: 100%;
}
</style>
