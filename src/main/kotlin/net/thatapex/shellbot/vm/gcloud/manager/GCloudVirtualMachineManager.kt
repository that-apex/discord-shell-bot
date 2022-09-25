package net.thatapex.shellbot.vm.gcloud.manager

import com.google.api.gax.rpc.NotFoundException
import com.google.cloud.compute.v1.*
import com.google.cloud.compute.v1.AccessConfig.NetworkTier
import mu.KotlinLogging
import net.thatapex.shellbot.vm.VirtualMachine
import net.thatapex.shellbot.vm.VirtualMachineId
import net.thatapex.shellbot.vm.gcloud.GCloudProject
import net.thatapex.shellbot.vm.gcloud.GCloudVirtualMachine
import net.thatapex.shellbot.vm.gcloud.GCloudVirtualMachineProperties
import net.thatapex.shellbot.vm.gcloud.GCloudZone
import net.thatapex.shellbot.vm.gcloud.api.ensureSuccessful
import net.thatapex.shellbot.vm.gcloud.api.suspend
import net.thatapex.shellbot.vm.manager.VirtualMachineManager
import net.thatapex.shellbot.vm.properties.VirtualMachineProperties
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class GCloudVirtualMachineManager(
    private val project: GCloudProject,
    private val zone: GCloudZone,
    private val globalTag: String? = null
) : VirtualMachineManager {

    override suspend fun createVirtualMachine(
        id: VirtualMachineId,
        properties: VirtualMachineProperties
    ): VirtualMachine {
        logger.trace { "Creating a new virtual machine $id" }

        val machineType = "zones/${zone.name}/machineTypes/${properties[GCloudVirtualMachineProperties.MACHINE_TYPE]}"
        val sourceImageProject = properties[GCloudVirtualMachineProperties.SOURCE_IMAGE_PROJECT]
        val sourceImageName = properties[GCloudVirtualMachineProperties.SOURCE_IMAGE_NAME]
        val sourceImage = "projects/${sourceImageProject}/global/images/family/${sourceImageName}"

        InstancesClient.create().use { client ->
            val disk = AttachedDisk.newBuilder()
                .setBoot(true)
                .setAutoDelete(true)
                .setType(AttachedDisk.Type.PERSISTENT.toString())
                .setDeviceName(id.value + "-disk")
                .setInitializeParams(
                    AttachedDiskInitializeParams.newBuilder()
                        .setSourceImage(sourceImage)
                        .setDiskSizeGb(properties[GCloudVirtualMachineProperties.DISK_SIZE_GB])
                        .build()
                )
                .build()

            val networkInterface: NetworkInterface = NetworkInterface.newBuilder()
                .setNetwork(properties[GCloudVirtualMachineProperties.NETWORK_NAME])
                .addAccessConfigs(
                    AccessConfig.newBuilder()
                        .setNetworkTier(NetworkTier.STANDARD.toString())
                        .build()
                )
                .build()

            val tags = this.globalTag?.let { listOf(it) }
                ?: emptyList()

            val items = ArrayList<Items>()
            if (properties.has(VirtualMachineProperties.SSH_KEY)) {
                val sshKey = properties[VirtualMachineProperties.SSH_KEY]
                val sshUser = properties[VirtualMachineProperties.SSH_USER]

                items.add(
                    Items.newBuilder()
                        .setKey("ssh-keys")
                        .setValue("${sshUser}:${sshKey}")
                        .build()
                )
            }

            val instanceResource = Instance.newBuilder()
                .setName(id.value)
                .setMachineType(machineType)
                .addDisks(disk)
                .addNetworkInterfaces(networkInterface)
                .setTags(
                    Tags.newBuilder()
                        .addAllItems(tags)
                        .build()
                )
                .setMetadata(
                    Metadata.newBuilder()
                        .addAllItems(items)
                        .build()
                )
                .build()

            val request = InsertInstanceRequest.newBuilder()
                .setProject(project.id)
                .setZone(zone.name)
                .setInstanceResource(instanceResource)
                .build()

            client.insertAsync(request)
                .suspend()
                .get()
                .ensureSuccessful()

            logger.info { "Created a new virtual machine $id" }

            return findVirtualMachine(id)
                ?: throw IllegalStateException("Failed to find a newly created virtual machine $id")
        }
    }

    override suspend fun findVirtualMachine(id: VirtualMachineId): VirtualMachine? {
        InstancesClient.create().use { client ->
            val request = GetInstanceRequest.newBuilder()
                .setInstance(id.value)
                .setProject(project.id)
                .setZone(zone.name)
                .build()

            val response = client.callable.futureCall(request)
                .suspend()

            if (response.error is NotFoundException) {
                return null
            }

            val data = response.get()

            return GCloudVirtualMachine(
                id = id,
                hostname = data.hostname,
                publicIp = data.networkInterfacesList
                    .flatMap { it.accessConfigsList }
                    .firstOrNull { it.hasNatIP() }
                    ?.natIP
                    ?: data.hostname,
                sshPort = 22,
                project = project,
                zone = GCloudZone.normalize(data.zone)
            )
        }
    }

    override suspend fun deleteVirtualMachine(machine: VirtualMachine) {
        logger.debug { "Deleting virtual machine ${machine.id}" }
        requireGCloudMachine(machine)

        InstancesClient.create().use { client ->
            val request = DeleteInstanceRequest.newBuilder()
                .setProject(machine.project.id)
                .setZone(machine.zone.name)
                .setInstance(machine.id.value)
                .build()

            client.deleteAsync(request)
                .suspend()
                .get()
                .ensureSuccessful()

            logger.info { "Deleted virtual machine ${machine.id}" }
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun requireGCloudMachine(machine: VirtualMachine) {
        contract { returns() implies (machine is GCloudVirtualMachine) }

        require(machine is GCloudVirtualMachine) { "machine must be a GCloudVirtualMachine" }
        require(machine.project == this.project) { "machine must be in the same project as this manager" }
        require(machine.zone == this.zone) { "machine must be in the same zone as this manager" }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
