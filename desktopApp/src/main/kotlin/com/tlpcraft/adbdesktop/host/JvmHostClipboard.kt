package com.tlpcraft.adbdesktop.host

import com.tlpcraft.adbdesktop.core.domain.service.HostClipboard
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

class JvmHostClipboard : HostClipboard {

    override suspend fun copyPng(png: ByteArray) {
        val image = ImageIO.read(ByteArrayInputStream(png))
        withContext(Dispatchers.Swing) {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(ImageSelection(image), null)
        }
    }

    private class ImageSelection(private val image: Image) : Transferable {
        override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor == DataFlavor.imageFlavor

        override fun getTransferData(flavor: DataFlavor): Any = if (isDataFlavorSupported(flavor)) image else throw UnsupportedFlavorException(flavor)
    }
}
