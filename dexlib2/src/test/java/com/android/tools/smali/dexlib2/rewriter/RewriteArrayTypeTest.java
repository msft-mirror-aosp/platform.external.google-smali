/*
 * Copyright 2020, Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * Neither the name of Google LLC nor the names of its
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

package com.android.tools.smali.dexlib2.rewriter;

import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.Field;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.MethodParameter;
import com.android.tools.smali.dexlib2.immutable.ImmutableAnnotation;
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef;
import com.android.tools.smali.dexlib2.immutable.ImmutableDexFile;
import com.android.tools.smali.dexlib2.immutable.ImmutableField;
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod;
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter;
import com.android.tools.smali.dexlib2.rewriter.DexRewriter;
import com.android.tools.smali.dexlib2.rewriter.Rewriter;
import com.android.tools.smali.dexlib2.rewriter.RewriterModule;
import com.android.tools.smali.dexlib2.rewriter.Rewriters;
import com.android.tools.smali.dexlib2.rewriter.TypeRewriter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.android.tools.smali.dexlib2.AccessFlags;
import com.android.tools.smali.dexlib2.AnnotationVisibility;
import com.android.tools.smali.dexlib2.Opcodes;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;

public class RewriteArrayTypeTest {
    @Test
    public void testRewriteArrayTypeTest() {
        ClassDef class1 = new ImmutableClassDef("Lcls1;", AccessFlags.PUBLIC.getValue(), "Ljava/lang/Object;", null, null,
                Lists.newArrayList(new ImmutableAnnotation(AnnotationVisibility.RUNTIME, "Lannotation;", null)),
                Lists.<Field>newArrayList(
                        new ImmutableField("Lcls1;", "field1", "I", AccessFlags.PUBLIC.getValue(), null, null, null)
                ),
                Lists.<Method>newArrayList(
                        new ImmutableMethod("Lcls1", "method1",
                                Lists.<MethodParameter>newArrayList(new ImmutableMethodParameter("[[[Lcls1;", null, null)), "V",
                                AccessFlags.PUBLIC.getValue(), null, null, null)));

        ImmutableDexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), ImmutableSet.of(class1));


        DexRewriter rewriter = new DexRewriter(new RewriterModule() {
            @Nonnull @Override public Rewriter<String> getTypeRewriter(@Nonnull Rewriters rewriters) {
                return new TypeRewriter() {
                    @Nonnull @Override public String rewriteUnwrappedType(@Nonnull String value) {
                        if (value.equals("Lcls1;")) {
                            return "Lcls2;";
                        }
                        return value;
                    }
                };
            }
        });

        DexFile rewrittenDexFile = rewriter.getDexFileRewriter().rewrite(dexFile);

        ClassDef rewrittenClassDef = Lists.newArrayList(rewrittenDexFile.getClasses()).get(0);
        Method rewrittenMethodDef = Lists.newArrayList(rewrittenClassDef.getMethods()).get(0);

        Assert.assertEquals(rewrittenClassDef.getType(), "Lcls2;");
        Assert.assertEquals(rewrittenMethodDef.getParameterTypes().get(0), "[[[Lcls2;");
    }

    @Test
    public void testUnmodifiedArrayTypeTest() {
        ClassDef class1 = new ImmutableClassDef("Lcls1;", AccessFlags.PUBLIC.getValue(), "Ljava/lang/Object;", null, null,
                Lists.newArrayList(new ImmutableAnnotation(AnnotationVisibility.RUNTIME, "Lannotation;", null)),
                Lists.<Field>newArrayList(
                        new ImmutableField("Lcls1;", "field1", "I", AccessFlags.PUBLIC.getValue(), null, null, null)
                ),
                Lists.<Method>newArrayList(
                        new ImmutableMethod("Lcls1", "method1",
                                Lists.<MethodParameter>newArrayList(new ImmutableMethodParameter("[[[Lcls1;", null, null)), "V",
                                AccessFlags.PUBLIC.getValue(), null, null, null)));

        ImmutableDexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), ImmutableSet.of(class1));

        DexRewriter rewriter = new DexRewriter(new RewriterModule());

        DexFile rewrittenDexFile = rewriter.getDexFileRewriter().rewrite(dexFile);

        ClassDef rewrittenClassDef = Lists.newArrayList(rewrittenDexFile.getClasses()).get(0);
        Method rewrittenMethodDef = Lists.newArrayList(rewrittenClassDef.getMethods()).get(0);

        Assert.assertEquals(rewrittenClassDef.getType(), "Lcls1;");
        Assert.assertEquals(rewrittenMethodDef.getParameterTypes().get(0), "[[[Lcls1;");
    }
}
