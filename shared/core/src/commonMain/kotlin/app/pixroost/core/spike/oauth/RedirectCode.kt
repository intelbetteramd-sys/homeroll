package app.pixroost.core.spike.oauth

/**
 * The authorization code from the redirect's parameters. Fails if the service returned an error, if `state` is not
 * the one this sign-in sent (someone else's redirect), or if there is no code.
 */
fun codeFromRedirect(expectedState: String, param: (String) -> String?): String {
    val problem = when {
        param("error") != null -> "${param("error")}: ${param("error_description").orEmpty()}"
        param("state") != expectedState -> "state не совпал"
        param("code") == null -> "нет code в ответе"
        else -> null
    }
    if (problem != null) throw OAuthException(problem)
    return checkNotNull(param("code"))
}
