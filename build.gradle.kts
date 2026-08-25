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
}

dependencies {
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings(loom.officialMojangMappings())

	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	implementation("io.gitlab.icestom:stomtrack:0.0.24")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

	runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
}

tasks {
	java {
		sourceCompatibility = JavaVersion.VERSION_25
		targetCompatibility = JavaVersion.VERSION_25
	}

	named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
		isZip64 = true
		archiveClassifier.set("dev")
		dependencies {
			include(dependency("io.gitlab.icestom:.*"))
		}
	}

	named("remapJar") {
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