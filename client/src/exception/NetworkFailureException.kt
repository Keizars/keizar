package org.keizar.client.exception


class NetworkFailureException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)