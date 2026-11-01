package com.tlpcraft.adbdesktop.core.data.source.adb.mirror

/** How the scrcpy server bundled as a resource is started. [VERSION] must match that file or the server refuses to run. */
internal object ScrcpyServerCommand {
    const val RESOURCE = "/scrcpy-server"
    private const val VERSION = "5.0.1"
    private const val MAX_SIZE = 1280
    private const val MAX_FPS = 30
    private const val BIT_RATE = 8_000_000

    fun remotePath(sessionId: Int): String = "/data/local/tmp/adb-desktop-scrcpy-${sessionName(sessionId)}.jar"

    /** The abstract socket the server listens on, derived from the id on its side. */
    fun socketName(sessionId: Int): String = "scrcpy_${sessionName(sessionId)}"

    /**
     * H.264 video only, no audio or input. Frame headers stay on because a raw stream cannot show where a frame ends, which
     * would hold back the last frame of a still screen. The dummy byte tells a live server from adb accepting early.
     */
    fun shellArguments(sessionId: Int): List<String> = listOf(
        "CLASSPATH=${remotePath(sessionId)}",
        "app_process",
        "/",
        "com.genymobile.scrcpy.Server",
        VERSION,
        "scid=${sessionName(sessionId)}",
        "log_level=warn",
        "audio=false",
        "control=false",
        "tunnel_forward=true",
        "video_codec=h264",
        "max_size=$MAX_SIZE",
        "max_fps=$MAX_FPS",
        "video_bit_rate=$BIT_RATE",
        "send_device_meta=false",
        "send_stream_meta=false",
    )

    private fun sessionName(sessionId: Int): String = sessionId.toString(radix = 16).padStart(SESSION_NAME_LENGTH, '0')

    private const val SESSION_NAME_LENGTH = 8
}
