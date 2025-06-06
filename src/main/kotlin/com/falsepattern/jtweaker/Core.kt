@file:OptIn(ExperimentalPathApi::class)

package com.falsepattern.jtweaker

import org.apache.bcel.Const
import org.apache.bcel.classfile.ClassParser
import org.apache.bcel.classfile.Constant
import org.apache.bcel.classfile.ConstantUtf8
import org.gradle.api.file.FileCollection
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.io.path.*

object Core {
    private val fullMatcher = Pattern.compile("(?:\\w+/)*stubpackage/((?:\\w+/)*\\w+(\\$\\w+)*)")
    private val partialMatcher = Pattern.compile("L${fullMatcher.pattern()};")

    fun removeStub(roots: FileCollection) {
        recurse(roots.mapNotNull { it?.toPath() }) { file ->
            val clazz = file.inputStream().use { input ->
                ClassParser(input, file.toString()).parse()
            }
            val pool = clazz.constantPool
            val length = pool.length
            var modified = false
            var prevLarge = false
            for (i in 1 until length) {
                if (prevLarge) {
                    prevLarge = false
                    continue
                }
                val constant = pool.getConstant<Constant>(i) ?: continue
                when(constant.tag) {
                    Const.CONSTANT_Double, Const.CONSTANT_Long -> {
                        prevLarge = true
                    }
                    Const.CONSTANT_Utf8 -> {
                        val utf8 = constant as ConstantUtf8
                        val bytes = utf8.bytes
                        val matcher = fullMatcher.matcher(bytes)

                        val resultBytes: String
                        val matched: Boolean
                        if (matcher.matches()) {
                            modified = true
                            matched = true
                            resultBytes = matcher.group(1)
                        } else {
                            val partialMatcher = partialMatcher.matcher(bytes)
                            if (partialMatcher.find()) {
                                matched = true
                                modified = true;
                                val result = StringBuilder()
                                var currPos = 0
                                do {
                                    result.append(bytes, currPos, partialMatcher.start())
                                    result.append('L')
                                    result.append(partialMatcher.group(1))
                                    result.append(';')
                                    currPos = partialMatcher.end()
                                } while (partialMatcher.find())
                                result.append(bytes, currPos, bytes.length)
                                resultBytes = result.toString()
                            } else {
                                matched = false
                                resultBytes = bytes
                            }
                        }
                        if (matched) {
                            pool.setConstant(i, ConstantUtf8(resultBytes))
                        }
                    }
                }
            }
            if (modified) {
                file.outputStream().use { out ->
                    clazz.dump(out)
                }
            }
        }
    }

    private fun recurse(root: Collection<Path>, classProcessor: (Path) -> Unit) {
        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
        var nuke = false
        var nukeDir: Path? = null
        root.forEach { subRoot ->
            try {
                Files.walkFileTree(subRoot, fileVisitor {
                    onPreVisitDirectory { dir, _ ->
                        if (dir.name == "stubpackage") {
                            nuke = true
                            nukeDir = dir
                        }
                        CONTINUE
                    }
                    onVisitFile { file, _ ->
                        if (!nuke && file.name.endsWith(".class")) {
                            executor.execute {
                                classProcessor(file)
                            }
                        } else if (nuke) {
                            file.deleteIfExists()
                        }
                        CONTINUE
                    }
                    onPostVisitDirectory { dir, _ ->
                        if (nuke) {
                            dir.deleteIfExists()
                            if (dir == nukeDir) {
                                nuke = false
                            }
                        }
                        CONTINUE
                    }
                })
            } catch (_: NoSuchFileException) {}
        }
        executor.shutdown()
        while (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
            Throwable("JTweaker is taking a long time!").printStackTrace()
        }
    }
}