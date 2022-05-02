<template>
  <div class="container">
    <div class="feather-row">
      <div class="feather-col-12">
        <div class="login-container">
          <Logo class="logo" />
          <form autocomplete="off">
            <!-- Username -->
            <FeatherInput 
              label="Username" 
              v-model="username"  
              autocomplete="new-username"
            />

            <!-- Password -->
            <FeatherProtectedInput
              label="Password" 
              v-model="password" 
              autocomplete="new-password"
            />
          </form>

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
import Logo from '@/assets/Logo.vue'
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
@import "@featherds/styles/mixins/elevation";

.container {
  position: relative;
  height: calc(100vh - 115px);

  .login-container {
    @include elevation(1);
    display: flex;
    flex-direction: column;
    margin: auto;
    width: 350px;
    position: absolute;
    top: 25%;
    left: 50%;
    transform: translateX(-50%);
    background: var($surface);
    padding: 50px;

    .logo {
      width: 16em;
      margin: auto;
      margin-bottom: 20px;
    }
  }
}
</style>
