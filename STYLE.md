STYLE.md — Guía de estilos — Dashboard Sano

Resumen
- Propósito: Directrices reutilizables para la UI del dashboard.
- Archivos: `src/main/resources/templates/index.html`, `src/main/resources/static/css/dashboard.css`

Colores
- Primario: #3B82F6 — acentos y bordes activos.
- Fondo principal: #F1F5F9
- Sidebar: #0F172A
- Textos primarios: #0F172A / #1E293B
- Neutros / bordes: #E2E8F0, #F8FAFC

Tipografía
- Familia: 'Segoe UI', system-ui, -apple-system, sans-serif
- Jerarquía:
  - Títulos principales: 17–18px, peso 700
  - Subtítulos/encabezados de sección: 14–16px, peso 600–700
  - Texto de tabla: 13–14px, peso 500
- Encabezados de tabla: text-transform: uppercase

Layout y espaciado
- Sidebar fijo de 252px + contenido flexible
- Topbar: altura ~65px, padding horizontal 32px
- Gutter principal: 32px
- Bordes: radio 6–10px; tarjetas con border 1px

Componentes
- Sidebar / Navegación:
  - Items con padding 10–14px; borde izquierdo activo en color primario
  - Hover: fondo más oscuro y texto claro
  - Uso de `class="active"` para estado seleccionado
- Topbar: título a la izquierda, etiqueta (badge) a la derecha
- Tabla: encabezado claro, filas con hover sutil
- Badge número oficio: fondo claro + texto azul primario
- Empty state: mensaje centrado con tipografía secundaria

Imágenes / Logo
- Ruta recomendada: `src/main/resources/static/images/logo.png`
- HTML de ejemplo: `<img th:src="@{/images/logo.png}" alt="Logo empresa">`
- Caja logo: 56x56px, radius 10px

Accesibilidad & Responsividad
- Mantener contraste adecuado (WCAG AA si es posible)
- Para mobile: colapsar sidebar como drawer (no implementado aquí)
- Preferir unidades relativas (rem/em) para escalado futuro

Clases CSS principales
- `.sidebar`, `.sidebar-header`, `.sidebar-nav`, `.sidebar-footer`
- `.main`, `.topbar`, `.page-title`, `.page-date`, `.system-tag`
- `.content`, `.section-header`, `.record-count`
- `.table-card`, `.oficio-badge`, `.empty-state`

Integración Thymeleaf
- Controlador debe pasar `fecha` y `oficios` al modelo.
- Fragmentos reutilizables: `templates/fragments/sidebar.html` (ver fragmento `sidebar(active, logoUrl)`).

Buenas prácticas
- Mantener separación: estructura en templates, estilos en `static/css`.
- Centralizar colores si se migra a SASS/variables.
- Evitar iconografía automática; usar imágenes de marca cuando sea necesario.

Ejemplo de controlador (Java)
```java
@GetMapping("/")
public String index(Model model) {
    model.addAttribute("fecha", LocalDate.now()
        .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es"))));
    model.addAttribute("oficios", servicioOficios.listar());
    model.addAttribute("active","asignar"); // para marcar el nav activo
    return "index";
}
```

Uso del fragmento
- Incluir en plantilla: `th:replace="fragments/sidebar :: sidebar(active=${active}, logoUrl='\/images\/logo.png')"`

---
Puedes pedirme que exporte esto a otro formato o que agregue variables SASS y tokens de diseño.