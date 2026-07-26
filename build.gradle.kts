plugins {
	id("net.fabricmc.fabric-loom-remap")
}

version = property("mod_version") as String
group = property("maven_group") as String

repositories {
	mavenLocal()

	maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
		content {
			includeGroup("me.djtheredstoner")
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings(loom.officialMojangMappings())

	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	implementation("io.gitlab.icestom:stomtrack:0.0.21")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

	runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
}

tasks {
	java {
		sourceCompatibility = JavaVersion.VERSION_25
		targetCompatibility = JavaVersion.VERSION_21
	}

	processResources {
		val version = project.version
		inputs.property("version", version)
		filesMatching("fabric.mod.json") {
			expand("version" to version)
		}

		dependsOn(":export-worker:shadowJar")
		from(project(":export-worker").tasks.named("shadowJar").map { it.outputs.files.singleFile }) {
			into("/")
		}
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(25)
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)
	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}