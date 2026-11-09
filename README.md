# Arcade UPB

App central con login (contraseña o huella), menú en cruz y un **servicio de scores**,
más 4 juegos que van **dentro del mismo APK** (cada juego es un módulo de librería con su Activity) y le envían su score a ese servicio.
Todo está en Kotlin + Jetpack Compose.

## Módulos (un solo proyecto, 6 módulos)

| Módulo | Qué es | Paquete |
|---|---|---|
| `core` | Librería compartida: contrato AIDL del servicio, `ArcadeClient`, logos, tema y diálogos | `edu.upb.arcade.core` |
| `hub` | **App central**: login, registro, huella, menú en cruz, base de datos y `ArcadeService` | `edu.upb.arcade` |
| `monopoly` | Monopoly con interfaz gráfica y fichas que se arrastran | `edu.upb.arcade.monopoly` |
| `tetris` | Tetris: flechas + arrastrar la ficha + tap para girar | `edu.upb.arcade.tetris` |
| `triqui` | Triqui contra la CPU (score = racha) | `edu.upb.arcade.triqui` |
| `culebrita` | Culebrita controlada con el giroscopio | `edu.upb.arcade.culebrita` |

## Generar el APK (una sola app)
```
.\gradlew.bat :hub:assembleDebug
```
El APK queda en `hub\build\outputs\apk\debug\hub-debug.apk` e incluye el hub y los 4 juegos.

## Cómo correrlo
1. Android Studio → **File › Open** → carpeta `ArcadeUPB` → espera el *Gradle Sync*.
   Si Android Studio ofrece actualizar el Android Gradle Plugin, **no lo actualices**.
2. En el selector de configuraciones (arriba, junto al botón ▶) aparece una por módulo.
   Ejecuta primero **hub** y después **monopoly**, **tetris**, **triqui** y **culebrita**
   (cada uno se instala como app aparte en el mismo emulador/celular).
3. Abre *Arcade UPB*, crea tu cuenta y entra. Desde el menú en cruz se abren los juegos.

### En el emulador
- **Huella**: en el emulador ve a *Settings › Security › Fingerprint* y registra una huella.
  Después, en los *Extended controls* (⋯) → *Fingerprint* → *Touch sensor* para "poner el dedo".
- **GPS**: *Extended controls* → *Location*: elige un punto (p. ej. Bucaramanga) y dale *Set location*.
- **Giroscopio**: *Extended controls* → *Virtual sensors* → mueve/inclina el teléfono virtual.

## Cómo se conectan las apps
```
 Juego (tetris, triqui…)                       Arcade UPB (hub)
 ───────────────────────                       ─────────────────
 ArcadeClient ── bindService(AIDL) ──────────▶ ArcadeService
   guardarScore("tetris", 1200)                  ├─ lee la sesión (SharedPreferences)
   getNickname() / getMejorScore()               ├─ guarda en SQLite (usuarios, scores)
                                                 └─ pide el GPS → Geocoder → "Ciudad, País"
```
- El servicio está protegido con un **permiso de firma** (`edu.upb.arcade.permission.SCORES`):
  solo las apps firmadas con la misma llave (las de este proyecto) pueden usarlo.
- La sesión queda guardada en el teléfono: los juegos guardan a nombre del usuario
  aunque el hub esté cerrado. "Salir" en el menú cierra la sesión.

## Datos guardados (todo local)
- `usuarios`: email, contraseña (**hash PBKDF2 con sal**, nunca en texto plano), nickname,
  fecha de nacimiento y score (suma de todos sus puntos).
- `scores`: juego, puntos, **ciudad (GPS)** y fecha. La ciudad aparece junto al score
  en el menú y en "Últimas partidas".
- La huella la valida Android; la app solo recuerda qué usuario la activó.

## Score de cada juego
| Juego | Score |
|---|---|
| Monopoly | Saldo final del jugador 1 (el usuario conectado) tras 10 rondas |
| Tetris | 100/300/500/800 por 1–4 líneas × nivel, +1 por fila empujada, +2 por caída rápida |
| Triqui | Racha contra la CPU: +100 por victoria, +30 por empate; termina al perder o al "plantarse" |
| Culebrita | +10 por cada comida |

## Controles
- **Tetris**: flechas ◀ ▼ ↻ ▶ y ⤓ (caída rápida). Además, arrastrar el dedo sobre el tablero
  mueve la ficha (el gesto se fija en horizontal **o** vertical) y un **tap la gira**,
  alternando solo entre horizontal y vertical.
- **Culebrita**: inclina el teléfono. Usa `TYPE_GAME_ROTATION_VECTOR` (giroscopio + acelerómetro);
  si el teléfono no tiene giroscopio usa el acelerómetro. "Recalibrar" toma la posición actual como recta.
- **Monopoly**: toca el dado y arrastra tu ficha hasta la casilla que parpadea.

## Pruebas
Cada módulo tiene pruebas unitarias de su lógica (`src/test`): clic derecho sobre la carpeta
`test` → *Run Tests*.
