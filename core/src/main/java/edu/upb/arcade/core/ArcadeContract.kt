package edu.upb.arcade.core

/** Datos que comparten el hub y los juegos para poder hablarse. */
object ArcadeContract {
    const val PAQUETE_HUB = "edu.upb.arcade"
    const val ACCION_SERVICIO = "edu.upb.arcade.action.SCORE_SERVICE"
    const val PERMISO = "edu.upb.arcade.permission.SCORES"

    enum class JuegoArcade(val id: String, val titulo: String, val paquete: String) {
        MONOPOLY("monopoly", "Monopoly", "edu.upb.arcade.monopoly"),
        TETRIS("tetris", "Tetris", "edu.upb.arcade.tetris"),
        TRIQUI("triqui", "Triqui", "edu.upb.arcade.triqui"),
        CULEBRITA("culebrita", "Culebrita", "edu.upb.arcade.culebrita");

        companion object {
            fun porId(id: String): JuegoArcade? = entries.firstOrNull { it.id == id }
        }
    }
}
