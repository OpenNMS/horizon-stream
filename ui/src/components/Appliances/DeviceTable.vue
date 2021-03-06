<template>
  <TableCard>
    <div class="header">
      <div class="title-container">
        <FeatherButton
          data-test="show-minions-btn"
          icon="Show Minions" 
          @click="appliancesStore.showMinionsTable"
          v-if="!appliancesStore.minionsTableOpen"
        >
          <FeatherIcon :icon="ChevronRight" />
        </FeatherButton>
        <span class="title">Devices</span>
      </div>

      <FeatherInput class="search" v-model="searchValue" label="Devices">
        <template v-slot:pre>
          <FeatherIcon :icon="Search" />
        </template>
      </FeatherInput>

      <div class="btns">
        <FeatherButton icon="Filter">
          <FeatherIcon :icon="FilterAlt" />
        </FeatherButton>
        <FeatherButton icon="Sort">
          <FeatherIcon :icon="Sort" />
        </FeatherButton>
      </div>

    </div>
    <div class="data-table">
      <TransitionGroup name="data-table" tag="div">
        <div class="card" v-for="(device) in listDevicesWithBgColor" :key="device.id" data-test="device-item">
          <div class="name" data-test="col-device">
              <div class="name-cell">
                <FeatherIcon :icon="Instances" class="icon"/>
                <div class="text">
                  <div class="name">{{ device.label }}</div>
                  <div class="server">{{ device.createTime }}</div>
                </div>
              </div>
          </div>
          <div data-test="col-latency">
            <pre class="title">ICMP Latency</pre>
            <div class="value" :class="device.latencyBgColor">{{ device.icmp_latency }}</div>
          </div>
          <div data-test="col-uptime">
            <pre class="title">SNMP Uptime</pre>
            <div class="value" :class="device.uptimeBgColor">{{ device.snmp_uptime }}</div>
          </div>
          <div data-test="col-status">
            <div class="value" :class="device.statusBgColor">{{ device.status }}</div>
          </div>
        </div>
      </TransitionGroup>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import FilterAlt from '@featherds/icon/action/FilterAlt'
import Sort from '@featherds/icon/action/Sort'
import Search from '@featherds/icon/action/Search'
import Instances from '@featherds/icon/hardware/Instances'
import ChevronRight from '@featherds/icon/navigation/ChevronRight'
import { useDeviceQueries } from '@/store/Queries/deviceQueries'
import { formatItemBgColor } from '@/helpers/formatting'
import { useAppliancesStore } from '@/store/Views/appliancesStore'
import { DeviceDto } from '@/types/graphql'

interface ExtendedDeviceDTO extends DeviceDto {
  id: number // override as required

  // TODO: These fields not yet on the BE
  status: string
  icmp_latency: string
  snmp_uptime: string

  // Color fields
  uptimeBgColor: string
  latencyBgColor: string
  statusBgColor: string
}

const deviceQueries = useDeviceQueries()
const appliancesStore = useAppliancesStore()

const listDevicesWithBgColor = computed<ExtendedDeviceDTO[]>(() => formatItemBgColor(deviceQueries.listDevices))

const searchValue = ref('')
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

.header {
  display: flex;
  justify-content: space-between;

  .title-container {
    display: flex;
    .title {
      @include headline3;
      margin-left: 15px;
      margin-top: 2px;
    }
  }
  
  .search {
    width: 300px;
  }

  .btns {
    display: flex;
  }
}
.card {
  border: 1px solid var($shade-4);
  display: flex;
  margin-bottom: 10px;
  border-radius: 5px;
  height: 65px;
  
  div {
    display: flex;
    flex-direction: column;
    justify-content: center;
    width: 20%;
    padding: 8px;
    line-height: 15px;
    font-size: 11px;

    &.name {
      @include subtitle1;
      width: 40%;
      color: var($primary);

      .name-cell {
        flex-direction: row;
        width: 100%;
        justify-content: flex-start;
        white-space: nowrap;
        align-items: center;
        .icon {
          font-size: 25px;
          color: var($shade-2);
        }

        .text {
          flex-direction: column;
          width: 100%;
          .name {
            font-size: 15px;
            line-height: 0px;
          }
          .server {
            line-height: 10px;
            color: var($secondary)
          }
        }
      }
    }

    .title {
      font-family: inherit;
      margin: 0px;
    }

    .value {
      display: inline-table;
      border-radius: 5px;
      padding: 3px;
      text-align: center;
    }
  }
}
</style>
