package net.bytebuddy.implementation.bytecode.constant;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.test.utility.MockitoRule;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class FieldConstantTest {

    private static final String FOO = "foo", BAR = "bar", QUX = "qux", BAZ = "baz";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private FieldDescription.InDefinedShape fieldDescription, cacheField;

    @Mock
    private TypeDescription declaringType, cacheDeclaringType, cacheFieldType;

    @Mock
    private MethodVisitor methodVisitor;

    @Mock
    private Implementation.Context implementationContext;

    @Before
    public void setUp() throws Exception {
        when(declaringType.getInternalName()).thenReturn(FOO);
        when(fieldDescription.getInternalName()).thenReturn(BAR);
        when(fieldDescription.getDeclaringType()).thenReturn(declaringType);
        when(declaringType.getDescriptor()).thenReturn("L" + QUX + ";");
        when(implementationContext.cache(new FieldConstant(fieldDescription), new TypeDescription.ForLoadedType(Field.class)))
                .thenReturn(cacheField);
        when(cacheField.getDeclaringType()).thenReturn(cacheDeclaringType);
        when(cacheField.isStatic()).thenReturn(true);
        when(cacheDeclaringType.getInternalName()).thenReturn(BAZ);
        when(cacheField.getName()).thenReturn(FOO + BAR);
        when(cacheField.getType()).thenReturn(cacheFieldType);
        when(cacheFieldType.getStackSize()).thenReturn(StackSize.SINGLE);
        when(cacheField.getInternalName()).thenReturn(FOO + BAR);
        when(cacheField.getDescriptor()).thenReturn(QUX + BAZ);
    }

    @Test
    public void testConstantCreation() throws Exception {
        StackManipulation.Size size = new FieldConstant(fieldDescription).apply(methodVisitor, implementationContext);
        assertThat(size.getSizeImpact(), is(1));
        assertThat(size.getMaximalSize(), is(2));
        verify(methodVisitor).visitLdcInsn(Type.getObjectType(QUX));
        verify(methodVisitor).visitLdcInsn(BAR);
        verify(methodVisitor).visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/lang/Class",
                "getDeclaredField",
                "(Ljava/lang/String;)Ljava/lang/reflect/Field;",
                false);
        verifyNoMoreInteractions(methodVisitor);
        verifyZeroInteractions(implementationContext);
    }

    @Test
    public void testCached() throws Exception {
        StackManipulation.Size size = new FieldConstant(fieldDescription).cached().apply(methodVisitor, implementationContext);
        assertThat(size.getSizeImpact(), is(1));
        assertThat(size.getMaximalSize(), is(1));
        verify(implementationContext).cache(new FieldConstant(fieldDescription), new TypeDescription.ForLoadedType(Field.class));
        verifyNoMoreInteractions(implementationContext);
        verify(methodVisitor).visitFieldInsn(Opcodes.GETSTATIC, BAZ, FOO + BAR, QUX + BAZ);
        verifyNoMoreInteractions(methodVisitor);
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(FieldConstant.class).apply();
        ObjectPropertyAssertion.of(FieldConstant.Cached.class).apply();
    }
}