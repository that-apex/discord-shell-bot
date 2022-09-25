package net.thatapex.shellbot.vm.gcloud

data class GCloudProject(val id: String) {
    init {
        require(id.matches(PROJECT_REGEX)) { "Invalid project ID: $id" }
    }

    companion object {
        val PROJECT_REGEX = "[a-z0-9-]{6,30}".toRegex()
        val URL_REGEX = "https://www.googleapis.com/compute/v1/projects/${PROJECT_REGEX.pattern}".toRegex()

        fun normalize(nameOrUrl: String): GCloudProject {
            val match = URL_REGEX.matchEntire(nameOrUrl)

            return GCloudProject(if (match != null) match.groupValues[1] else nameOrUrl)
        }
    }
}

data class GCloudZone(val name: String) {
    init {
        require(name.matches(ZONE_REGEX)) { "Invalid zone name: $name" }
    }

    companion object {
        val ZONE_REGEX = "([a-z0-9-]*)".toRegex()
        val URL_REGEX = "${GCloudProject.URL_REGEX.pattern}/zones/${ZONE_REGEX.pattern}".toRegex()

        fun normalize(name: String): GCloudZone {
            val match = URL_REGEX.matchEntire(name)

            return GCloudZone(if (match != null) match.groupValues[1] else name)
        }
    }
}
