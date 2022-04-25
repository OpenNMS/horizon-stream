<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12">
        <FeatherButton class="send-btn" @click="onSend({})">Send Trigger</FeatherButton>
        <FeatherButton class="clear-btn" @click="onClear({})">Send Clear</FeatherButton>
      </div>
    </div>
    <div class="feather-row">
      <div class="feather-col-12">
        <table summary="Alarms">
          <thead>
            <tr>
              <th>Severity</th>
              <th>Description</th>
              <th>Last Event Time</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="alarm in alarms" :key="alarm.id">
              <td>{{ alarm.severity }}</td>
              <td>{{ alarm.description }}</td>
              <td>{{ alarm.lastEventTime }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAlarmStore } from '@/store/alarmStore'
// import { Alarm } from '@/types/alarms.js'

const alarmStore = useAlarmStore()
const alarms = computed(() => alarmStore.alarms)

const onSend = (alarm: any) => alarmStore.sendAlarm(alarm)
const onClear = (alarm: any) => alarmStore.clearAlarm(alarm)
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/typography";

.send-btn {
  background: var($error);
  color: var($primary-text-on-color);
}

.clear-btn {
  background: var($success);
  color: var($primary-text-on-color);

}

.card {
  background: var($surface);
  padding: 15px;
}

table {
  width: 100%;
  @include table;
}
</style>
