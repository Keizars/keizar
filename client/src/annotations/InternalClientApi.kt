package org.keizar.client.annotations

/**
 * Marks an API as internal to the client module and should not be used directly.
 */
@RequiresOptIn(
    message = "This API is internal to the client module and should not be used directly.",
    level = RequiresOptIn.Level.ERROR
)
annotation class InternalClientApi

