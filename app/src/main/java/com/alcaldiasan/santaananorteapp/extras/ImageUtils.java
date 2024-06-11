package com.alcaldiasan.santaananorteapp.extras;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    private static final int MAX_IMAGE_SIZE = 1024; // Tamaño máximo de la imagen en píxeles

    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        // Decodificar el inputStream en un Bitmap
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        // Redimensionar el Bitmap si es necesario
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
            float aspectRatio = (float) width / (float) height;
            if (width > height) {
                width = MAX_IMAGE_SIZE;
                height = (int) (width / aspectRatio);
            } else {
                height = MAX_IMAGE_SIZE;
                width = (int) (height * aspectRatio);
            }
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        // Comprimir el Bitmap en un ByteArrayOutputStream
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, buffer); // 80 es la calidad de compresión (de 0 a 100)

        // Convertir el ByteArrayOutputStream en un arreglo de bytes y devolverlo
        return buffer.toByteArray();
    }




}
