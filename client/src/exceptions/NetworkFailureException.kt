package org.keizar.client.exceptions


class NetworkFailureException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)