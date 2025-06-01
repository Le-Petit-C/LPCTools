package lpctools.lpcfymasaapi.gl.furtherWarpped;

import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.lpcfymasaapi.gl.VertexAttribElement;

@SuppressWarnings("unused")
public class VertexAttribElements {
    public static final VertexAttribElement FLOAT = new VertexAttribElement(1, Constants.DataType.FLOAT, false);
    public static final VertexAttribElement ARGB32 = new VertexAttribElement(4, Constants.DataType.UNSIGNED_BYTE, true);
    public static final VertexAttribElement VEC3F = new VertexAttribElement(3, Constants.DataType.FLOAT, false);
    public static final VertexAttribElement VEC4F = new VertexAttribElement(4, Constants.DataType.FLOAT, false);
}
