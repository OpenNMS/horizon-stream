(function checkTheme() {
  const localStorageTheme = window.localStorage.getItem('theme')

  // check param on the redirectUri for dark mode
  const params = new Proxy(new URLSearchParams(window.location.search), {
    get: (searchParams, prop) => searchParams.get(prop),
  })

  const appUri = params.redirect_uri

  if (appUri){
    isDark = appUri.includes('dark')
  } else {
    isDark = localStorageTheme === 'dark'
  }
  
  if (isDark) {
    // remove the light theme, dark will apply
    const lightStyle = document.querySelector('link[href$="open-light.css"]')
    if (lightStyle) lightStyle.remove()
  }

  // save to localStorage
  window.localStorage.setItem('theme', isDark ? 'dark' : 'light')
}())