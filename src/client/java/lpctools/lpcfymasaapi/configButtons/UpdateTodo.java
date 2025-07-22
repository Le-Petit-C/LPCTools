package lpctools.lpcfymasaapi.configButtons;

import lpctools.lpcfymasaapi.interfaces.ILPCConfig;

@SuppressWarnings("unused")
public class UpdateTodo {
    public boolean valueChanged = false;
    public UpdateTodo valueChanged(boolean b){valueChanged |= b; return this;}
    public UpdateTodo valueChanged(){valueChanged = true; return this;}
    public void combine(UpdateTodo todo){valueChanged |= todo.valueChanged;}
    public UpdateTodo apply(ILPCConfig config){
        if(valueChanged) config.onValueChanged();
        return this;
    }
}
