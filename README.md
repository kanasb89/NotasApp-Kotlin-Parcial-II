# NotasApp
Aplicación Android (Kotlin, MVVM, LiveData, Retrofit) + MySql 

## Estructura

```
NotasApp/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/notasapp/
│       │   ├── data/api/        (ApiService, AuthInterceptor, RetrofitClient)
│       │   ├── data/model/      (User, LoginRequest, LoginResponse, NoteRequest, NoteResponse)
│       │   ├── data/repository/ (LoginRepository, NoteRepository)
│       │   ├── ui/login/        (LoginActivity, LoginViewModel)
│       │   ├── ui/dashboard/    (DashboardActivity)
│       │   ├── ui/notes/        (CreateNote*, NotesList*, NotesAdapter)
│       │   └── utils/           (Resource, SessionManager, SafeApiCall, ViewModelFactory, DateFormatter)
│       └── res/                 (layout, values)
└── backend/
    ├── database.sql
    ├── server.js
    ├── package.json
    └── .env
```

## Android

1. Abre la carpeta `NotasApp` en Android Studio (Koala o superior, JDK 17) y sincroniza Gradle.
2. En `RetrofitClient.kt` la `BASE_URL` es `http://10.0.2.2:3000/` (emulador). Para un dispositivo físico usa la IP LAN de tu PC.
3. Ejecuta la app.

## Backend

1. Ejecuta `backend/database.sql` en MySQL.
2. Crea el archivo `.env` ajusta credenciales y `JWT_SECRET`.
3. `npm install` y `npm start`.
4. Creación de usuario por medio de npm o insertar desde la base de datos:

```
curl -X POST http://localhost:3000/register -H "Content-Type: application/json" \
  -d '{"name":"Ana","email":"ana@correo.com","password":"123456"}'
```

## API

| Método | Ruta     | Auth   | Cuerpo                       | Respuesta                         |
|--------|----------|--------|------------------------------|-----------------------------------|
| POST   | /login   | No     | `{email, password}`          | `{token, user:{id,name,email}}`   |
| POST   | /notes   | Bearer | `{title, content}`           | `{id,user_id,title,content,created_at}` (201) |
| GET    | /notes   | Bearer | —                            | `[ {id,user_id,title,content,created_at} ]`   |
