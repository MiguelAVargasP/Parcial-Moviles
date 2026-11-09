// Interfaz del servicio que expone la app central (hub) a los 4 juegos.
package edu.upb.arcade.core;

interface IArcadeService {
    /** Nickname del usuario con sesión iniciada en el hub, o null si nadie ha iniciado sesión. */
    String getNickname();

    /** Mejor score del usuario actual en ese juego (0 si no tiene). */
    int getMejorScore(String juego);

    /**
     * Guarda un score para el usuario actual. El hub agrega la ciudad (GPS) por su cuenta.
     * Devuelve false si no hay sesión o el juego no existe.
     */
    boolean guardarScore(String juego, int puntos);
}
