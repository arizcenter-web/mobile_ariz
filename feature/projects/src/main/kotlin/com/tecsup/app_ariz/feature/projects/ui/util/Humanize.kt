package com.tecsup.app_ariz.feature.projects.ui.util

/** Los catálogos del backend llegan como slugs (`club-house`) o texto libre: "Club house". */
internal fun String.humanize(): String = replace('-', ' ').replace('_', ' ').replaceFirstChar { it.uppercase() }
