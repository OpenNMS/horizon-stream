<#import "template.ftl" as layout>
<@layout.emailLayout>
  <p style="color: blue">
    ${kcSanitize(msg("forgotPasswordText"))?no_esc}
  </p>
  <p>
  ${kcSanitize(msg("resetPasswordText", linkExpirationFormatter(linkExpiration)))?no_esc}
  </p>
  <a href="${link}">RESET PASSWORD</a>
</@layout.emailLayout>