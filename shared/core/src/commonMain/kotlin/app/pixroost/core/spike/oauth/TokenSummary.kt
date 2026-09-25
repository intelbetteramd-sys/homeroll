package app.pixroost.core.spike.oauth

/**
 * "токен 1180 символов, истекает через 59 мин, есть refresh token, права: …" for the screen and the report. No part
 * of the token itself: the report goes into a public issue.
 */
fun tokenSummary(tokens: TokenSet, nowMillis: Long): String = listOfNotNull(
    "токен ${tokens.accessToken.length} символов",
    tokens.expiresAtMillis?.let { "истекает через ${formatDuration(it - nowMillis)}" },
    if (tokens.refreshToken != null) "есть refresh token" else "нет refresh token",
    tokens.scope?.let { "права: $it" },
).joinToString(", ")

private fun formatDuration(millis: Long): String = if (millis < OAuthSpikeConstants.MILLIS_IN_DAY * 2) {
    "${millis / OAuthSpikeConstants.MILLIS_IN_MINUTE} мин"
} else {
    "${millis / OAuthSpikeConstants.MILLIS_IN_DAY} дн"
}
