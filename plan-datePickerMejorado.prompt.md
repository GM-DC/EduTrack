# Plan: DatePicker mejorado — campo Mes numérico + validación reforzada

> **Estado actual:** `DatePickerDialog`, `DatePickerField` y `DateState` ya existen y funcionan.  
> **Objetivo:** sustituir el dropdown de Mes por un `OutlinedTextField` numérico, alinear validaciones y añadir `MAX_YEAR`.

---

## Diseño objetivo

```
┌─────────────────────────────┐
│      Selecciona fecha       │
├─────────────────────────────┤
│  ┌─────────────────────┐   │
│  │   Lun, 28 Abr 2026  │   │  ← Preview dinámico (toFriendlyString)
│  └─────────────────────┘   │
│                             │
│  [ Día ]  [ Mes ]  [ Año ] │  ← Tres OutlinedTextField numéricos
│    28       04      2026    │
│                             │
├─────────────────────────────┤
│  Cancelar          Aplicar  │
└─────────────────────────────┘
```

---

## Archivos a modificar

| Archivo | Tipo de cambio |
|---|---|
| `DateState.kt` | Añadir `MAX_YEAR`, actualizar `isValid()`, eliminar `MESES_ES` |
| `DatePickerDialog.kt` | Reemplazar dropdown de Mes por TextField numérico |

**Sin cambios necesarios** — se benefician automáticamente al mejorar `DatePickerDialog`:

| Archivo | Feature | Campo(s) de fecha |
|---|---|---|
| `EventFormDialog.kt` | `calendar` | `Fecha *` |
| `PersonalFormDialog.kt` | `calendar` | `Fecha *` |
| `ClaseFormDialog.kt` | `courses` | `Inicio *` + `Fin` |
| `ExamenFormDialog.kt` | `courses` | `Fecha *` |
| `TareaFormDialog.kt` | `courses` | `Fecha límite *` |
| `RecurrencePickerDialog.kt` | `courses` | `Hasta fecha` |

---

## Comportamiento por defecto: fecha actual

Cuando el diálogo se abre **sin fecha previa** (`initial = null`), los tres campos deben inicializarse con la **fecha de hoy**, obtenida a través de `Clock.System.now()` de `kotlinx-datetime`.

Este comportamiento ya existe parcialmente en `DatePickerDialog.kt`:
```kotlin
val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
val resolved  = initial ?: DateState(
    day   = todayDate.dayOfMonth,
    month = todayDate.monthNumber,
    year  = todayDate.year
)
```

Al introducir `monthText` en la Fase 2, hay que asegurarse de que **inicialice también con `resolved.month`**, igual que ya hacen `dayText` y `yearText`:

```kotlin
var dayText   by remember { mutableStateOf(resolved.day.toString().padStart(2, '0')) }
var monthText by remember { mutableStateOf(resolved.month.toString().padStart(2, '0')) }  // ← nuevo
var yearText  by remember { mutableStateOf(resolved.year.toString()) }
```

Así, al abrir el picker sin fecha, el usuario ve directamente la fecha de hoy lista para editar en lugar de un campo vacío.

---

## Fase 1 — `DateState.kt`

**Ruta:** `composeApp/src/webMain/kotlin/org/owlcode/edutrack/features/calendar/components/DateState.kt`

### Cambios

1. **Eliminar** `MESES_ES` (solo se usaba en el dropdown que se elimina; verificado con grep).

2. **Añadir** la constante de año máximo:
   ```kotlin
   const val MAX_YEAR = 2100
   ```

3. **Actualizar** `isValid()` para incluir el límite superior de año:
   ```kotlin
   fun DateState.isValid(): Boolean {
       if (month !in 1..12 || day < 1 || year !in 1900..MAX_YEAR) return false
       val maxDay = when (month) {
           2    -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
           4, 6, 9, 11 -> 30
           else -> 31
       }
       return day <= maxDay
   }
   ```

4. **Añadir** función auxiliar `daysInMonth()` para reutilizar la lógica bisexto y simplificar `isValid()`:
   ```kotlin
   fun DateState.daysInMonth(): Int = when (month) {
       2    -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
       4, 6, 9, 11 -> 30
       else -> 31
   }
   ```

---

## Fase 2 — `DatePickerDialog.kt`

**Ruta:** `composeApp/src/webMain/kotlin/org/owlcode/edutrack/features/calendar/components/DatePickerDialog.kt`

### Cambios

1. **Reemplazar** el estado `monthExpanded` por `monthText`:
   ```kotlin
   var monthText by remember { mutableStateOf(resolved.month.toString().padStart(2, '0')) }
   ```

2. **Ampliar el `LaunchedEffect`** para incluir `monthText` y sincronizar los tres campos:
   ```kotlin
   LaunchedEffect(dayText, monthText, yearText) {
       val d = dayText.toIntOrNull()   ?: return@LaunchedEffect
       val m = monthText.toIntOrNull() ?: return@LaunchedEffect
       val y = yearText.toIntOrNull()  ?: return@LaunchedEffect
       draft = draft.copy(day = d, month = m, year = y)
   }
   ```

3. **Reemplazar** el bloque `ExposedDropdownMenuBox` por un `OutlinedTextField` numérico:
   ```kotlin
   // Mes
   OutlinedTextField(
       value           = monthText,
       onValueChange   = { monthText = it.filter(Char::isDigit).take(2) },
       label           = { Text("Mes") },
       singleLine      = true,
       isError         = monthText.toIntOrNull()?.let { it !in 1..12 } ?: true,
       textStyle       = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
       keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
       modifier        = Modifier.weight(1f)
   )
   ```

4. **Ajustar pesos** de los tres campos:
   - Día: `Modifier.weight(1f)`
   - Mes: `Modifier.weight(1f)`
   - Año: `Modifier.weight(1.5f)` (necesita 4 dígitos)

5. **Mejorar `isError` del campo Día** con validación real según el mes:
   ```kotlin
   isError = dayText.toIntOrNull()?.let { it !in 1..draft.daysInMonth() } ?: true
   ```

6. **Limpiar imports**: eliminar `ExperimentalMaterial3Api`, referencias a `MESES_ES`
   y los imports de `ExposedDropdownMenu*`.

---

## Orden de implementación

```
1. DateState.kt        → cambios de modelo (sin riesgo)
2. DatePickerDialog.kt → cambio de UI (depende de DateState actualizado)
```

---

## Criterios de aceptación

- [ ] Los tres campos son `OutlinedTextField` numéricos visualmente uniformes.
- [ ] El preview se actualiza en tiempo real al cambiar cualquiera de los tres campos.
- [ ] `isError` se activa correctamente en cada campo:
  - Día: fuera del rango real del mes (ej. 31 en Abril → error).
  - Mes: fuera de `1..12`.
  - Año: fuera de `1900..2100`.
- [ ] El botón **Aplicar** está deshabilitado mientras `!isValid()`.
- [ ] Al abrir sin fecha previa (`initial = null`), los tres campos muestran la **fecha de hoy** por defecto.
- [ ] El campo Mes inicializa con el valor de `resolved.month` en formato `"04"` (usando `initial` si se pasa, o la fecha actual si no).
- [ ] El preview muestra `"Fecha invalida"` mientras algún campo sea inválido.
- [ ] `EventFormDialog`, `PersonalFormDialog` y `TareaFormDialog` funcionan sin cambios.
- [ ] `MESES_ES` eliminado y no queda dead code.
- [ ] Los 6 formularios consumidores (`EventFormDialog`, `PersonalFormDialog`, `ClaseFormDialog`, `ExamenFormDialog`, `TareaFormDialog`, `RecurrencePickerDialog`) funcionan sin ningún cambio y muestran el nuevo picker mejorado.
- [ ] Todo campo de fecha en el proyecto (fecha, inicio, fin, límite, hasta) usa exclusivamente `DatePickerField`.

---

## Riesgos y consideraciones

| Riesgo | Mitigación |
|---|---|
| `monthText` válido pero mes inválido para el día actual | `LaunchedEffect` solo actualiza `draft` si los tres valores son `Int`; `isValid()` bloquea el botón Aplicar |
| Emoji "📅" en `DatePickerField` puede no renderizar en todos los navegadores | Fuera de scope; considerar `Icon(Icons.Default.DateRange)` en el futuro |
| `MAX_YEAR` fijo vs. dinámico | Se usa `2100` (fijo, predecible para contexto académico) |

---

---

## Cobertura universal: `DatePickerField` como estándar en todo el proyecto

`DatePickerField` es el **único componente autorizado** para cualquier campo de tipo fecha (fecha, fecha inicio, fecha fin, fecha límite, fecha hasta). **Queda prohibido** usar `OutlinedTextField` libre o cualquier otro control para capturar fechas.

### Inventario actual de usos (ya correcto ✅)

| Componente | Feature | Campo(s) | Estado |
|---|---|---|---|
| `EventFormDialog.kt` | `calendar` | `Fecha *` | ✅ usa `DatePickerField` |
| `PersonalFormDialog.kt` | `calendar` | `Fecha *` | ✅ usa `DatePickerField` |
| `ClaseFormDialog.kt` | `courses` | `Inicio *` + `Fin` | ✅ usa `DatePickerField` × 2 |
| `ExamenFormDialog.kt` | `courses` | `Fecha *` | ✅ usa `DatePickerField` |
| `TareaFormDialog.kt` | `courses` | `Fecha límite *` | ✅ usa `DatePickerField` |
| `RecurrencePickerDialog.kt` | `courses` | `Hasta fecha` | ✅ usa `DatePickerField` |

Todos los consumidores ya están correctamente conectados. Al mejorar `DatePickerDialog` (Fase 2), **la mejora se propaga automáticamente** a los 6 formularios sin tocar ninguno de ellos.

### Regla para futuros formularios

Siempre que un formulario nuevo necesite capturar una fecha:

```kotlin
// ✅ Correcto
DatePickerField(
    value          = miFecha,            // "YYYY-MM-DD" o ""
    onDateSelected = { miFecha = it },
    label          = "Fecha inicio *",   // etiqueta descriptiva
    isError        = miError,
    modifier       = Modifier.fillMaxWidth()   // o .weight(1f) si va en Row
)

// ❌ Incorrecto — nunca capturar fechas con texto libre
OutlinedTextField(value = miFecha, onValueChange = { miFecha = it }, label = { Text("Fecha") })
```

### Patrón estándar para fecha inicio + fecha fin en `Row`

```kotlin
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    DatePickerField(
        value          = startDate,
        onDateSelected = { startDate = it; startDateError = false },
        label          = "Inicio *",
        isError        = startDateError,
        modifier       = Modifier.weight(1f)
    )
    DatePickerField(
        value          = endDate,
        onDateSelected = { endDate = it },
        label          = "Fin",
        modifier       = Modifier.weight(1f)
    )
}
```

---

## Referencias de código

- [`DateState.kt`](composeApp/src/webMain/kotlin/org/owlcode/edutrack/features/calendar/components/DateState.kt)
- [`DatePickerDialog.kt`](composeApp/src/webMain/kotlin/org/owlcode/edutrack/features/calendar/components/DatePickerDialog.kt)
- [`DatePickerField.kt`](composeApp/src/webMain/kotlin/org/owlcode/edutrack/features/calendar/components/DatePickerField.kt)
- [`ClaseFormDialog.kt`](composeApp/src/webMain/kotlin/org/owlcode/edutrack/features/courses/components/ClaseFormDialog.kt) — ejemplo de `fecha inicio + fecha fin`
- [`RecurrencePickerDialog.kt`](composeApp/src/webMain/kotlin/org/owlcode/edutrack/features/courses/components/RecurrencePickerDialog.kt) — ejemplo de `fecha hasta`

