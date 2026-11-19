package com.example.notepad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class NoteImageStore {
    private static final String IMAGE_DIRECTORY = "note_images";

    private NoteImageStore() {
    }

    public static File imageDirectory(Context context) {
        File directory = new File(context.getFilesDir(), IMAGE_DIRECTORY);
        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
        return directory;
    }

    public static String copyFromUri(Context context, Uri uri) throws IOException {
        String mimeType = context.getContentResolver().getType(uri);
        String extension = extensionForMimeType(mimeType);
        String fileName = "note_" + System.currentTimeMillis() + extension;
        File destination = new File(imageDirectory(context), fileName);
        try (InputStream input = context.getContentResolver().openInputStream(uri);
             FileOutputStream output = new FileOutputStream(destination)) {
            if (input == null) {
                throw new IOException("无法读取所选图片");
            }
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        } catch (IOException exception) {
            //noinspection ResultOfMethodCallIgnored
            destination.delete();
            throw exception;
        }
        return fileName;
    }

    public static File resolve(Context context, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        File file = new File(imageDirectory(context), fileName);
        return file.isFile() ? file : null;
    }

    public static void delete(Context context, String fileName) {
        File file = resolve(context, fileName);
        if (file != null) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public static Bitmap decodePreview(Context context, String fileName, int maxWidth,
                                       int maxHeight) {
        File file = resolve(context, fileName);
        if (file == null) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        int sample = 1;
        while (bounds.outWidth / (sample * 2) >= maxWidth
                || bounds.outHeight / (sample * 2) >= maxHeight) {
            sample *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        try (FileInputStream input = new FileInputStream(file)) {
            return BitmapFactory.decodeStream(input, null, options);
        } catch (IOException exception) {
            return null;
        }
    }

    private static String extensionForMimeType(String mimeType) {
        if ("image/png".equals(mimeType)) {
            return ".png";
        }
        if ("image/webp".equals(mimeType)) {
            return ".webp";
        }
        if ("image/gif".equals(mimeType)) {
            return ".gif";
        }
        return ".jpg";
    }
}
