<template>
  <p class="title">Horizon Stream</p>
  <p class="welcome" v-if="keycloak?.authenticated">Welcome, {{ keycloak.profile?.username }}</p>
  <AlarmsTable />
</template>

<script setup lang="ts">
import useKeycloak from '@/composables/useKeycloak'
const { keycloak } = useKeycloak()
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";

p {
  text-align: center;
}
.title {
  @include headline2;
  margin-top: 10px;
}
.welcome {
  @include headline4;
}
</style>
