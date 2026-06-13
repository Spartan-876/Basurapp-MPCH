# File Tree: Basurapp-MPCH

**Generated:** 13/6/2026, 13:16:21
**Root Path:** `c:\Users\johnc\Desktop\John\ciclo VII\Diseno de productos y servicios\Basurapp-MPCH`

```
├── Backend
│   ├── .mvn
│   │   └── wrapper
│   │       └── maven-wrapper.properties
│   ├── FotosReportes
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com
│   │   │   │       └── utp
│   │   │   │           └── Basurapp
│   │   │   │               ├── admin
│   │   │   │               │   ├── controller
│   │   │   │               │   │   ├── AdminAlertaController.java
│   │   │   │               │   │   ├── AdminAuthController.java
│   │   │   │               │   │   ├── AdminCamionController.java
│   │   │   │               │   │   ├── AdminCiudadanoController.java
│   │   │   │               │   │   └── AdminReporteController.java
│   │   │   │               │   ├── dto
│   │   │   │               │   │   ├── AdminLoginRequest.java
│   │   │   │               │   │   ├── AdminRegisterRequest.java
│   │   │   │               │   │   ├── AlertaManualRequest.java
│   │   │   │               │   │   ├── CamionEstadoRequest.java
│   │   │   │               │   │   └── ReporteEstadoRequest.java
│   │   │   │               │   └── service
│   │   │   │               │       └── AdminService.java
│   │   │   │               ├── app
│   │   │   │               │   ├── controller
│   │   │   │               │   │   ├── AuthController.java
│   │   │   │               │   │   ├── CamionController.java
│   │   │   │               │   │   ├── ReportController.java
│   │   │   │               │   │   └── UsuarioController.java
│   │   │   │               │   └── service
│   │   │   │               │       ├── AlertaService.java
│   │   │   │               │       └── CamionAlertScheduler.java
│   │   │   │               ├── common
│   │   │   │               │   ├── config
│   │   │   │               │   │   ├── AdminInitializer.java
│   │   │   │               │   │   ├── DataInitializer.java
│   │   │   │               │   │   ├── FirebaseConfig.java
│   │   │   │               │   │   ├── JacksonConfig.java
│   │   │   │               │   │   ├── JwtAuthFilter.java
│   │   │   │               │   │   ├── JwtUtil.java
│   │   │   │               │   │   └── SecurityConfig.java
│   │   │   │               │   ├── dto
│   │   │   │               │   │   ├── FamiliarDTO.java
│   │   │   │               │   │   └── UsuarioDTO.java
│   │   │   │               │   ├── model
│   │   │   │               │   │   ├── Admin.java
│   │   │   │               │   │   ├── Distrito.java
│   │   │   │               │   │   ├── Familiar.java
│   │   │   │               │   │   ├── Reporte.java
│   │   │   │               │   │   └── Usuario.java
│   │   │   │               │   └── repository
│   │   │   │               │       ├── AdminRepository.java
│   │   │   │               │       ├── DistritoRepository.java
│   │   │   │               │       ├── FamiliarRepository.java
│   │   │   │               │       ├── ReporteRepository.java
│   │   │   │               │       └── UsuarioRepository.java
│   │   │   │               └── BasurappApplication.java
│   │   │   └── resources
│   │   │       ├── geojson
│   │   │       │   └── limites_chiclayo.geojson
│   │   │       ├── static
│   │   │       ├── templates
│   │   │       └── application.properties
│   │   └── test
│   │       └── java
│   │           └── com
│   │               └── utp
│   │                   └── Basurapp
│   │                       └── BasurappApplicationTests.java
│   ├── uploads
│   ├── .gitattributes
│   ├── .gitignore
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── PanelAdmin
│   └── ecocix-admin
│       ├── public
│       │   ├── favicon.svg
│       │   └── icons.svg
│       ├── src
│       │   ├── assets
│       │   │   └── ecocix-logo.png
│       │   ├── components
│       │   ├── config
│       │   │   └── axios.ts
│       │   ├── context
│       │   │   └── AuthContext.tsx
│       │   ├── hooks
│       │   ├── layouts
│       │   │   └── AdminLayout.tsx
│       │   ├── pages
│       │   │   ├── AdminUsersPage.tsx
│       │   │   ├── AlertasPage.tsx
│       │   │   ├── CamionesPage.tsx
│       │   │   ├── CiudadanosPage.tsx
│       │   │   ├── DashboardPage.tsx
│       │   │   ├── LiveMapPage.tsx
│       │   │   ├── LoginPage.tsx
│       │   │   └── ReportesPage.tsx
│       │   ├── services
│       │   │   └── api.ts
│       │   ├── types
│       │   │   └── index.ts
│       │   ├── utils
│       │   ├── App.tsx
│       │   ├── index.css
│       │   └── main.tsx
│       ├── .gitignore
│       ├── README.md
│       ├── eslint.config.js
│       ├── index.html
│       ├── package.json
│       ├── pnpm-lock.yaml
│       ├── tsconfig.app.json
│       ├── tsconfig.json
│       ├── tsconfig.node.json
│       └── vite.config.ts
├── RecolectorAPP
│   ├── .gradle
│   │   ├── 9.4.1
│   │   │   ├── checksums
│   │   │   │   ├── checksums.lock
│   │   │   │   ├── md5-checksums.bin
│   │   │   │   └── sha1-checksums.bin
│   │   │   ├── executionHistory
│   │   │   │   ├── executionHistory.bin
│   │   │   │   └── executionHistory.lock
│   │   │   ├── expanded
│   │   │   ├── fileChanges
│   │   │   │   └── last-build.bin
│   │   │   ├── fileHashes
│   │   │   │   ├── fileHashes.bin
│   │   │   │   ├── fileHashes.lock
│   │   │   │   └── resourceHashesCache.bin
│   │   │   ├── vcsMetadata
│   │   │   └── gc.properties
│   │   ├── buildOutputCleanup
│   │   │   ├── buildOutputCleanup.lock
│   │   │   ├── cache.properties
│   │   │   └── outputFiles.bin
│   │   ├── vcs-1
│   │   │   └── gc.properties
│   │   ├── config.properties
│   │   └── file-system.probe
│   ├── .kotlin
│   │   └── sessions
│   ├── app
│   │   ├── .gradle
│   │   │   └── config.properties
│   │   ├── src
│   │   │   ├── androidTest
│   │   │   │   └── java
│   │   │   │       └── com
│   │   │   │           └── utp
│   │   │   │               └── basurapp
│   │   │   │                   └── recolectorapp
│   │   │   │                       └── ExampleInstrumentedTest.kt
│   │   │   ├── main
│   │   │   │   ├── java
│   │   │   │   │   └── com
│   │   │   │   │       └── utp
│   │   │   │   │           └── basurapp
│   │   │   │   │               └── recolectorapp
│   │   │   │   │                   ├── api
│   │   │   │   │                   ├── data
│   │   │   │   │                   ├── service
│   │   │   │   │                   ├── util
│   │   │   │   │                   ├── AjustesFragment.kt
│   │   │   │   │                   ├── CompartirAlertaActivity.kt
│   │   │   │   │                   ├── HistorialFragment.kt
│   │   │   │   │                   ├── HomeFragment.kt
│   │   │   │   │                   ├── LoginActivity.kt
│   │   │   │   │                   ├── MainActivity.kt
│   │   │   │   │                   ├── MyFirebaseMessagingService.kt
│   │   │   │   │                   ├── RegisterActivity.kt
│   │   │   │   │                   ├── ReportarFragment.kt
│   │   │   │   │                   └── SeleccionarUbicacionActivity.kt
│   │   │   │   ├── res
│   │   │   │   │   ├── drawable
│   │   │   │   │   │   ├── bg_info_message.xml
│   │   │   │   │   │   ├── ic_add_photo.xml
│   │   │   │   │   │   ├── ic_arrow_back.xml
│   │   │   │   │   │   ├── ic_arrow_forward.xml
│   │   │   │   │   │   ├── ic_cameraswitch.xml
│   │   │   │   │   │   ├── ic_check_circle.xml
│   │   │   │   │   │   ├── ic_chevron_right.xml
│   │   │   │   │   │   ├── ic_delete.xml
│   │   │   │   │   │   ├── ic_edit.xml
│   │   │   │   │   │   ├── ic_expand_less.xml
│   │   │   │   │   │   ├── ic_fiber_manual_record.xml
│   │   │   │   │   │   ├── ic_help_outline.xml
│   │   │   │   │   │   ├── ic_history.xml
│   │   │   │   │   │   ├── ic_home_marker.xml
│   │   │   │   │   │   ├── ic_info.xml
│   │   │   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   │   │   ├── ic_launcher_foreground.xml
│   │   │   │   │   │   ├── ic_local_shipping.xml
│   │   │   │   │   │   ├── ic_location_on.xml
│   │   │   │   │   │   ├── ic_lock.xml
│   │   │   │   │   │   ├── ic_logo_stitch.png
│   │   │   │   │   │   ├── ic_logo_stitch_64x64.png
│   │   │   │   │   │   ├── ic_mail.xml
│   │   │   │   │   │   ├── ic_map.xml
│   │   │   │   │   │   ├── ic_my_location.xml
│   │   │   │   │   │   ├── ic_notificacion_camion.xml
│   │   │   │   │   │   ├── ic_notifications.xml
│   │   │   │   │   │   ├── ic_notifications_active.xml
│   │   │   │   │   │   ├── ic_person_outline.xml
│   │   │   │   │   │   ├── ic_persona.xml
│   │   │   │   │   │   ├── ic_schedule.xml
│   │   │   │   │   │   ├── ic_send.xml
│   │   │   │   │   │   ├── ic_settings.xml
│   │   │   │   │   │   ├── ic_share.xml
│   │   │   │   │   │   ├── ic_telefono.xml
│   │   │   │   │   │   ├── ic_truck_marker.xml
│   │   │   │   │   │   ├── ic_truck_marker_green.xml
│   │   │   │   │   │   └── ic_truck_marker_orange.xml
│   │   │   │   │   ├── drawable-night
│   │   │   │   │   │   └── bg_info_message.xml
│   │   │   │   │   ├── layout
│   │   │   │   │   │   ├── activity_compartir_alerta.xml
│   │   │   │   │   │   ├── activity_login.xml
│   │   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   │   ├── activity_register.xml
│   │   │   │   │   │   ├── activity_seleccionar_ubicacion.xml
│   │   │   │   │   │   ├── dialog_familiar.xml
│   │   │   │   │   │   ├── fragment_ajustes.xml
│   │   │   │   │   │   ├── fragment_historial.xml
│   │   │   │   │   │   ├── fragment_home.xml
│   │   │   │   │   │   ├── fragment_reportar.xml
│   │   │   │   │   │   ├── item_familiar.xml
│   │   │   │   │   │   ├── item_reporte.xml
│   │   │   │   │   │   └── view_truck_info_window.xml
│   │   │   │   │   ├── menu
│   │   │   │   │   │   └── bottom_nav_menu.xml
│   │   │   │   │   ├── mipmap-anydpi
│   │   │   │   │   │   ├── ic_launcher.xml
│   │   │   │   │   │   └── ic_launcher_round.xml
│   │   │   │   │   ├── values
│   │   │   │   │   │   ├── colors.xml
│   │   │   │   │   │   ├── strings.xml
│   │   │   │   │   │   └── themes.xml
│   │   │   │   │   ├── values-night
│   │   │   │   │   │   ├── colors.xml
│   │   │   │   │   │   └── themes.xml
│   │   │   │   │   └── xml
│   │   │   │   │       ├── backup_rules.xml
│   │   │   │   │       ├── data_extraction_rules.xml
│   │   │   │   │       └── file_paths.xml
│   │   │   │   └── AndroidManifest.xml
│   │   │   └── test
│   │   │       └── java
│   │   │           └── com
│   │   │               └── utp
│   │   │                   └── basurapp
│   │   │                       └── recolectorapp
│   │   │                           └── ExampleUnitTest.kt
│   │   ├── .gitignore
│   │   ├── build.gradle.kts
│   │   ├── local.properties
│   │   └── proguard-rules.pro
│   ├── gradle
│   │   ├── wrapper
│   │   │   ├── gradle-wrapper.jar
│   │   │   └── gradle-wrapper.properties
│   │   └── libs.versions.toml
│   ├── .gitignore
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   ├── local.properties
│   └── settings.gradle.kts
├── ServidorRutas
│   ├── package.json
│   ├── pnpm-lock.yaml
│   ├── ruta_camion.geojson
│   ├── ruta_camion2.geojson
│   └── server.js
└── .gitignore
```

---

_Generated by FileTree Pro Extension_
