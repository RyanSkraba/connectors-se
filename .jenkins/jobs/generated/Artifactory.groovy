import groovy.json.JsonSlurper

import java.nio.charset.StandardCharsets

import static java.util.Collections.emptyMap

class HttpConfig<T> {
    String method = 'GET'
    String url
    Map<String, String> headers = emptyMap()
    InputStream payload
    Closure<T> responseProcessor
}

abstract class Http<T> { // normally plain java is allowed on jenkins, HTTPBuilder is not always
    T http(HttpConfig<T> config) {
        HttpURLConnection connection = new URL(config.url).openConnection()
        connection.requestMethod = config.method
        config.headers.each({ e -> connection.setRequestProperty(e.key, e.value) })
        if (config.payload) {
            connection.doOutput = true
            connection.outputStream.write(config.payload.bytes)
        }
        if (connection.responseCode > 299) {
            throw new IllegalArgumentException("Invalid response: $connection.responseCode: $connection.inputStream.text")
        }
        config.responseProcessor(connection)
    }
}

class Artifactory extends Http<Map<String, Object>> {
    String token
    String base = 'https://talendregistry.jfrog.io/talendregistry'

    Collection<String> listTags(String image) {
        http(new HttpConfig<Map<String, Object>>(
                url: "$base/api/search/aql",
                method: 'POST',
                headers: [
                        'Content-Type': 'text/plain',
                        'Accept': 'application/json',
                        'X-JFrog-Art-Api': token
                ],
                payload: new ByteArrayInputStream("""items.find({
                "@docker.repoName":{ "\$match":"$image" }
            })""".stripMargin().getBytes()),
                responseProcessor: { HttpURLConnection connection -> new JsonSlurper().parse(connection.inputStream) }
        )).results.collect {
            def path = it.path
            def sep = path.lastIndexOf('/')
            "${path.substring(0, sep)}:${path.substring(sep + 1)}"
        }
    }
}

return new Artifactory()
