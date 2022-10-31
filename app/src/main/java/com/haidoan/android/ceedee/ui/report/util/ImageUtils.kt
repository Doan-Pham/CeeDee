package com.haidoan.android.ceedee.ui.report.util

import android.graphics.Bitmap


class ImageUtils {
    companion object {
        @JvmStatic
        fun resizeBitmap(inputImage: Bitmap, resultWidth: Int, resultHeight: Int): Bitmap {
            var resultImage = inputImage
            return if (resultHeight > 0 && resultWidth > 0) {
                val width = inputImage.width
                val height = inputImage.height
                val ratioBitmap = width.toFloat() / height.toFloat()
                val ratioMax = resultWidth.toFloat() / resultHeight.toFloat()
                var finalWidth = resultWidth
                var finalHeight = resultHeight
                if (ratioMax > ratioBitmap) {
                    finalWidth = (resultHeight.toFloat() * ratioBitmap).toInt()
                } else {
                    finalHeight = (resultWidth.toFloat() / ratioBitmap).toInt()
                }
                resultImage = Bitmap.createScaledBitmap(inputImage, finalWidth, finalHeight, true)
                resultImage
            } else {
                resultImage
            }
        }
    }
}