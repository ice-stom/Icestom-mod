plugins {
	id("net.fabricmc.fabric-loom-remap")
	id("com.gradleup.shadow") version("9.4.3")
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

	exclusiveContent {
		forRepository {
			maven( "https://api.modrinth.com/maven")
		}
		filter {
			includeGroup("maven.modrinth")
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings(loom.officialMojangMappings())

	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	implementation("io.gitlab.icestom:stomtrack:0.0.24")
	implementation("io.gitlab.icestom:protocol:0.0.1")
	implementation("io.gitlab.icestom:linefold:0.0.1")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

	runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")

	modImplementation("maven.modrinth:openboatutils:0.5.1c+1.21.9-1.21.11")
}

tasks {
	java {
		sourceCompatibility = JavaVersion.VERSION_25
		targetCompatibility = JavaVersion.VERSION_25
	}

	shadowJar {
		isZip64 = true
		archiveClassifier.set("dev")
		dependencies {
			include(dependency("io.gitlab.icestom:.*"))
			exclude(dependency("maven.modrinth:openboatutils:0.5.1c+1.21.9-1.21.11"))
		}
	}

	remapJar {
		dependsOn("shadowJar")
	}

	withType<net.fabricmc.loom.task.RemapJarTask>().configureEach {
		if (name == "remapJar") {
			inputFile.set(named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").flatMap { it.archiveFile })
			archiveClassifier.set("")
		}
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
	options.release.set(21)
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)
	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}