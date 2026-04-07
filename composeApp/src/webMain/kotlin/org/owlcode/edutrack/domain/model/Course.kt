package org.owlcode.edutrack.domain.model

data class Course(
    val id: String,
    val name: String,
    val teacher: String = "",
    val description: String = "",
    val color: String = "#4A90D9",          // hex para serialización; se convierte a Color en UI
    val locationOrPlatform: String = "",    // aula física o URL de plataforma
    val credits: Int? = null                // créditos académicos (opcional)
)

