/*
 * Copyright 2013, Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google LLC nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.tools.smali.dexlib2.analysis;

import com.android.tools.smali.dexlib2.analysis.ClassPath;
import com.android.tools.smali.dexlib2.analysis.ClassPathResolver;
import com.android.tools.smali.dexlib2.analysis.CustomInlineMethodResolver;
import com.android.tools.smali.dexlib2.analysis.InlineMethodResolver;
import com.android.tools.smali.dexlib2.analysis.MethodAnalyzer;
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef;
import com.android.tools.smali.dexlib2.immutable.ImmutableDexFile;
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod;
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation;
import com.android.tools.smali.dexlib2.immutable.ImmutableMultiDexContainer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.android.tools.smali.dexlib2.AccessFlags;
import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction;
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction10x;
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction35mi;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class CustomMethodInlineTableTest {
    @Test
    public void testCustomMethodInlineTable_Virtual() throws IOException {
        List<ImmutableInstruction> instructions = Lists.newArrayList(
                new ImmutableInstruction35mi(Opcode.EXECUTE_INLINE, 1, 0, 0, 0, 0, 0, 0),
                new ImmutableInstruction10x(Opcode.RETURN_VOID));

        ImmutableMethodImplementation methodImpl = new ImmutableMethodImplementation(1, instructions, null, null);
        ImmutableMethod method = new ImmutableMethod("Lblah;", "blah", null, "V", AccessFlags.PUBLIC.getValue(), null,
                null, methodImpl);

        ClassDef classDef = new ImmutableClassDef("Lblah;", AccessFlags.PUBLIC.getValue(), "Ljava/lang/Object;", null,
                null, null, null, null, null, ImmutableList.of(method));

        ImmutableDexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), ImmutableList.of(classDef));

        ImmutableMultiDexContainer container = new ImmutableMultiDexContainer(ImmutableMap.of("classes.dex", dexFile));

        ClassPathResolver resolver = new ClassPathResolver(ImmutableList.<String>of(),
                ImmutableList.<String>of(), ImmutableList.<String>of(), container.getEntry("classes.dex"));
        ClassPath classPath = new ClassPath(resolver.getResolvedClassProviders(), false, ClassPath.NOT_ART);

        InlineMethodResolver inlineMethodResolver = new CustomInlineMethodResolver(classPath, "Lblah;->blah()V");
        MethodAnalyzer methodAnalyzer = new MethodAnalyzer(classPath, method, inlineMethodResolver, false);

        Instruction deodexedInstruction = methodAnalyzer.getInstructions().get(0);
        Assert.assertEquals(Opcode.INVOKE_VIRTUAL, deodexedInstruction.getOpcode());

        MethodReference methodReference = (MethodReference)((Instruction35c)deodexedInstruction).getReference();
        Assert.assertEquals(method, methodReference);
    }

    @Test
    public void testCustomMethodInlineTable_Static() throws IOException {
        List<ImmutableInstruction> instructions = Lists.newArrayList(
                new ImmutableInstruction35mi(Opcode.EXECUTE_INLINE, 1, 0, 0, 0, 0, 0, 0),
                new ImmutableInstruction10x(Opcode.RETURN_VOID));

        ImmutableMethodImplementation methodImpl = new ImmutableMethodImplementation(1, instructions, null, null);
        ImmutableMethod method = new ImmutableMethod("Lblah;", "blah", null, "V", AccessFlags.STATIC.getValue(), null,
                null, methodImpl);

        ClassDef classDef = new ImmutableClassDef("Lblah;", AccessFlags.PUBLIC.getValue(), "Ljava/lang/Object;", null,
                null, null, null, null, ImmutableList.of(method), null);

        ImmutableDexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), ImmutableList.of(classDef));

        ImmutableMultiDexContainer container = new ImmutableMultiDexContainer(ImmutableMap.of("classes.dex", dexFile));

        ClassPathResolver resolver = new ClassPathResolver(ImmutableList.<String>of(),
                ImmutableList.<String>of(), ImmutableList.<String>of(), container.getEntry("classes.dex"));
        ClassPath classPath = new ClassPath(resolver.getResolvedClassProviders(), false, ClassPath.NOT_ART);

        InlineMethodResolver inlineMethodResolver = new CustomInlineMethodResolver(classPath, "Lblah;->blah()V");
        MethodAnalyzer methodAnalyzer = new MethodAnalyzer(classPath, method, inlineMethodResolver, false);

        Instruction deodexedInstruction = methodAnalyzer.getInstructions().get(0);
        Assert.assertEquals(Opcode.INVOKE_STATIC, deodexedInstruction.getOpcode());

        MethodReference methodReference = (MethodReference)((Instruction35c)deodexedInstruction).getReference();
        Assert.assertEquals(method, methodReference);
    }

    @Test
    public void testCustomMethodInlineTable_Direct() throws IOException {
        List<ImmutableInstruction> instructions = Lists.newArrayList(
                new ImmutableInstruction35mi(Opcode.EXECUTE_INLINE, 1, 0, 0, 0, 0, 0, 0),
                new ImmutableInstruction10x(Opcode.RETURN_VOID));

        ImmutableMethodImplementation methodImpl = new ImmutableMethodImplementation(1, instructions, null, null);
        ImmutableMethod method = new ImmutableMethod("Lblah;", "blah", null, "V", AccessFlags.PRIVATE.getValue(), null,
                null, methodImpl);

        ClassDef classDef = new ImmutableClassDef("Lblah;", AccessFlags.PUBLIC.getValue(), "Ljava/lang/Object;", null,
                null, null, null, null, ImmutableList.of(method), null);

        ImmutableDexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), ImmutableList.of(classDef));

        ImmutableMultiDexContainer container = new ImmutableMultiDexContainer(ImmutableMap.of("classes.dex", dexFile));

        ClassPathResolver resolver = new ClassPathResolver(ImmutableList.<String>of(),
                ImmutableList.<String>of(), ImmutableList.<String>of(), container.getEntry("classes.dex"));
        ClassPath classPath = new ClassPath(resolver.getResolvedClassProviders(), false, ClassPath.NOT_ART);

        InlineMethodResolver inlineMethodResolver = new CustomInlineMethodResolver(classPath, "Lblah;->blah()V");
        MethodAnalyzer methodAnalyzer = new MethodAnalyzer(classPath, method, inlineMethodResolver, false);

        Instruction deodexedInstruction = methodAnalyzer.getInstructions().get(0);
        Assert.assertEquals(Opcode.INVOKE_DIRECT, deodexedInstruction.getOpcode());

        MethodReference methodReference = (MethodReference)((Instruction35c)deodexedInstruction).getReference();
        Assert.assertEquals(method, methodReference);
    }
}
