<template>
  <div class="header-container">
    <div class="welcome" 
      v-if="keycloak?.authenticated">
        Welcome, {{ keycloak.tokenParsed?.preferred_username }}
    </div>
    <div class="btns">
      <NotificationsCtrl />
      <AddDeviceCtrl />
    </div>
  </div>

  <div class="minions-device-container">
    <div class="minions-table"><MinionsTable /></div>
    <div class="device-table"><DeviceTable /></div>
  </div>
</template>

<script lang="ts" setup>
import { useAppliancesStore } from '@/store/Views/appliancesStore'
import useKeycloak from '@/composables/useKeycloak'

const { keycloak } = useKeycloak()
const appliancesStore = useAppliancesStore()

const minionTableWidth = computed<string>(() => appliancesStore.minionsTableOpen ? '40%' : '0%')
const gapWidth = computed<string>(() => appliancesStore.minionsTableOpen ? '20px' : '0px')
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
.header-container {
  display: flex;
  justify-content: space-between;
  margin: 30px 0px 25px 0px;
  .welcome {
    @include headline2;
    font-weight: bold;
  }
  .btns {
    display: flex;
    justify-content: flex-end;
    gap: 20px;
  }
}
.minions-device-container {
  display: flex;
  gap: v-bind(gapWidth);
  
  .minions-table {
    width: v-bind(minionTableWidth);
    transition: width 0.4s ease-out;
  }
  .device-table {
    flex-grow: 1;
  }
}

// small screen / tablet / mobile
$breakpoint: 1024px;
@media (max-width: $breakpoint) {
  .header-container {
    display: block;
    .btns {
      display: block;
    }
  }
  .minions-device-container {
    display: block;
    .minions-table {
      width: 100%;
      margin-bottom: 20px;
    }
    .device-table {
      width: 100%;
    }
  }
}
</style>
