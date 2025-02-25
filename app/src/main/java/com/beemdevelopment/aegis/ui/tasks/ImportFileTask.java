package com.beemdevelopment.aegis.ui.tasks;

import android.content.Context;
import android.net.Uri;

import com.beemdevelopment.aegis.R;
import com.beemdevelopment.aegis.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ImportFileTask reads an SAF file from a background thread and
 * writes it to a temporary file in the cache directory.
 */
public class ImportFileTask extends ProgressDialogTask<ImportFileTask.Params, ImportFileTask.Result> {
    private final Callback _cb;

    public ImportFileTask(Context context, Callback cb) {
        super(context, context.getString(R.string.reading_file));
        _cb = cb;
    }

    @Override
    protected Result doInBackground(Params... params) {
        Context context = getDialog().getContext();

        Params p = params[0];
        try (InputStream inStream = context.getContentResolver().openInputStream(p.getUri())) {
            if (inStream == null) {
                throw new IOException("openInputStream returned null");
            }

            String prefix = p.getNamePrefix() != null ? p.getNamePrefix() + "-" : "";
            String suffix = p.getNameSuffix() != null ? "-" + p.getNameSuffix() : "";

            File tempFile = File.createTempFile(prefix, suffix, context.getCacheDir());
            try (FileOutputStream outStream = new FileOutputStream(tempFile)) {
                IOUtils.copy(inStream, outStream);
            }

            return new Result(tempFile, null);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(null, e);
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        _cb.onTaskFinished(result);
    }

    public interface Callback {
        void onTaskFinished(Result result);
    }

    public static class Params {
        private final Uri _uri;
        private final String _namePrefix;
        private final String _nameSuffix;

        public Params(Uri uri, String namePrefix, String nameSuffix) {
            _uri = uri;
            _namePrefix = namePrefix;
            _nameSuffix = nameSuffix;
        }

        public Uri getUri() {
            return _uri;
        }

        public String getNamePrefix() {
            return _namePrefix;
        }

        public String getNameSuffix() {
            return _nameSuffix;
        }
    }

    public static class Result {
        private final File _file;
        private final Exception _e;

        public Result(File file, Exception e) {
            _file = file;
            _e = e;
        }

        public File getFile() {
            return _file;
        }

        public Exception getException() {
            return _e;
        }
    }
}
