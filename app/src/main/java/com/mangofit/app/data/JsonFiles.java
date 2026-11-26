package com.mangofit.app.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** 내부 저장소의 JSON 배열 파일 읽기·쓰기. 쓰기는 임시 파일에 쓴 뒤 바꿔치기한다. */
public final class JsonFiles {

    private JsonFiles() { }

    static JSONArray readArray(Context context, String name) {
        File file = new File(context.getFilesDir(), name);
        if (!file.exists()) return new JSONArray();
        try (InputStream in = new FileInputStream(file)) {
            return new JSONArray(readAll(in));
        } catch (IOException | JSONException e) {
            throw new IllegalStateException(name + " 파일을 읽을 수 없음", e);
        }
    }

    static void writeArray(Context context, String name, JSONArray array) {
        File file = new File(context.getFilesDir(), name);
        File tmp = new File(context.getFilesDir(), name + ".tmp");
        try (OutputStream out = new FileOutputStream(tmp)) {
            out.write(array.toString(2).getBytes(StandardCharsets.UTF_8));
        } catch (IOException | JSONException e) {
            throw new IllegalStateException(name + " 파일을 쓸 수 없음", e);
        }
        if (!tmp.renameTo(file)) {
            throw new IllegalStateException(name + " 파일을 바꿀 수 없음");
        }
    }

    public static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            bytes.write(buf, 0, n);
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }
}
