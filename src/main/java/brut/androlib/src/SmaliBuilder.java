/**
 *  Copyright 2014 Ryszard Wiśniewski <brut.alll@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package brut.androlib.src;

import brut.androlib.AndrolibException;
import brut.androlib.mod.SmaliMod;
import brut.androlib.res.util.ExtFile;
import brut.directory.DirectoryException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.io.IOUtils;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.writer.DexWriter;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.FileDataStore;

/**
 * @author Ryszard Wiśniewski <brut.alll@gmail.com>
 */
public class SmaliBuilder {
    public static void build(ExtFile smaliDir, File dexFile, int apiLevel) throws AndrolibException {
        new SmaliBuilder(smaliDir, dexFile, apiLevel).build();
    }

    public static void build(ExtFile smaliDir, File dexFile) throws AndrolibException {
        new SmaliBuilder(smaliDir, dexFile, 0).build();
    }

    private SmaliBuilder(ExtFile smaliDir, File dexFile, int apiLevel) {
        mSmaliDir = smaliDir;
        mDexFile = dexFile;
        mApiLevel = apiLevel;
    }

    private ConcurrentMap mInternedItems;

    private void build() throws AndrolibException {
        try {
            DexBuilder dexBuilder;
            if (mApiLevel > 0) {
                dexBuilder = DexBuilder.makeDexBuilder(Opcodes.forApi(mApiLevel));
            } else {
                dexBuilder = DexBuilder.makeDexBuilder();
            }

            try {
                Field methodSectionFiled = DexWriter.class.getDeclaredField("methodSection");
                methodSectionFiled.setAccessible(true);
                Object methodSection = methodSectionFiled.get(dexBuilder);
                Class builderMethodPoolClass = Class.forName("org.jf.dexlib2.writer.builder.BuilderMethodPool");
                Field internedItemsField = builderMethodPoolClass.getDeclaredField("internedItems");
                internedItemsField.setAccessible(true);
                mInternedItems = (ConcurrentMap) internedItemsField.get(methodSection);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (String fileName : mSmaliDir.getDirectory().getFiles(true)) {
                buildFile(fileName, dexBuilder);
                LOGGER.info("parse fileName, now method count is " + mInternedItems.values().size());
            }
            dexBuilder.writeTo(new FileDataStore( new File(mDexFile.getAbsolutePath())));
        } catch (IOException | DirectoryException ex) {
            throw new AndrolibException(ex);
        }
    }

    private void buildFile(String fileName, DexBuilder dexBuilder)
            throws AndrolibException, IOException {
        File inFile = new File(mSmaliDir, fileName);
        InputStream inStream = new FileInputStream(inFile);

        if (fileName.endsWith(".smali")) {
            try {
                if (!SmaliMod.assembleSmaliFile(inFile,dexBuilder, false, false)) {
                    throw new AndrolibException("Could not smali file: " + fileName);
                }
            } catch (IOException | RecognitionException ex) {
                throw new AndrolibException(ex);
            }
        } else {
            LOGGER.warning("Unknown file type, ignoring: " + inFile);
        }
        inStream.close();
    }

    private final ExtFile mSmaliDir;
    private final File mDexFile;
    private int mApiLevel = 0;

    private final static Logger LOGGER = Logger.getLogger(SmaliBuilder.class.getName());
}