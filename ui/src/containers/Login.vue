<template>
  <div class="container">
    <div class="feather-row">
      <div class="feather-col-12">
        <div class="login-container">
          <Logo class="logo" />
          <span class="hs-title">HORIZON STREAM</span>
          <span class="sign-in-title">Sign In</span>

          <form autocomplete="off" @submit.prevent="onLoginBtnClick">
            <!-- Username -->
            <FeatherInput
              data-test="username-input"
              autocomplete="new-username"
              ref="usernameInput"
              label="Username" 
              v-model="username"  
              :error="usernameError"
              autofocus
            />

            <!-- Password -->
            <FeatherProtectedInput
              data-test="password-input"
              autocomplete="new-password"
              label="Password" 
              v-model="password"
              :error="passwordError"
            />
            
            <!-- Remember Me Checkbox -->
            <FeatherCheckbox
              v-model="rememberMe"
              class="remember-me">
              Remember Me
            </FeatherCheckbox>

            <!-- Login -->
            <FeatherButton
              type="submit"
              primary>
              Sign In
            </FeatherButton>
          </form>

          <!-- Forgot Password Link -->
          <span class="forgot-password">
            Forgot Password
          </span>

          <div class="footer">
            <span>
              <span class="terms">Terms of Service</span>
              and
              <span class="privacy">Privacy Policy</span>  
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Logo from '@/assets/Logo.vue'
import { animateGradient, killGradient } from '@/helpers/gradient'
import { useAuthStore } from '@/store/authStore'

const authStore = useAuthStore()

const usernameInput = ref()
const username = ref('')
const password = ref('')
const usernameError = ref()
const passwordError = ref()
const storedRememberMe = JSON.parse(localStorage.getItem('remember-me') || 'false')
const rememberMe = useStorage('remember-me', storedRememberMe)

const onLoginBtnClick = () => {
  // check for valid username
  if (!username.value) {
    usernameError.value = 'Username is required.'
  } else {
    usernameError.value = undefined
  }

  // check for valid password
  if (!password.value) {
    passwordError.value = 'Password is required.'
  } else {
    passwordError.value = undefined
  }

  // submit if form is valid
  if (!usernameError.value && !passwordError.value) {
    authStore.login(username.value, password.value)

    // if remember me is selected
    if (rememberMe.value) {
      localStorage.setItem('hs-u', username.value)
      localStorage.setItem('hs-p', password.value)
    } else {
      localStorage.setItem('hs-u', '')
      localStorage.setItem('hs-p', '')
    }
  }
}

onMounted(() => {
  if (rememberMe.value) {
    username.value = localStorage.getItem('hs-u') || ''
    password.value = localStorage.getItem('hs-p') || ''
  }

  animateGradient()
})

onUnmounted(() => killGradient())
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/typography";

.container {
  position: relative;
  height: calc(100vh - 115px);

  .login-container {
    z-index: 2;
    display: flex;
    flex-direction: column;
    margin: auto;
    width: 448px;
    height: 504px;
    position: absolute;
    top: calc(50% - 230px);
    left: 50%;
    transform: translateX(-50%);
    background: var($surface);
    padding: 30px 66px 0px 66px;
    border: 1px solid var($shade-4);
    border-radius: 4px;

    .logo {
      width: 10.5em;
      margin-left: auto;
      margin-right: auto;
      fill: var($primary-text-on-surface);
    }

    .hs-title {
      @include button;
      text-align: center;
      margin-bottom: 43px;
    }

    .sign-in-title {
      @include headline1;
      text-align: center;
      margin-bottom: 20px;
    }

    .remember-me {
      margin-bottom: 12px;
    }
    
    button {
      width: 100%;
      margin-bottom: 5px;
    }

    .forgot-password {
      color: var($primary);
      display: flex;
      justify-content: flex-end;
      cursor: pointer;
    }

    .footer {
      font-weight: 10px;
      position: absolute;
      bottom: 0px;
      width: 100%;
      left: 0px;
      text-align: center;
      height: 35px;
      border-top: 1px solid var($shade-4);
      color: var($shade-1);

      .terms, .privacy {
        font-weight: 600;
        line-height: 2.2;
        cursor: pointer;
      }
    }
  }
}
</style>
