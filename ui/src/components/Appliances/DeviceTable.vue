<template>
  <table class="tl1 tl2 tl3 data-table" summary="Devices" data-test="device-table">
    <thead>
      <tr>
        <th scope="col" data-test="col-device">Device</th>
        <th scope="col" data-test="col-latency">ICMP Latency</th>
        <th scope="col" data-test="col-uptime">SNMP Uptime</th>
        <th scope="col" data-test="col-status">Status</th>
      </tr>
    </thead>
    <TransitionGroup name="data-table" tag="tbody">
      <tr v-for="(device, index) in deviceQueries.listDevices" :key="device.id" :data-index="index" data-test="device-item">
        <td>{{ device.name }}</td>
        <td>{{ device.icmp_latency }}</td>
        <td>{{ device.snmp_uptime }}</td>
        <td>{{ device.status }}</td>
      </tr>
    </TransitionGroup>
  </table>
</template>

<script setup lang="ts">
import { useDeviceQueries } from '@/store/Queries/deviceQueries'
const deviceQueries = useDeviceQueries()
</script>
