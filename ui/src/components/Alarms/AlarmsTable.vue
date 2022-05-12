<template>
  <div class="container">
    <p>Alarms</p>
    <div class="feather-row">
      <div class="feather-col-12">
        <FeatherButton primary @click="trigger">Send Alarm</FeatherButton>
        <FeatherButton secondary @click="clear">Clear Alarms</FeatherButton>
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
              <td v-date>{{ alarm.lastEventTime }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getMockEvent } from '@/types/mocks'
import { useAlarmStore } from '@/store/alarmStore'
import { useEventStore } from '@/store/eventStore'

const alarmStore = useAlarmStore()
const eventStore = useEventStore()

const alarms = computed(() => alarmStore.alarms)

const trigger = () => {
  eventStore.sendEvent(getMockEvent())
  alarmStore.getAlarms()
}

const clear = () => {
  for (const alarm of alarms.value) {
    alarmStore.deleteAlarmById(alarm.id)
  }
  alarmStore.getAlarms()
}

onMounted(() => alarmStore.getAlarms())
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
  margin-top: 10px;

  p {
    @include headline1;
    margin-bottom: 24px;
  }

  table {
    @include table;
  }
}
</style>
