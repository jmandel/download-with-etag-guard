package download

import com.github.kittinunf.fuel.Fuel
import java.util.Scanner
import java.io.File

fun main(args : Array<String>) {

    val url = args[0]
    val igFilename = args[1]

    val etagFilename = "${igFilename}.etag"
    val etag = try { File(etagFilename).readText(Charsets.UTF_8) } catch (e: java.io.FileNotFoundException) {""}

    var finalLocation : String
    var latestEtag : String
    var latestLength : Long

    val (request, response, results) = Fuel.head(args[0])
    .header(mapOf("Host" to "oss.sonatype.org"))
    //.also { println(it) }
    .response()

    println(response.headers)
    finalLocation = response.url.toString()
    latestEtag = response.headers["ETAG"].first()
    latestLength = response.headers["Content-Length"].first().toLong()

    val igFile = File(igFilename)
    val length = igFile.length()
    val needDownload = (length != latestLength || etag != latestEtag)

    if (needDownload) {
        var lastPercentage : Float = 0.0f
        Fuel.download(finalLocation)
            .fileDestination { response, url ->
                File(igFilename) }
            .progress { readBytes, totalBytes ->
                val progress = readBytes.toFloat() / totalBytes.toFloat() * 100
                if (progress > lastPercentage + 5) {
                    lastPercentage += 5
                    println("Bytes downloaded $readBytes / $totalBytes ($lastPercentage %)")
                }
            }
            .response()
    }
    File(etagFilename).writeText(latestEtag)
    println(finalLocation)
}

