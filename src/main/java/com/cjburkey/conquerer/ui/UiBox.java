package com.cjburkey.conquerer.ui;

import com.cjburkey.conquerer.ecs.component.engine.Transform;
import com.cjburkey.conquerer.gl.Mesh;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/27
 */
@SuppressWarnings({"UnusedReturnValue"})
public class UiBox extends UiComponent implements IUiHandler {

    private final ObjectArrayList<UiComponent> children = new ObjectArrayList<>();

    public UiBox setSize(Vector2fc size) {
        this.size.set(size);
        regenerateMesh();
        return this;
    }

    @Override
    protected void generateMesh(Mesh mesh) {
    }

    @Override
    public void add(UiComponent component) {
        if (component.equals(this)) return;
        component.getEntity().getComponent(Transform.class).parent = getEntity().getComponent(Transform.class);
        children.add(component);
    }

    @Override
    public int indexOf(UiComponent component) {
        return children.indexOf(component);
    }

    @Override
    public void rem(int componentId) {
        children.remove(componentId);
    }

    @Override
    public int getCount() {
        return children.size();
    }

    @Override
    public UiComponent get(int componentId) {
        return children.get(componentId);
    }

}
