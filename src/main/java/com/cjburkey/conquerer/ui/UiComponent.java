package com.cjburkey.conquerer.ui;

import com.artemis.Entity;
import com.cjburkey.conquerer.ecs.component.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.render.ui.UiElement;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.ecs.component.transform.Rot;
import com.cjburkey.conquerer.ecs.component.transform.Scale;
import com.cjburkey.conquerer.gl.Mesh;
import com.cjburkey.conquerer.gl.Texture;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.cjburkey.conquerer.Conquerer.*;

/**
 * Created by CJ Burkey on 2019/01/27
 */
@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
public abstract class UiComponent {

    protected Texture texture = null;
    protected boolean isFont = false;
    private Entity entity;
    private Pos pos;
    private Rot rot;
    private Scale scale;
    private MeshRender meshRender;
    private UiElement uiElement;
    private Mesh mesh = new Mesh();

    public UiComponent() {
        entity = INSTANCE.world().getEntity(INSTANCE.createEntity(Pos.class, Rot.class, Scale.class, MeshRender.class, UiElement.class));
        pos = entity.getComponent(Pos.class);
        rot = entity.getComponent(Rot.class);
        scale = entity.getComponent(Scale.class);
        meshRender = entity.getComponent(MeshRender.class);
        uiElement = entity.getComponent(UiElement.class);

        uiElement.uiComponent = this;
        mesh.canBeWireframe = false;
        meshRender.mesh = mesh;

        onEntityCreated();

        onExit.add(this::remove);
    }

    protected static int screenWidth() {
        return INSTANCE.window().getWidth();
    }

    protected static int screenHeight() {
        return INSTANCE.window().getHeight();
    }

    public final void regenerateMesh() {
        uiElement.isFont = isFont;
        uiElement.texture = texture;
        generateMesh(mesh);
    }

    public final void remove() {
        onEntityDestroy();

        mesh.destroy();
        meshRender.mesh = null;
        uiElement.texture = null;

        INSTANCE.world().deleteEntity(entity);

        entity = null;
        pos = null;
        rot = null;
        scale = null;
        meshRender = null;
        uiElement = null;
        mesh = null;
        texture = null;
        isFont = false;
    }

    protected final Entity getEntity() {
        return entity;
    }

    public final Vector3f position() {
        return pos.position;
    }

    public final Quaternionf rotation() {
        return rot.rotation;
    }

    public final Vector3f scale() {
        return scale.scale;
    }

    protected UiElement uiElement() {
        return uiElement;
    }

    // Override if necessary
    protected void onEntityCreated() {
    }

    // Override if necessary
    protected void onEntityDestroy() {
    }

    protected abstract void generateMesh(Mesh mesh);

}
