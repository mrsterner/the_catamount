architectury {
    common("fabric", "neoforge")
    platformSetupLoomIde()
}

val minecraftVersion = project.properties["minecraft_version"] as String
val geckolibVersion = project.properties["geckolib_version"] as String

loom.accessWidenerPath.set(file("src/main/resources/the_catamount.accesswidener"))

sourceSets.main.get().resources.srcDir("src/main/generated/resources")

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")

    modImplementation("software.bernie.geckolib:geckolib-common-${minecraftVersion}:${geckolibVersion}")
}
