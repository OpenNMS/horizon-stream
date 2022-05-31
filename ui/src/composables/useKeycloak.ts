import { KeycloakInstance } from '@dsb-norge/vue-keycloak-js/dist/types'

const keycloak = ref<KeycloakInstance>()

const useKeycloak = () => {

  const setKeycloak = (kc: KeycloakInstance) => {
    keycloak.value = kc
  }

  return { keycloak, setKeycloak }
}

export default useKeycloak
