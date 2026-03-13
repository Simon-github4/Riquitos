# End-to-End Tests - Riquitos

## Descripción

Este directorio contiene pruebas End-to-End (E2E) para la aplicación Riquitos, utilizando **Playwright** para automatizar pruebas de navegador.

## Requisitos

- Java 21
- Maven
- La aplicación debe estar ejecutándose en `http://localhost:8080`

## Ejecución

```bash
# Primero, inicia la aplicación en una terminal:
./mvnw spring-boot:run

# En otra terminal, ejecuta las pruebas E2E:
./mvnw test -Dtest=LoginFlowE2ETest
```

## Tests Incluidos

### LoginFlowE2ETest.java

1. **test_admin_login_and_navigation** - Verifica que el usuario admin puede iniciar sesión y es redirigido al dashboard
2. **test_invalid_login_shows_error** - Verifica que credenciales inválidas muestran un mensaje de error
3. **test_operario_login_and_view_products** - Verifica que un operario puede login y navegar a la vista de productos

## Estructura de un test E2E con Playwright

```java
@Test
void test_nombre() {
    // 1. Navegar a la página
    page.navigate("http://localhost:8080");
    
    // 2. Interactuar con elementos
    page.fill("#username", "admin");
    page.fill("#password", "admin");
    page.click("button[type=\"submit\"]");
    
    // 3. Verificar resultados
    assertTrue(page.url().contains("dashboard"));
}
```

## Notas

- Las pruebas usan headless mode por defecto
- Los selectores CSS pueden requerir ajuste según la UI de Vaadin
- Para debugging, cambiar `setHeadless(true)` a `false` en el setup
