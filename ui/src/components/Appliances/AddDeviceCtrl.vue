<template>
  <FeatherButton 
    primary
    data-test="add-device-btn" 
    @click="openModal"
  > 
    <template v-slot:icon>
      <FeatherIcon 
        :icon="Add" 
        aria-hidden="true" 
        focusable="false" 
      />
      Add Device
    </template>
  </FeatherButton>

  <PrimaryModal title="Add Device" :visible="isVisible">
    <template v-slot:content>
      <!-- Device Name -->
      <FeatherInput
        data-test="name-input"
        label="Name"
        v-model="device.label"
      />

      <!-- Management IP -->
      <FeatherInput
        data-test="ip-input"
        label="Management IP (Optional temp)"
        v-model="device.managementIp"
      />

      <!-- Community String -->
      <FeatherInput
        data-test="string-input"
        label="Community String (Optional)"
        v-model="device.snmpCommunityString"
      />

      <!-- Port -->
      <FeatherInput
        type="number"
        data-test="port-input"
        label="Port (Optional)"
        v-model="device.port"
      />
      
      <!-- Latitude -->
      <FeatherInput
        type="number"
        data-test="port-latitude"
        label="Latitude (Optional)"
        v-model="device.location.latitude"
      />
      
      <!-- Longitude -->
      <FeatherInput
        type="number"
        data-test="port-longitude"
        label="Longitude (Optional)"
        v-model="device.location.longitude"
      />
    </template>

    <template v-slot:footer>
      <FeatherButton 
        data-test="cancel-btn" 
        secondary 
        @click="closeModal">
          Cancel
      </FeatherButton>
      
      <!-- TODO: Management IP should also be required, when available. -->
      <FeatherButton 
        data-test="save-btn" 
        primary
        :disabled="!device.label" 
        @click="save">
          Save
      </FeatherButton>
    </template>
  </PrimaryModal>
</template>

<script setup lang="ts">
import Add from '@featherds/icon/action/Add'
import { useDeviceMutations } from '@/store/Mutations/deviceMutations'
import { useDeviceQueries } from '@/store/Queries/deviceQueries'
import useModal from '@/composables/useModal'
import useSnackbar from '@/composables/useSnackbar'

const { showSnackbar } = useSnackbar()
const { openModal, closeModal, isVisible } = useModal()
const deviceMutations = useDeviceMutations()
const deviceQueries = useDeviceQueries()

const defaultDevice = {
  label: undefined,
  managementIp: undefined,
  snmpCommunityString: undefined,
  port: undefined,
  location: {
    latitude: undefined,
    longitude: undefined
  }
}

const device = reactive({...defaultDevice})

const save = async () => {
  await deviceMutations.addDevice({ device })

  if (!deviceMutations.error) {
    // clears device obj on successful save
    Object.assign(device, defaultDevice)

    closeModal()

    showSnackbar({
      msg: 'Device successfuly saved.'
    })

    // Timeout because device may not be available right away
    // TODO: Replace timeout with websocket/polling
    setTimeout(() => {
      deviceQueries.fetch()
    }, 350)
  }
}
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";

.title {
  @include headline3;
  text-align: center;
  margin-bottom: 15px;
}
</style>