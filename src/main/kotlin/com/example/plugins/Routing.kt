package com.example.plugins

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

fun Application.configureRouting() {
    routing {
        get("/") {
//            call.respondText("Hello World!")
            val client = HttpClient()
            val page =
                client.get("https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html#dispatchers-and-threads")
            println(page.bodyAsText())
        }

        get("/channel") {
            val flow = MutableSharedFlow<Int?>()
            launch {
                repeat(10) {
                    flow.emit(it * 10)
                    delay(500)
                }
                flow.emit(null)
            }
            launch {
                flow.takeWhile { it != null }.collect {
                    println("it received $it")
                }
            }
        }

        post("/upload") {
            val channel = call.receiveChannel()
            call.attributes
            var counter = 0
            FileOutputStream(File("out.txt")).use { out ->
                channel.consumeEachBufferRange { buffer, last ->
                    println("counter = $counter")
                    counter += 1
                    out.write(buffer.moveToByteArray())
                    if (last) {
                        out.flush()
                    }
                    true
                }
            }
        }
    }
}
