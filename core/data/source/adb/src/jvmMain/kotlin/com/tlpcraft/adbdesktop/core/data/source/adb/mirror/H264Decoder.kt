package com.tlpcraft.adbdesktop.core.data.source.adb.mirror

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorFrameDto
import org.bytedeco.ffmpeg.avcodec.AVCodecContext
import org.bytedeco.ffmpeg.avcodec.AVPacket
import org.bytedeco.ffmpeg.avutil.AVFrame
import org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_FLAG_LOW_DELAY
import org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264
import org.bytedeco.ffmpeg.global.avcodec.AV_INPUT_BUFFER_PADDING_SIZE
import org.bytedeco.ffmpeg.global.avcodec.av_packet_alloc
import org.bytedeco.ffmpeg.global.avcodec.av_packet_free
import org.bytedeco.ffmpeg.global.avcodec.avcodec_alloc_context3
import org.bytedeco.ffmpeg.global.avcodec.avcodec_find_decoder
import org.bytedeco.ffmpeg.global.avcodec.avcodec_free_context
import org.bytedeco.ffmpeg.global.avcodec.avcodec_open2
import org.bytedeco.ffmpeg.global.avcodec.avcodec_receive_frame
import org.bytedeco.ffmpeg.global.avcodec.avcodec_send_packet
import org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_BGRA
import org.bytedeco.ffmpeg.global.avutil.av_frame_alloc
import org.bytedeco.ffmpeg.global.avutil.av_frame_free
import org.bytedeco.ffmpeg.global.swscale.SWS_BILINEAR
import org.bytedeco.ffmpeg.global.swscale.sws_freeContext
import org.bytedeco.ffmpeg.global.swscale.sws_getCachedContext
import org.bytedeco.ffmpeg.global.swscale.sws_scale
import org.bytedeco.ffmpeg.swscale.SwsContext
import org.bytedeco.ffmpeg.swscale.SwsFilter
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.javacpp.PointerPointer

/** Turns whole H.264 access units into BGRA frames. Not thread safe. Frames may change size when the device rotates. */
internal class H264Decoder : AutoCloseable {
    private val codec = checkNotNull(avcodec_find_decoder(AV_CODEC_ID_H264)) { "This build has no H.264 decoder" }
    private val context: AVCodecContext = avcodec_alloc_context3(codec).apply { flags(flags() or AV_CODEC_FLAG_LOW_DELAY) }
    private val packet: AVPacket = av_packet_alloc()
    private val frame: AVFrame = av_frame_alloc()
    private var input = BytePointer()
    private val outputPlanes = PointerPointer<BytePointer>(1L)
    private val outputRowBytes = IntPointer(1L)
    private var output = BytePointer()
    private var scaler: SwsContext? = null

    init {
        check(avcodec_open2(context, codec, null as PointerPointer<*>?) == 0) { "Could not open the H.264 decoder" }
    }

    /** Decodes one access unit, parameter sets included, and returns the frames it completes. */
    fun decode(accessUnit: ByteArray): List<MirrorFrameDto> {
        val paddedSize = accessUnit.size + AV_INPUT_BUFFER_PADDING_SIZE
        if (input.capacity() < paddedSize) {
            input.close()
            input = BytePointer(paddedSize.toLong())
        }
        input.position(0L).put(accessUnit, 0, accessUnit.size)
        input.position(accessUnit.size.toLong()).put(PADDING, 0, AV_INPUT_BUFFER_PADDING_SIZE)
        packet.data(input.position(0L))
        packet.size(accessUnit.size)
        return drainFrames()
    }

    /** A broken packet only costs the frames up to the next key frame, so it is skipped, not fatal. */
    private fun drainFrames(): List<MirrorFrameDto> {
        if (avcodec_send_packet(context, packet) != 0) return emptyList()
        return buildList {
            while (avcodec_receive_frame(context, frame) == 0) add(toBgra())
        }
    }

    private fun toBgra(): MirrorFrameDto {
        val width = frame.width()
        val height = frame.height()
        val rowBytes = width * BYTES_PER_PIXEL
        val size = rowBytes * height
        if (output.capacity() < size) {
            output.close()
            output = BytePointer(size.toLong())
        }
        scaler = sws_getCachedContext(
            scaler,
            width,
            height,
            frame.format(),
            width,
            height,
            AV_PIX_FMT_BGRA,
            SWS_BILINEAR,
            null as SwsFilter?,
            null as SwsFilter?,
            null as DoublePointer?,
        )
        outputPlanes.put(0L, output.position(0L))
        outputRowBytes.put(0L, rowBytes)
        sws_scale(scaler, frame.data(), frame.linesize(), 0, height, outputPlanes, outputRowBytes)
        return MirrorFrameDto(width, height, ByteArray(size).also { output.position(0L).get(it) })
    }

    override fun close() {
        sws_freeContext(scaler)
        avcodec_free_context(context)
        av_packet_free(packet)
        av_frame_free(frame)
        listOf(input, outputPlanes, outputRowBytes, output).forEach { it.close() }
    }

    private companion object {
        const val BYTES_PER_PIXEL = 4
        val PADDING = ByteArray(AV_INPUT_BUFFER_PADDING_SIZE)
    }
}
