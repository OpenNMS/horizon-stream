<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12">
        <div class="login-container">
          <!-- Username -->
          <FeatherInput label="Username" v-model="username" />

          <!-- Password -->
          <FeatherProtectedInput label="Password" v-model="password" />

          <!-- Login -->
          <FeatherButton primary @click="onLoginBtnClick">
            Login
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useLoginStore } from '@/store/loginStore'
const loginStore = useLoginStore()

const username = ref('')
const password = ref('')

const onLoginBtnClick = () => {
  loginStore.login(username.value, password.value)
}
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";

.card {
  position: relative;
  height: calc(100vh - 115px);
  background: var($surface);
  padding: 15px;

  .login-container {
    display: flex;
    flex-direction: column;
    margin: auto;
    width: 400px;
    position: absolute;
    top: 30%;
    transform: translateY(-70%);
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
