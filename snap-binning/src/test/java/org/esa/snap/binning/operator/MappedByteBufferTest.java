/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.snap.binning.operator;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests runtime behaviour and performance of {@link FileChannel#map(java.nio.channels.FileChannel.MapMode, long, long)}.
 * May be used to store intermediate spatial bins.
 *
 * @author Norman Fomferra
 */
public class MappedByteBufferTest {

    private static final int MiB = 1024 * 1024;

    private File file;

    @Before
    public void setUp() throws Exception {
        file = genTestFile();
    }

    @After
    public void tearDown() {
        deleteFile(file);
    }

    @Test
    public void testThatMemoryMappedFileIODoesNotConsumeHeapSpace() throws Exception {
        final int fileSize = Integer.MAX_VALUE; // 2GB!
        final long mem1, mem2, mem3, mem4;

        final RandomAccessFile raf = new RandomAccessFile(file, "rw");
        final FileChannel fc = raf.getChannel();
        try {
            mem1 = getFreeMiB();
            final MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            mem2 = getFreeMiB();

            // Modify buffer, so that it must be written when channel is closed.
            buffer.putDouble(1.2);
            buffer.putFloat(3.4f);
            buffer.putLong(fileSize - 8, 123456789L);

        } finally {
            mem3 = getFreeMiB();
            raf.close();
            mem4 = getFreeMiB();
        }

        assertTrue(file.exists());
        assertEquals(fileSize, file.length());

        System.out.println("free mem before opening: " + mem1 + " MiB");
        System.out.println("free mem after opening:  " + mem2 + " MiB");
        System.out.println("free mem before closing: " + mem3 + " MiB");
        System.out.println("free mem after closing:  " + mem4 + " MiB");

        // If these memory checks fail, check if 1 MiB is still too fine grained
        assertEquals(mem2, mem1);
        assertEquals(mem3, mem1);
        assertEquals(mem4, mem1);

        // Now make sure we get the values back
        try (DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
            assertEquals(1.2, stream.readDouble(), 1e-10); // 8 bytes
            assertEquals(3.4, stream.readFloat(), 1e-5f);  // 4 bytes
            stream.skip(fileSize - (8 + 4 + 8));
            assertEquals(123456789L, stream.readLong());
        }
    }

    @Test
    public void testThatFileMappingsCanGrow() throws Exception {

        final int chunkSize = 100 * MiB;

        final RandomAccessFile raf1 = new RandomAccessFile(file, "rw");
        final FileChannel gc1 = raf1.getChannel();
        try {
            final MappedByteBuffer buffer1 = gc1.map(FileChannel.MapMode.READ_WRITE, 0, chunkSize);
            buffer1.putDouble(0, 0.111);
            buffer1.putDouble(chunkSize - 8, 1.222);
        } finally {
            raf1.close();
            assertEquals(chunkSize, file.length());
        }

        final RandomAccessFile raf2 = new RandomAccessFile(file, "rw");
        final FileChannel fc2 = raf2.getChannel();
        try {
            final MappedByteBuffer buffer2 = fc2.map(FileChannel.MapMode.READ_WRITE, 0, 2 * chunkSize);
            assertEquals(0.111, buffer2.getDouble(0), 1e-10);
            assertEquals(1.222, buffer2.getDouble(chunkSize - 8), 1e-10);
            buffer2.putDouble(2 * chunkSize - 8, 2.333);
        } finally {
            raf2.close();
            assertEquals(2 * chunkSize, file.length());
        }

        final RandomAccessFile raf3 = new RandomAccessFile(file, "rw");
        final FileChannel fc3 = raf3.getChannel();
        try {
            final MappedByteBuffer buffer3 = fc3.map(FileChannel.MapMode.READ_WRITE, 0, 3 * chunkSize);
            assertEquals(0.111, buffer3.getDouble(0), 1e-10);
            assertEquals(1.222, buffer3.getDouble(chunkSize - 8), 1e-10);
            assertEquals(2.333, buffer3.getDouble(2 * chunkSize - 8), 1e-10);
            buffer3.putDouble(3 * chunkSize - 8, 3.444);
        } finally {
            fc3.close();
            raf3.close();
            assertEquals(3 * chunkSize, file.length());
        }

        final RandomAccessFile raf4 = new RandomAccessFile(file, "rw");
        final FileChannel fc4 = raf4.getChannel();
        try {
            final MappedByteBuffer buffer4 = fc4.map(FileChannel.MapMode.READ_WRITE, 0, 3 * chunkSize);
            assertEquals(0.111, buffer4.getDouble(0), 1e-10);
            assertEquals(1.222, buffer4.getDouble(chunkSize - 8), 1e-10);
            assertEquals(2.333, buffer4.getDouble(2 * chunkSize - 8), 1e-10);
            assertEquals(3.444, buffer4.getDouble(3 * chunkSize - 8), 1e-10);
        } finally {
            raf4.close();
            assertEquals(3 * chunkSize, file.length());
        }
    }


    private static long getFreeMiB() {
        return Runtime.getRuntime().freeMemory() / MiB;
    }

    static File genTestFile() throws IOException {
        return File.createTempFile(MappedByteBufferTest.class.getSimpleName() + "-", ".dat");
    }

    static void deleteFile(File file) {
        if (file.exists()) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }
}
