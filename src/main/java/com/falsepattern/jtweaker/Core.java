/**
 * Copyright (C) 2022 FalsePattern
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.jtweaker;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantUtf8;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

public class Core {
    @SuppressWarnings("deprecation")
    @SneakyThrows
    public static void removeStub(Project project) {
        recurse(new File(project.getBuildDir(), "classes"), (file) -> {
            try {
                val parser = new ClassParser(Files.newInputStream(file), file.toString());
                val clazz = parser.parse();
                val cp = clazz.getConstantPool();
                val length = cp.getLength();
                for (int i = 0; i < length; i++) {
                    val constant = cp.getConstant(i);
                    if (constant != null && constant.getTag() == Const.CONSTANT_Utf8) {
                        val utf8 = (ConstantUtf8) constant;
                        var bytes = utf8.getBytes();
                        if (bytes.contains("stubpackage/")) {
                            bytes = bytes.substring(bytes.indexOf("stubpackage/") + "stubpackage/".length());
                            utf8.setBytes(bytes);
                        }
                    }
                }
                @Cleanup val out = Files.newOutputStream(file);
                clazz.dump(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void recurse(File root, Consumer<Path> classProcessor) throws IOException {
        Files.walkFileTree(root.toPath(), new FileVisitor<Path>() {
            boolean nuke = false;
            Path nukeDir = null;

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.getFileName().toString().equals("stubpackage")) {
                    nuke = true;
                    nukeDir = dir;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!nuke && file.getFileName().toString().endsWith(".class")) {
                    classProcessor.accept(file);
                } else if (nuke) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (nuke) {
                    Files.delete(dir);
                    if (dir.equals(nukeDir)) {
                        nuke = false;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
