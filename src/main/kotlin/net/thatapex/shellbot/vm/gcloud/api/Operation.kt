package net.thatapex.shellbot.vm.gcloud.api

import com.google.cloud.compute.v1.Operation

class GCloudException(message: String) : Exception(message)

fun Operation.ensureSuccessful(errorMessage: String = "API Call failed: ") {
    if (hasError()) {
        throw GCloudException(errorMessage + error.toString())
    }
}
