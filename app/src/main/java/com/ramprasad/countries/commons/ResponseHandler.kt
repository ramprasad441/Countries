package com.ramprasad.countries.commons

/**
 * Created by Ramprasad on 7/6/25.
 */
class NullResponseMessage(
    message: String = "The Response is null",
) : Exception(message)

class FailureResponse(
    message: String = "Error: Failed to fetch the response",
) : Exception(message)
