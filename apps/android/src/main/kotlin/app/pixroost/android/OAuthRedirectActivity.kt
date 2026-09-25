package app.pixroost.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.pixroost.android.spike.data.RedirectInbox

/**
 * Receives the OAuth redirect from Custom Tabs, passes it on and brings the main screen back, which also closes
 * the tab above it.
 */
class OAuthRedirectActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.let(RedirectInbox::deliver)
        startActivity(
            Intent(this, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP,
            ),
        )
        finish()
    }
}
