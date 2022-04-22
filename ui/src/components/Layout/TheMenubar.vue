  
<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app">
    <template v-slot:left>
      <FeatherAppBarLink :icon="Logo" title="Home" type="home" url="/" />
    </template>

    <template v-slot:right>
      <FeatherIcon
        :icon="LightDarkMode"
        class="pointer light-dark"
        @click="toggleDark()"
      />
    </template>
  </FeatherAppBar>
</template>
    
<script setup lang="ts">
import { FeatherAppBar, FeatherAppBarLink } from '@featherds/app-bar'
import { FeatherIcon } from '@featherds/icon'
import LightDarkMode from '@featherds/icon/action/LightDarkMode'
import Logo from '@/assets/TheLogo.vue'

const isDark = useDark({
  selector: 'body',
  attribute: 'class',
  valueDark: 'open-dark',
  valueLight: 'open-light',
})

const toggleDark = useToggle(isDark)
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
</style>

<style lang="scss">
@import "@featherds/styles/themes/open-mixins";
body {
  background: var($background);
}
.open-light {
  @include open-light;
}
.open-dark {
  @include open-dark;
}
.light-dark {
  font-size: 24px;
  margin-top: 2px;
}
</style>
  