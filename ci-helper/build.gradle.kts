@file:Suppress("UnstableApiUsage")

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.util.cio.readChannel
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    kotlin("jvm")
    id("kotlinx-atomicfu")
}

val hostOS: OS by lazy {
    when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> OS.WINDOWS
        Os.isFamily(Os.FAMILY_MAC) -> OS.MACOS
        Os.isFamily(Os.FAMILY_UNIX) -> OS.LINUX
        else -> error("Unsupported OS: ${System.getProperty("os.name")}")
    }
}

val hostArch: String by lazy {
    when (val arch = System.getProperty("os.arch")) {
        "x86_64" -> "amd64"
        "amd64" -> "amd64"
        "arm64" -> "arm64"
        "aarch64" -> "arm64"
        else -> error("Unsupported host architecture: $arch")
    }
}


enum class OS(
    val isUnix: Boolean,
) {
    WINDOWS(false),
    MACOS(true),
    LINUX(true),
}


val namer = ArtifactNamer()

class ArtifactNamer {
    private val APP_NAME = "Keizar"

    fun getFullVersionFromTag(tag: String): String {
        return tag.substringAfter("v")
    }

    // fullVersion example: 2.0.0-beta03
    fun androidApp(fullVersion: String): String {
        return "$APP_NAME-$fullVersion.apk"
    }

    fun androidAppQR(fullVersion: String): String {
        return "${androidApp(fullVersion)}.qrcode.png"
    }
}

tasks.register("uploadAndroidApk") {
    doLast {
        ReleaseEnvironment().run {
            uploadReleaseAsset(
                name = namer.androidApp(fullVersion),
                contentType = "application/vnd.android.package-archive",
                file = project(":app").layout.buildDirectory.file("outputs/apk/release")
                    .get().asFile.walk()
                    .single { it.extension == "apk" && it.name.contains("release") },
            )
        }
    }
}

tasks.register("uploadAndroidApkQR") {
    doLast {
        ReleaseEnvironment().run {
            uploadReleaseAsset(
                name = namer.androidAppQR(fullVersion),
                contentType = "image/png",
                file = rootProject.file("apk-qrcode.png"),
            )
        }
    }
}

fun getProperty(name: String) =
    System.getProperty(name)
        ?: System.getenv(name)
        ?: properties[name]?.toString()
//        ?: getLocalProperty(name)
        ?: ext.get(name).toString()

// do not use `object`, compiler bug
open class ReleaseEnvironment {
    private val tag by lazy {
        getProperty("CI_TAG").also { println("tag = $it") }
    }
    open val fullVersion by lazy {
        namer.getFullVersionFromTag(tag).also { println("fullVersion = $it") }
    }
    val releaseId by lazy {
        getProperty("CI_RELEASE_ID").also { println("releaseId = $it") }
    }
    val repository by lazy {
        getProperty("GITHUB_REPOSITORY").also { println("repository = $it") }
    }
    val token by lazy {
        getProperty("GITHUB_TOKEN").also { println("token = ${it.isNotEmpty()}") }
    }

    open fun uploadReleaseAsset(
        name: String,
        contentType: String,
        file: File,
    ) {
        check(file.exists()) { "File '${file.absolutePath}' does not exist when attempting to upload '$name'." }
        val tag = getProperty("CI_TAG")
        val fullVersion = namer.getFullVersionFromTag(tag)
        val releaseId = getProperty("CI_RELEASE_ID")
        val repository = getProperty("GITHUB_REPOSITORY")
        val token = getProperty("GITHUB_TOKEN")
        println("tag = $tag")
        return uploadReleaseAsset(repository, releaseId, token, fullVersion, name, contentType, file)
    }

    fun uploadReleaseAsset(
        repository: String,
        releaseId: String,
        token: String,
        fullVersion: String,

        name: String,
        contentType: String,
        file: File,
    ) {
        println("fullVersion = $fullVersion")
        println("releaseId = $releaseId")
        println("token = ${token.isNotEmpty()}")
        println("repository = $repository")

        runBlocking {
            val url = "https://uploads.github.com/repos/$repository/releases/$releaseId/assets"
            val resp = HttpClient().post(url) {
                header("Authorization", "Bearer $token")
                header("Accept", "application/vnd.github+json")
                parameter("name", name)
                contentType(ContentType.parse(contentType))
                setBody(object : OutgoingContent.ReadChannelContent() {
                    override val contentType: ContentType get() = ContentType.parse(contentType)
                    override val contentLength: Long = file.length()
                    override fun readFrom(): ByteReadChannel {
                        return file.readChannel()
                    }

                })
            }
            check(resp.status.isSuccess()) {
                "Request $url failed with ${resp.status}. Response: ${
                    resp.runCatching { bodyAsText() }.getOrNull()
                }"
            }
        }
    }
}
