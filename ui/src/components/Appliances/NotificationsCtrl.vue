<template>
  <FeatherButton 
    text
    data-test="notifications-btn"
    @click="openModal"
  >
    Outbound Notification
  </FeatherButton>

  <PrimaryModal title="Outbound Notifications" :visible="isVisible">
    <template v-slot:content>
      <p class="title" data-test="notifications-modal">
        Pager Duty
      </p>
      <FeatherInput
        data-test="routing-input"
        label="API Routing Key"
        v-model="routingKey"
      />
    </template>

    <template v-slot:footer>
      <FeatherButton 
        data-test="cancel-btn" 
        secondary 
        @click="closeModal">
          Cancel
      </FeatherButton>
      
      <FeatherButton 
        data-test="save-btn" 
        primary
        :disabled="!routingKey"
        @click="save">
          Save
      </FeatherButton>
    </template>
  </PrimaryModal>
</template>

<script setup lang="ts">
import { useNotificationMutations } from '@/store/Mutations/notificationMutations'
import useModal from '@/composables/useModal'
import useSnackbar from '@/composables/useSnackbar'

const { showSnackbar } = useSnackbar()
const { openModal, closeModal, isVisible } = useModal()
const notificationMutations = useNotificationMutations()

const routingKey = ref<string>()

const save = async () => {
  await notificationMutations.sendPagerDutyRoutingKey({ key: routingKey.value as string })
  if (!notificationMutations.error) {
    routingKey.value = undefined 
    closeModal()

    showSnackbar({
      msg: 'Routing key successfuly saved.'
    })
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