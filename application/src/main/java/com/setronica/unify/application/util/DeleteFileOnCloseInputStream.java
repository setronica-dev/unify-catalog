package com.setronica.unify.application.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DeleteFileOnCloseInputStream extends FileInputStream {
    private File file;

    public DeleteFileOnCloseInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (file != null) {
            file.delete();
            file = null;
        }
    }
}
