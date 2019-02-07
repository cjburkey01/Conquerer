package com.cjburkey.conquerer.ui;

import com.artemis.Entity;
import com.cjburkey.conquerer.ecs.component.engine.Transform;
import com.cjburkey.conquerer.ecs.component.engine.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.engine.render.ui.UiElement;
import com.cjburkey.conquerer.engine.GameEngine;
import com.cjburkey.conquerer.gl.Mesh;
import com.cjburkey.conquerer.gl.Texture;
import com.cjburkey.conquerer.util.property.BoolProperty;
import java.util.Objects;
import java.util.UUID;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Created by CJ Burkey on 2019/01/27
 */
@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
public abstract class UiComponent {

    public final BoolProperty visible = new BoolProperty(true);
    protected final Vector2f size = new Vector2f();
    private final UUID uuid = UUID.randomUUID();
    protected Texture texture = null;
    protected boolean isFont = false;
    private Entity entity;
    private Transform transform;
    private MeshRender meshRender;
    private UiElement uiElement;
    private Mesh mesh = new Mesh();

    public UiComponent() {
        entity = GameEngine.instantiateEntity(Transform.class, MeshRender.class, UiElement.class);
        transform = entity.getComponent(Transform.class);
        meshRender = entity.getComponent(MeshRender.class);
        uiElement = entity.getComponent(UiElement.class);

        uiElement.uiComponent = this;
        mesh.canBeWireframe = false;
        meshRender.mesh = mesh;

        onEntityCreated();

        visible.listen((o, n) -> meshRender.mesh = (n ? mesh : null));

        GameEngine.onExit(this::remove);
    }

    protected static int screenWidth() {
        return GameEngine.window().getWidth();
    }

    protected static int screenHeight() {
        return GameEngine.window().getHeight();
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

        GameEngine.delete(entity);

        entity = null;
        transform = null;
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
        return transform.position;
    }

    public final Quaternionf rotation() {
        return transform.rotation;
    }

    public final Vector3f scale() {
        return transform.scale;
    }

    public final Vector2f size() {
        return size;
    }

    public UiElement uiElement() {
        return uiElement;
    }

    // Override if necessary
    protected void onEntityCreated() {
    }

    // Override if necessary
    protected void onEntityDestroy() {
    }

    // Override if necessary
    public void onEntityUpdate() {
    }

    protected abstract void generateMesh(Mesh mesh);

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UiComponent that = (UiComponent) o;
        return uuid.equals(that.uuid);
    }

    public int hashCode() {
        return Objects.hash(uuid);
    }

}
