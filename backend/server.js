require('dotenv').config();

const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const mysql = require('mysql2/promise');

const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET;
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '8h';

if (!JWT_SECRET) {
  console.error('Falta la variable JWT_SECRET en el archivo .env');
  process.exit(1);
}

const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  port: Number(process.env.DB_PORT) || 3306,
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'notasapp',
  dateStrings: false,
  timezone: 'Z',
  waitForConnections: true,
  connectionLimit: 10
});

const app = express();
app.use(cors());
app.use(express.json());

//Middleware: valida el token JWT y expone el usuario autenticado en req.user
function authenticateToken(req, res, next) {
  const header = req.headers.authorization || '';
  const [scheme, token] = header.split(' ');

  if (scheme !== 'Bearer' || !token) {
    return res.status(401).json({ message: 'Token de autorización requerido' });
  }

  jwt.verify(token, JWT_SECRET, (error, payload) => {
    if (error) {
      return res.status(401).json({ message: 'Token inválido o expirado' });
    }
    req.user = { id: payload.id, email: payload.email };
    next();
  });
}

//POST /register - alta de usuarios para poder probar la aplicación
app.post('/register', async (req, res) => {
  try {
    const { name, email, password } = req.body || {};
    if (!name || !email || !password) {
      return res.status(400).json({ message: 'Nombre, email y contraseña son obligatorios' });
    }

    const passwordHash = await bcrypt.hash(password, 10);
    const [result] = await pool.execute(
      'INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)',
      [name.trim(), email.trim().toLowerCase(), passwordHash]
    );

    return res.status(201).json({ id: result.insertId, name: name.trim(), email: email.trim().toLowerCase() });
  } catch (error) {
    if (error.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({ message: 'El correo ya está registrado' });
    }
    console.error(error);
    return res.status(500).json({ message: 'Error interno del servidor' });
  }
});

//POST /login
app.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body || {};
    if (!email || !password) {
      return res.status(400).json({ message: 'Email y contraseña son obligatorios' });
    }

    const [rows] = await pool.execute(
      'SELECT id, name, email, password_hash FROM users WHERE email = ?',
      [email.trim().toLowerCase()]
    );
    const user = rows[0];

    if (!user || !(await bcrypt.compare(password, user.password_hash))) {
      return res.status(401).json({ message: 'Correo o contraseña incorrectos' });
    }

    const token = jwt.sign({ id: user.id, email: user.email }, JWT_SECRET, { expiresIn: JWT_EXPIRES_IN });

    return res.json({
      token,
      user: { id: user.id, name: user.name, email: user.email }
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Error interno del servidor' });
  }
});

//POST /notas - crea una nota para el usuario autenticado
app.post('/notes', authenticateToken, async (req, res) => {
  try {
    const { title, content } = req.body || {};
    if (!title || !title.trim() || !content || !content.trim()) {
      return res.status(400).json({ message: 'El título y el contenido son obligatorios' });
    }

    const [result] = await pool.execute(
      'INSERT INTO notes (user_id, title, content) VALUES (?, ?, ?)',
      [req.user.id, title.trim(), content.trim()]
    );

    const [rows] = await pool.execute(
      'SELECT id, user_id, title, content, created_at FROM notes WHERE id = ? AND user_id = ?',
      [result.insertId, req.user.id]
    );

    return res.status(201).json(rows[0]);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Error interno del servidor' });
  }
});

//GET /notas - lista únicamente las notas
app.get('/notes', authenticateToken, async (req, res) => {
  try {
    const [rows] = await pool.execute(
      'SELECT id, user_id, title, content, created_at FROM notes WHERE user_id = ? ORDER BY created_at DESC, id DESC',
      [req.user.id]
    );
    return res.json(rows);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Error interno del servidor' });
  }
});

//Borrar notas
app.delete('/notes/:id', authenticateToken, async (req, res) => {
  try {
    const noteId = Number(req.params.id);
    if (!Number.isInteger(noteId) || noteId <= 0) {
      return res.status(400).json({ message: 'Identificador de nota no válido' });
    }

    const [result] = await pool.execute(
      'DELETE FROM notes WHERE id = ? AND user_id = ?',
      [noteId, req.user.id]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ message: 'La nota no existe' });
    }

    return res.json({ message: 'Nota eliminada' });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Error interno del servidor' });
  }
});

// GET / - página de estado de la API
app.get('/', (req, res) => {
  res.type('html').send(`<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>NotasApp API</title>
  <style>
    body { font-family: Arial, sans-serif; background: #f4f6fa; color: #1b1f27; margin: 0; padding: 32px; }
    .card { max-width: 720px; margin: 0 auto; background: #fff; border-radius: 12px; padding: 24px 32px; box-shadow: 0 2px 8px rgba(0,0,0,.1); }
    h1 { margin-top: 0; }
    .ok { color: #1b8a3a; font-weight: bold; }
    table { width: 100%; border-collapse: collapse; margin-top: 16px; }
    th, td { text-align: left; padding: 8px; border-bottom: 1px solid #e3e7ee; }
    code { background: #eef2f8; padding: 2px 6px; border-radius: 4px; }
  </style>
</head>
<body>
  <div class="card">
    <h1>NotasApp API</h1>
    <p>Estado: <span class="ok">en línea</span> · Versión 1.0.0</p>
    <table>
      <tr><th>Método</th><th>Ruta</th><th>Autenticación</th><th>Descripción</th></tr>
      <tr><td>POST</td><td><code>/register</code></td><td>No</td><td>Crear usuario</td></tr>
      <tr><td>POST</td><td><code>/login</code></td><td>No</td><td>Iniciar sesión y obtener token</td></tr>
      <tr><td>POST</td><td><code>/notes</code></td><td>Bearer</td><td>Crear nota</td></tr>
      <tr><td>GET</td><td><code>/notes</code></td><td>Bearer</td><td>Listar notas del usuario</td></tr>
      <tr><td>DELETE</td><td><code>/notes/:id</code></td><td>Bearer</td><td>Eliminar una nota</td></tr>
    </table>
  </div>
</body>
</html>`);
});

// GET /health - verificación rápida de que el servidor y la base de datos responden
app.get('/health', async (req, res) => {
  try {
    await pool.query('SELECT 1');
    res.json({ status: 'ok', database: 'connected' });
  } catch (error) {
    res.status(500).json({ status: 'error', database: 'disconnected' });
  }
});

app.use((req, res) => {
  res.status(404).json({ message: 'Ruta no encontrada' });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`API NotasApp escuchando en el puerto ${PORT}`);
});