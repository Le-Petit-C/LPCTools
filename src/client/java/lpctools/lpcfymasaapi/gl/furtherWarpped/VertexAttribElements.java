package lpctools.lpcfymasaapi.gl.furtherWarpped;

import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.lpcfymasaapi.gl.VertexAttribElement;

@SuppressWarnings("unused")
public class VertexAttribElements {
    public static final VertexAttribElement F64VEC1 = new VertexAttribElement(1, Constants.DataType.DOUBLE, false);
    public static final VertexAttribElement F64VEC2 = new VertexAttribElement(2, Constants.DataType.DOUBLE, false);
    public static final VertexAttribElement F64VEC3 = new VertexAttribElement(3, Constants.DataType.DOUBLE, false);
    public static final VertexAttribElement F64VEC4 = new VertexAttribElement(4, Constants.DataType.DOUBLE, false);
    public static final VertexAttribElement F32VEC1 = new VertexAttribElement(1, Constants.DataType.FLOAT, false);
    public static final VertexAttribElement F32VEC2 = new VertexAttribElement(2, Constants.DataType.FLOAT, false);
    public static final VertexAttribElement F32VEC3 = new VertexAttribElement(3, Constants.DataType.FLOAT, false);
    public static final VertexAttribElement F32VEC4 = new VertexAttribElement(4, Constants.DataType.FLOAT, false);
    public static final VertexAttribElement I32VEC1 = new VertexAttribElement(1, Constants.DataType.INT, false);
    public static final VertexAttribElement I32VEC2 = new VertexAttribElement(2, Constants.DataType.INT, false);
    public static final VertexAttribElement I32VEC3 = new VertexAttribElement(3, Constants.DataType.INT, false);
    public static final VertexAttribElement I32VEC4 = new VertexAttribElement(4, Constants.DataType.INT, false);
    public static final VertexAttribElement U32VEC1 = new VertexAttribElement(1, Constants.DataType.UNSIGNED_INT, false);
    public static final VertexAttribElement U32VEC2 = new VertexAttribElement(2, Constants.DataType.UNSIGNED_INT, false);
    public static final VertexAttribElement U32VEC3 = new VertexAttribElement(3, Constants.DataType.UNSIGNED_INT, false);
    public static final VertexAttribElement U32VEC4 = new VertexAttribElement(4, Constants.DataType.UNSIGNED_INT, false);
    public static final VertexAttribElement FLOAT = F32VEC1;
    public static final VertexAttribElement DOUBLE = F64VEC1;
    public static final VertexAttribElement VEC1F = F32VEC1;
    public static final VertexAttribElement VEC2F = F32VEC2;
    public static final VertexAttribElement VEC3F = F32VEC3;
    public static final VertexAttribElement VEC4F = F32VEC4;
    public static final VertexAttribElement ARGB32 = new VertexAttribElement(4, Constants.DataType.UNSIGNED_BYTE, true);
}
