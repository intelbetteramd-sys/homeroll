package app.pixroost.core.spike.oauth

/** The service refused: [message] carries its error code and description. */
class OAuthException(message: String) : Exception(message)
