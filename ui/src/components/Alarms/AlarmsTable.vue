<template>
  <div class="container">
    <p>Alarms</p>
    <div class="feather-row">
      <div class="feather-col-12">
        <FeatherButton primary @click="onSend({})">Send Alarm</FeatherButton>
        <FeatherButton secondary @click="onClear({})">Clear Alarms</FeatherButton>
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
@import "@featherds/styles/mixins/elevation";
.container {
  @include elevation(1);
  background: var($surface);
  padding: 10px 30px 30px 30px;
  width: 700px;
  margin: auto;
  margin-top: 150px;

  p {
    @include headline1;
    margin-bottom: 24px;
  }

  table {
    @include table;
  }
}
</style>
