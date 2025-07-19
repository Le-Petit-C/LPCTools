package lpctools.lpcfymasaapi.configButtons;

import lpctools.lpcfymasaapi.interfaces.ILPCConfig;

@SuppressWarnings("unused")
public class UpdateTodo {
    public boolean valueChanged = false;
    public boolean updatePage = false;
    public UpdateTodo valueChanged(boolean b){valueChanged |= b; return this;}
    @SuppressWarnings("UnusedReturnValue")
	public UpdateTodo updatePage(boolean b){updatePage |= b; return this;}
    public UpdateTodo valueChanged(){valueChanged = true; return this;}
    public UpdateTodo updatePage(){updatePage = true; return this;}
    public void combine(UpdateTodo todo){
        valueChanged |= todo.valueChanged;
        updatePage |= todo.updatePage;
    }
    public UpdateTodo apply(ILPCConfig config){
        if(valueChanged) config.onValueChanged();
        return this;
    }
}
