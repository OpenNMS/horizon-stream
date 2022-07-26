  
<template>
  <FeatherAppLayout class="feather-styles layout">
    <template v-slot:header>
      <Menubar />
    </template>

    <template v-slot:rail>
      <NavigationRail :modelValue="store.navRailOpen" />
    </template>

    <div class="main-content">
      <Spinner />
      <Snackbar />
      <router-view />
    </div>
  </FeatherAppLayout>
</template>
  
<script setup lang="ts">
import { useLayoutStore } from './store/Views/layoutStore'

// Remove KC redirectUri theme param
const route = useRoute()
const router = useRouter()
if (route.query.theme) router.replace(route.path)

// transition nav rail open / close
const store = useLayoutStore()
const contentMargin = computed(() => store.navRailOpen ? '218px' : '0px')
const ease = computed(() => store.navRailOpen ? '10ms' : '80ms')
const maxWidth = computed(() => store.navRailOpen ? '223px' : '0px')
</script>
  
<style lang="scss">
@import "@featherds/styles/lib/grid";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/open-mixins";
@import "@featherds/table/scss/table";
@import "@featherds/styles/themes/variables";

html {
  overflow-x: hidden;
}

.main-content {
  margin-left: v-bind(contentMargin);
  transition: margin-left 0.28s ease-in-out v-bind(ease);
  max-width: calc(100% - v-bind(maxWidth));

  table {
    width: 100%;
    @include table;
  }

  .data-table tr,
  .data-table div {
    @for $i from 1 through 50 {
    &:nth-child(#{$i}) {
      transition: all 0.3s ease $i * 0.05s;
    }
    }
  }
  .data-table-enter-active,
  .data-table-leave-active {
    transform: translateX(0px);
    opacity:1;
  }
  .data-table-enter-from,
  .data-table-leave-to {
    transform: translateX(30px);
    opacity:0;
  }
}

a {
  text-decoration: none;
  color: var($primary);
}

.pointer {
  cursor: pointer;
}

.bg-ok {
  background-color: var($success);
  color: var($primary-text-on-color);
}
.bg-failed {
  background-color: var($error);
  color: var($primary-text-on-color);
}
.bg-unknown {
    background-color: grey;
    color: var($primary-text-on-color);
}
.open-dark {
  .bg-unknown {
    color: rgb(10, 12, 27);
  }
}
</style>
