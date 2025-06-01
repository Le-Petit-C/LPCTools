package lpctools.lpcfymasaapi.gl.furtherWarpped;

import lpctools.lpcfymasaapi.gl.VertexAttrib;

import static lpctools.lpcfymasaapi.gl.furtherWarpped.VertexAttribElements.*;

public class VertexTypes {
    public static final PositionColor POSITION_COLOR = new PositionColor();
    public static class PositionColor extends VertexAttrib{
        private PositionColor(){super(VEC3F, ARGB32);}
    }
}
