package edu.upb.arcade.datos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Usuario(
    val id: Long,
    val email: String,
    val nickname: String,
    val fechaNacimiento: String, // yyyy-MM-dd
    val score: Int               // suma de todos sus puntos
)

data class RegistroScore(
    val id: Long,
    val juego: String,
    val puntos: Int,
    val ciudad: String,
    val fecha: Long
)

/** Base de datos local (SQLite) con usuarios y scores. Una sola instancia por proceso. */
class Repositorio private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "arcade_upb.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL UNIQUE COLLATE NOCASE,
                hash TEXT NOT NULL,
                sal TEXT NOT NULL,
                nickname TEXT NOT NULL,
                fecha_nacimiento TEXT NOT NULL,
                score INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE scores (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                juego TEXT NOT NULL,
                puntos INTEGER NOT NULL,
                ciudad TEXT NOT NULL,
                fecha INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_scores_usuario ON scores(usuario_id, juego)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS scores")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        onCreate(db)
    }

    // ---------------------------------------------------------------- usuarios

    sealed interface ResultadoRegistro {
        data class Ok(val usuario: Usuario) : ResultadoRegistro
        data class Error(val mensaje: String) : ResultadoRegistro
    }

    fun registrar(email: String, contrasena: String, nickname: String, fechaNacimiento: String): ResultadoRegistro {
        val correo = email.trim().lowercase()
        if (existeEmail(correo)) return ResultadoRegistro.Error("Ya existe una cuenta con ese email")
        val sal = Seguridad.nuevaSal()
        val valores = ContentValues().apply {
            put("email", correo)
            put("hash", Seguridad.hash(contrasena, sal))
            put("sal", sal)
            put("nickname", nickname.trim())
            put("fecha_nacimiento", fechaNacimiento)
        }
        val id = writableDatabase.insert("usuarios", null, valores)
        if (id == -1L) return ResultadoRegistro.Error("No se pudo crear la cuenta")
        return ResultadoRegistro.Ok(usuarioPorId(id)!!)
    }

    private fun existeEmail(email: String): Boolean =
        readableDatabase.rawQuery("SELECT 1 FROM usuarios WHERE email = ?", arrayOf(email)).use { it.moveToFirst() }

    /** Devuelve el usuario si email y contraseña coinciden. */
    fun login(email: String, contrasena: String): Usuario? {
        val fila = readableDatabase.rawQuery(
            "SELECT id, hash, sal FROM usuarios WHERE email = ?",
            arrayOf(email.trim().lowercase())
        ).use { c ->
            if (!c.moveToFirst()) null else Triple(c.getLong(0), c.getString(1), c.getString(2))
        } ?: return null
        val (id, hash, sal) = fila
        return if (Seguridad.verificar(contrasena, sal, hash)) usuarioPorId(id) else null
    }

    fun usuarioPorId(id: Long): Usuario? =
        readableDatabase.rawQuery(
            "SELECT id, email, nickname, fecha_nacimiento, score FROM usuarios WHERE id = ?",
            arrayOf(id.toString())
        ).use { c ->
            if (!c.moveToFirst()) null
            else Usuario(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4))
        }

    // ---------------------------------------------------------------- perfil

    /** Cambia nickname y fecha de nacimiento. El email no se puede cambiar (es la identidad de la cuenta). */
    fun actualizarDatos(usuarioId: Long, nickname: String, fechaNacimiento: String): Boolean {
        val filas = writableDatabase.update(
            "usuarios",
            ContentValues().apply {
                put("nickname", nickname.trim())
                put("fecha_nacimiento", fechaNacimiento)
            },
            "id = ?", arrayOf(usuarioId.toString())
        )
        return filas > 0
    }

    /** Cambia la contraseña solo si [actual] es correcta. Se genera una sal nueva. */
    fun cambiarContrasena(usuarioId: Long, actual: String, nueva: String): Boolean {
        val fila = readableDatabase.rawQuery(
            "SELECT hash, sal FROM usuarios WHERE id = ?", arrayOf(usuarioId.toString())
        ).use { c -> if (c.moveToFirst()) Pair(c.getString(0), c.getString(1)) else null } ?: return false
        if (!Seguridad.verificar(actual, fila.second, fila.first)) return false
        val sal = Seguridad.nuevaSal()
        val filas = writableDatabase.update(
            "usuarios",
            ContentValues().apply {
                put("hash", Seguridad.hash(nueva, sal))
                put("sal", sal)
            },
            "id = ?", arrayOf(usuarioId.toString())
        )
        return filas > 0
    }

    // ---------------------------------------------------------------- scores

    fun insertarScore(usuarioId: Long, juego: String, puntos: Int, ciudad: String): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val id = db.insert("scores", null, ContentValues().apply {
                put("usuario_id", usuarioId)
                put("juego", juego)
                put("puntos", puntos)
                put("ciudad", ciudad)
                put("fecha", System.currentTimeMillis())
            })
            db.execSQL("UPDATE usuarios SET score = score + ? WHERE id = ?", arrayOf<Any>(puntos, usuarioId))
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun actualizarCiudad(scoreId: Long, ciudad: String) {
        writableDatabase.update(
            "scores", ContentValues().apply { put("ciudad", ciudad) },
            "id = ?", arrayOf(scoreId.toString())
        )
    }

    fun mejorScore(usuarioId: Long, juego: String): Int =
        readableDatabase.rawQuery(
            "SELECT MAX(puntos) FROM scores WHERE usuario_id = ? AND juego = ?",
            arrayOf(usuarioId.toString(), juego)
        ).use { c -> if (c.moveToFirst() && !c.isNull(0)) c.getInt(0) else 0 }

    /** Mejor partida de cada juego (con la ciudad donde se hizo). */
    fun mejoresPorJuego(usuarioId: Long): Map<String, RegistroScore> {
        val resultado = mutableMapOf<String, RegistroScore>()
        readableDatabase.rawQuery(
            "SELECT id, juego, puntos, ciudad, fecha FROM scores WHERE usuario_id = ? ORDER BY puntos DESC, fecha DESC",
            arrayOf(usuarioId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                val r = RegistroScore(c.getLong(0), c.getString(1), c.getInt(2), c.getString(3), c.getLong(4))
                if (r.juego !in resultado) resultado[r.juego] = r
            }
        }
        return resultado
    }

    fun historial(usuarioId: Long, limite: Int = 15): List<RegistroScore> =
        readableDatabase.rawQuery(
            "SELECT id, juego, puntos, ciudad, fecha FROM scores WHERE usuario_id = ? ORDER BY fecha DESC LIMIT ?",
            arrayOf(usuarioId.toString(), limite.toString())
        ).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(RegistroScore(c.getLong(0), c.getString(1), c.getInt(2), c.getString(3), c.getLong(4)))
                }
            }
        }

    companion object {
        @Volatile
        private var instancia: Repositorio? = null

        fun obtener(context: Context): Repositorio =
            instancia ?: synchronized(this) {
                instancia ?: Repositorio(context).also { instancia = it }
            }
    }
}
