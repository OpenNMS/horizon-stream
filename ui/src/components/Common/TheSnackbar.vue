<template>
  <FeatherSnackbar v-model="isDisplayed" :center="isCentered" :error="hasError">
    {{ message }}
    <template v-slot:button>
      <FeatherButton @click="hideSnackbar" text>Dismiss</FeatherButton>
    </template>
  </FeatherSnackbar>
</template>

<script setup lang="ts">
import { FeatherSnackbar } from '@featherds/snackbar'
import { FeatherButton } from '@featherds/button'
import useSnackbar from '@/composables/useSnackbar'

const { hideSnackbar, isDisplayed, isCentered, hasError, message } = useSnackbar()
</script>
