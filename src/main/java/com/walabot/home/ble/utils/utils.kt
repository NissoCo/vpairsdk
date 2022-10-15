

import android.util.Log
import com.walabot.home.ble.BleService
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/***
 * Created by Dima Shostak on 1/19/2022
 */

const val BLE_MESSAGE_HEADER_SIZE = 8


fun BleService.requestMtuSafe(mtu: Int) {
    val scope = CoroutineScope(Dispatchers.IO)

    scope.launch {
        withTimeoutOrNull(3_000L) {
            repeat(3) {
                if (!_mtuChanged) {
                    Log.d("BleService utilsKt", "trying to request mtu $mtu")

                    //check if request mtu supported by the current sdk level
                    if (!requestMtu(mtu)) {
                        Log.d("BleService utilsKt", "request mtu is not supported")
                        return@withTimeoutOrNull null
                    }
                    delay(1_000L)

                } else {
                    return@withTimeoutOrNull true
                }
            }
        }
    }
}

fun flatMessageBuffer(messageBuffer: Queue<ByteArray>) =
    messageBuffer.flatMap { it.asIterable() }.toByteArray()


/**
 * Split byte array into chunks of specified size,
 * @param [blockSize] size of each block (MTU).
 * @return [List] of [ByteArray]
 */
fun ByteArray.split(blockSize: Int): List<ByteArray> {
    val result: MutableList<ByteArray> = mutableListOf()


    val blockCount = (this.size + blockSize - 1) / blockSize
    var range: ByteArray?

    for (i in 1 until blockCount) {
        val idx = (i - 1) * blockSize
        range = this.copyOfRange(idx, idx + blockSize)
        result.add(range)
    }

    // Last chunk
    var end: Int = if (this.size % blockSize == 0) {
        this.size
    } else {
        this.size % blockSize + blockSize * (blockCount - 1)
    }
    range = this.copyOfRange((blockCount - 1) * blockSize, end)

    result.add(range)

    result.add(0, generateStartChunk(chunks = blockCount, totalSize = this.size))
    return result
}

fun checkHeaderMessage(data: ByteArray): BleDataHeader? {
    if (data.size == BLE_MESSAGE_HEADER_SIZE) {
        val chunks = ByteBuffer.wrap(data.take(4).toByteArray()).order(ByteOrder.LITTLE_ENDIAN).int
        val totalSize =
            ByteBuffer.wrap(data.takeLast(4).toByteArray()).order(ByteOrder.LITTLE_ENDIAN).int
        return BleDataHeader(chunks = chunks, totalSize = totalSize)
    }

    return null
}

private fun generateStartChunk(chunks: Int, totalSize: Int): ByteArray {
    val numberOfChunksBytes =
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(chunks).array()
    val totalSizeBytes =
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(totalSize).array()
    return numberOfChunksBytes + totalSizeBytes
}