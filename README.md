
###########################################################################################################################################################
|    ---- Nombre del proyecto: Mis Lugares. ---                                                                                                           |
|    ---- Descripción: Aplicación Android para registrar, visualizar y gestionar lugares de interés usando Google Maps y una base local SQLite. ----      |
###########################################################################################################################################################


### 1. ¿Qué hace el proyecto? ###

MisLugares es una aplicación móvil para Android que permite al usuario guardar, visualizar y gestionar distintos lugares, como restaurantes, bares, instituciones educativas, etc. La aplicación almacena información básica y muestra la ubicación exacta en un mapa interactivo utilizando Google Maps.

**Propósito:**
El propósito principal es ofrecer una herramienta sencilla e intuitiva para que cualquier persona pueda:

- Registrar lugares que desea recordar.
- Consultarlos rápidamente.
- Ver su localización exacta en un mapa.
- Acceder a una lista organizada de todos sus puntos guardados.


**Contexto:**
MisLugares surge como una solución práctica para personas que desean guardar y consultar lugares importantes sin depender de aplicaciones pesadas, cuentas en la nube o funciones innecesariamente complejas.
Muchas apps actuales requieren iniciar sesión, sincronizar datos o navegar por menús extensos para realizar tareas simples como guardar un restaurante, una cafetería o un punto de interés.
El proyecto se desarrolla como parte de un ejercicio académico en Android Studio, integrando varias tecnologías modernas del entorno Android como Activities, RecyclerView, SQLite y Google Maps, manteniendo un equilibrio entre una arquitectura clara y una implementación accesible.


**Problema que resuelve:**
Muchas apps para guardar lugares son complejas o requieren una cuenta en la nube. MisLugares resuelve ese problema al ofrecer una forma:

- Local.
- Privada.
- Simple.
para gestionar ubicaciones importantes sin necesidad de internet (excepto para cargar el mapa).


**Alcance:**
El proyecto permite:

- Guardar lugares.
- Listarlos en una interfaz tipo agenda.
- Visualizarlos en un mapa (Google Maps).
- Abrir y navegar entre pantallas.

No incluye:
- Edición de lugares guardados.
- Eliminación de lugares.
- Bases de datos avanzadas.
- Sincronización en la nube.



### 2. ¿Cómo funciona internamente? (Arquitectura del proyecto). ###
**Arquitectura general del sistema:**
La aplicación MisLugares está construida siguiendo una arquitectura sencilla basada en:

- Activities como controladores principales de cada pantalla.
- Repositorios para manejar los datos de los lugares.
- Clases modelo que representan cada lugar.
- Adaptadores (RecyclerView) para mostrar listas dinámicas.
- Google Maps API para mostrar mapas interactivos.

La estructura sigue un patrón MVC simplificado:
- Modelo: Lugar, TipoLugar, datos y repositorios.
- Vista: Layouts XML, RecyclerView, Google Maps.
- Controlador: Activities como MainActivity, VistaLugarActivity, EdicionLugarActivity.


**Estructura de carpetas del proyecto:**
MisLugares/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/misLugares/
│   │   │   │   ├── presentacion/          -> Activities y pantallas del usuario.
│   │   │   │   ├── datos/                 -> Repositorios y manejo de datos.
│   │   │   │   ├── casos_uso/             -> Lógica de uso (Casos de uso).
│   │   │   │   ├── modelo/                -> Clases de modelo (Lugar, TipoLugar).
│   │   │   │   └── Aplicacion.java        -> Inicialización global del repositorio.
│   │   │   ├── res/                       -> Recursos XML.
│   │   │   │   ├── layout/                -> Diseños de pantallas.
│   │   │   │   ├── menu/                  -> Menús de opciones.
│   │   │   │   ├── values/                -> Strings, estilos, arrays.
│   │   │   │   └── xml/                   -> Preferencias.
│   │   │   └── AndroidManifest.xml
│   │   └── test/                          -> Tests.
│   └── build.gradle.kts                   -> Configuración Gradle.
│
├── gradle/                                -> Configuración automática.
└── README.md                              -> Documentación del proyecto.


**Tecnologías y librerías usadas:**
Esta aplicación utiliza las siguientes herramientas:

- Android / UI
- Android SDK 36
- minSdk 24
- targetSdk 36
- Activities & Intents
- RecyclerView para listas
- Adapters personalizados
- ViewBinding para acceder a vistas


**Dependencias principales:**
El proyecto utiliza varias librerías oficiales de Android para garantizar eficiencia, compatibilidad y una buena experiencia de usuario:

- Librería / Tecnología	Uso en el proyecto:
  - AppCompat: Compatibilidad con temas modernos en versiones antiguas de Android.
  - Material Design Components:	Botones, tarjetas, colores y estilos visuales.
  - ConstraintLayout: Diseño flexible y optimizado de las pantallas.
  - RecyclerView: Lista de lugares mostrando todos los elementos de manera eficiente.
  - Preference Library:	Manejo de configuración desde Ajustes.
  - Google Maps API (play-services-maps:18.2.0): Mostrar la ubicación del lugar seleccionado usando Google Maps.

- Servicios externos:
  - Google Play Services Maps: Permite integrar mapas interactivos dentro de la actividad MapsActivity.

- Tecnologías internas del framework Android:
  - Activities (Home, Detalles, MapsActivity): para la navegación entre pantallas.
  - Intents: permiten pasar información entre actividades (nombre, descripción, coordenadas).
  - ViewBinding: evita errores y asegura acceso seguro a los views.
  - Layouts XML → diseño visual del proyecto.


**Resumen técnico del sistema:**
MisLugares utiliza una arquitectura MVC simplificada donde las Activities funcionan como controladores, los layouts XML como vistas y las clases del paquete `modelo` como la capa de datos.  
La interacción principal fluye así:

1. UI (Activities). 
   Muestran listas, formularios y mapas. Reciben eventos del usuario.

2. Casos de uso ("casos_uso").
   Encapsulan la lógica de negocio:
   - Editar lugar.
   - Guardar cambios.
   - Abrir mapa.
   - Compartir.
   - Borrar elementos.

3. Capa de datos ("datos"). 
   Gestiona el acceso a SQLite mediante:
   - "LugaresBDAdapter" (acceso de bajo nivel).
   - "RepositorioLugares" (acceso de alto nivel).

4. Modelo (modelo).  
   Define las clases "Lugar" y "TipoLugar".

La aplicación inicializa su repositorio global en "Aplicacion.java", permitiendo que todas las Activities accedan al mismo origen de datos.  
Google Maps API se utiliza únicamente en "VistaMapaActivity" para mostrar ubicaciones.



**Comunicación entre módulos:**
Capa Presentación (/presentacion).
Activities -> Casos de uso -> Datos -> Modelo.

- MainActivity: Muestra el listado de lugares desde SQLite.
 - Usa RepositorioLugares (indirectamente a través de Aplicacion).
 - Navega a VistaLugarActivity pasando posición o ID.

- VistaLugarActivity: Muestra la información completa de un lugar.
 - Llama métodos de CasosUsoLugar:
   - abrir mapa.
   - llamar por teléfono.
   - ver URL.
   - compartir.
   - borrar.
   - editar (abre EdicionLugarActivity).

- EdicionLugarActivity: Permite modificar datos del lugar.
 - Recibe el ID desde VistaLugarActivity.
 - Usa:
   - lugares = ((Aplicacion) getApplication()).lugares
   - usoLugar.guardar(_id, lugar)

- VistaMapaActivity: Usa Google Maps para mostrar la ubicación de un lugar.

- AcercaDeActivity: Información estática.

- PreferenciasActivity: Carga preferencias desde XML usando androidx.preference.

Capa de Lógica (/casos_uso).
- CasosUsoLugar: Es la capa que conecta UI ↔ datos.
 - Sus funciones habituales:
   - mostrar(int id).
   - editar.
   - guardar(id, lugar).
   - borrar(id).
   - multimedia (cámara/galería).
   - abrir mapa.
   - compartir.

Todas las Activities se llaman aquí para no mezclar lógica con interfaz.

Capa de Datos (/datos)
- LugaresBDAdapter: Administra la base SQLite.
 - Opera a bajo nivel:
   - insertar.
   - actualizar.
   - eliminar.
   - obtener id

- RepositorioLugares: Capa intermedia entre la BD y el resto de la app.
 - Ofrece métodos de alto nivel:
   - elemento(id).
   - elementoPos(pos).
   - nuevo().
   - borrar(id).

Capa Modelo (/modelo)
- Lugar: Clase que representa un lugar real con:
  - nombre.
  - dirección.
  - tipo (enum).
  - comentario.
  - latitud/longitud.
  - teléfono.
  - URL.
  - foto.
  - valoración.

- TipoLugar: Enum que define tipos predefinidos con nombre e ícono.

- Clase Aplicacion: Se ejecuta antes que cualquier Activity.
 - Crea el objeto lugares que es la BD principal.
- Permite compartir el repositorio en toda la app.

Flujo de comunicación:
  - Usuario -> Activity (UI).
            -> CasosUsoLugar (lógica).
            -> RepositorioLugares (datos).
            -> Activity actualiza UI.

Diagrama de arquitectura:
                *************************
                |     Usuario           |
                *************************
                       |
                       v
                *************************
                |     Activities        |
                | (UI y navegación)     |
                *************************
                       |
                       v
                *************************
                |  CasosUsoLugar        |
                | (lógica de uso)       |
                *************************
                       |
                       v
                *************************
                | RepositorioLugar      |
                | (almacenamiento)      |
                *************************
                       |
                       v
                *************************
                |     Modelo            |
                | (Lugar/tipos)         |
                *************************


### Diagrama de secuencia. ###
**Diagrama de secuencia — Agregar un nuevo lugar.

Usuario → MainActivity → CasosUsoLugar → RepositorioLugares → SQLite → MainActivity

 1. El usuario presiona el botón "+" en MainActivity.
 2. MainActivity llama a "CasosUsoLugar.nuevo()".
 3. CasosUsoLugar solicita al "RepositorioLugares" crear un nuevo registro vacío.
 4. RepositorioLugares llama a "LugaresBDAdapter.insertar()" para guardar el lugar en SQLite.
 5. SQLite devuelve el ID del nuevo lugar.
 6. MainActivity abre "EdicionLugarActivity" enviando el ID recién creado.
 7. El usuario completa los datos y presiona Guardar.
 8. EdicionLugarActivity llama a "CasosUsoLugar.guardar(id, lugar)".
 9. CasosUsoLugar actualiza los datos llamando a "RepositorioLugares.actualizar(id, lugar)".
 10. RepositorioLugares modifica el registro en SQLite.
 11. Se regresa a MainActivity y se refresca la lista.



### 3. ¿Cómo se usa o contribuye alguien más? ###
Esta guía explica cómo ejecutar el proyecto MisLugares en otro equipo, ya sea para uso personal o para colaborar en su desarrollo.

**Requisitos previos.**
 - Antes de instalar el proyecto, asegúrate de contar con lo siguiente:
   - Android Studio.
   - JDK 11 (Android Studio ya lo incluye).
   - SDK Platform 36.
   - Mínimo 3 GB de espacio libre para evitar complicaciones.

Un dispositivo Android físico o un emulador configurado.


**Cómo clonar o descargar el proyecto.**
 - Abrir el proyecto en Android Studio
   1. Descarga el archivo comprimido perteneciente a la aplicación.
   2. Descomprime el proyecto en una carpeta local.


**Instalación paso a paso.**
 1. Previamente descargar o realizar la clonación del proyecto.
 2. Abrir Android Studio.
 3. Seleccionar "File", luego la opción "Open".
 4. Elegir la carpeta raíz del proyecto (donde está la carpeta "app/").
 5. Esperar a que Android Studio sincronice Gradle.
 6. Conectar un dispositivo Android o abrir un emulador.
 7. Presionar "Run" para ejecutar la aplicación.


**Instalar dependencias.**
Las dependencias se encuentran definidas en:
app/build.gradle.kts

Android Studio las instala automáticamente.
Si ocurre un error, usa:
File -> Sync Project with Gradle Files.

**Configurar API de rutas (GraphHopper).**
Las rutas (coche, bici, a pie) usan la API de GraphHopper, que soporta senderos y distintos modos de transporte.
1. Regístrate en https://graphhopper.com/dashboard y crea una API key.
2. En la raíz del proyecto crea o edita `local.properties` y añade:
   `graphhopperApiKey=TU_API_KEY_DE_GRAPHHOPPER`
3. Sincroniza el proyecto con Gradle. La app usará esta clave para calcular rutas.


**Ejecutar el proyecto (entorno local).**
  - Conectar un teléfono Android o iniciar un emulador.
  - En la barra superior, seleccionar el dispositivo de ejecución.
  - Dar clic en Run.
  - La app se instalará y abrirá automáticamente.


**Tests incluidos.**
El proyecto incluye un test instrumentado generado automáticamente por Android Studio:

 - Ubicación: app/src/androidTest/java/com/example/misLugares/ExampleInstrumentedTest.java
 - Tipo: Test instrumentado ejecutado en un dispositivo/emulador Android.
 - Framework:
   - JUnit4.
   - AndroidX Test.

 - Descripción:
   Este test únicamente comprueba que el package name de la aplicación sea correcto.
   No evalúa lógica interna, vistas, repositorios ni casos de uso.
   Es un test mínimo creado por defecto y sirve como punto de partida para futuros tests.

- Cómo correr los tests.
 - Para ejecutar los tests instrumentados incluidos:
   1. Abrir Android Studio.
   2. Conectar un dispositivo físico o iniciar un emulador.
   3. Ir al panel Project → app/src/androidTest/java/...
   4. Clic derecho sobre ExampleInstrumentedTest → Run 'ExampleInstrumentedTest'.
Este test valida únicamente el contexto de la aplicación.

- FAQ — Preguntas Frecuentes
  1. ¿Para qué sirve la aplicación MisLugares?
      MisLugares es una aplicación Android que permite registrar, visualizar y gestionar lugares de interés con datos como nombre, dirección, tipo de lugar, comentario y ubicación en mapa.

  2. ¿La aplicación funciona sin internet?
      Sí. Toda la información se almacena localmente mediante una base de datos SQLite.
      Solo el mapa utiliza servicios de Google Maps, por lo que para visualizar el mapa se necesita conexión.

  3. ¿Cómo agrego un nuevo lugar?
     Presiona el botón flotante con el símbolo "+" en la pantalla principal.
     La app abrirá el formulario de edición donde puedes registrar los datos del nuevo lugar.

  4. ¿Puedo editar un lugar existente?
     Sí. Solo toca cualquier lugar de la lista para abrir sus detalles y seleccionar la opción de Editar.

  5. ¿Qué pasa si no doy permisos de localización?
     La app puede seguir funcionando, pero no podrás ver la ubicación de los lugares disponibles.
     Puedes activar el permiso más tarde desde los ajustes del dispositivo.

  6. El mapa no se muestra, ¿qué puedo hacer?
     Verifica lo siguiente:
      - Tener conexión a internet.
      - Tener Google Play Services actualizado.
      - Haber habilitado los permisos de ubicación.

  7. ¿Cómo puedo resetear todos los lugares?
      En la pantalla de edición, si entras a un lugar creado temporalmente, puedes usar la opción Cancelar, pero si deseas limpiar toda la base de datos debes hacerlo manualmente borrando los lugares disponibles.

  8. ¿El proyecto soporta tests avanzados?
     Actualmente solo incluye el test básico generado por Android Studio, pero la arquitectura permite agregar pruebas de UI y pruebas unitarias.

############################################################
|	AUTORES:                                           |
|                                                          |
|	Este proyecto fue desarrollado por:                |
|                                                          |
|	- **Fátima Itzel Rojas Flores 1963043**            |
|	- **Dania Montserrat Pérez Ortiz 1962281**         |
|	- **Antonio Enrique Hernández Ramírez 1948932**    |
|	- **América Lizeth Domínguez Ramírez 1950087**     |
|	                                                   |
|	Universidad Autónoma de Nuevo León.                |  
|	Facultad de Ingeniería Mecánica y Eléctrica.       | 
|	Materia: Ingeniería de Dispositivos Móviles.       |
|                                                          |
############################################################