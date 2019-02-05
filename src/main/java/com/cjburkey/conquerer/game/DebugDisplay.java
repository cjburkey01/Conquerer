package com.cjburkey.conquerer.game;

import com.cjburkey.conquerer.Conquerer;
import com.cjburkey.conquerer.engine.GameEngine;
import com.cjburkey.conquerer.gl.FontHelper;
import com.cjburkey.conquerer.ui.UiSolidBox;
import com.cjburkey.conquerer.ui.UiText;
import com.cjburkey.conquerer.util.Util;
import com.cjburkey.conquerer.util.property.BoolProperty;
import com.cjburkey.conquerer.world.EmpireHandler;
import com.cjburkey.conquerer.world.Territory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Created by CJ Burkey on 2019/02/03
 */
public class DebugDisplay {

    public final BoolProperty enabled = new BoolProperty();
    private final ObjectArrayList<UiText> text = new ObjectArrayList<>();

    public DebugDisplay() {
        FontHelper.FontBitmap font = Conquerer.Q.robotoAscii256();

        final int count = 8;
        final float size = 24.0f;

        // Background box
        UiSolidBox background = new UiSolidBox().setColor(new Vector4f(0.0f, 0.0f, 0.0f, 0.75f));
        for (int i = 0; i < count; ) {
            UiText text = new UiText(null, font)
                .setColor(new Vector3f(1.0f, 1.0f, 1.0f))
                .setSize(size);
            text.position().set(20.0f, i++ * size + 6.0f, 0.0f);
            background.add(text);
            this.text.add(text);
        }
        background.setSize(new Vector2f(375.0f, count * size + 12.0f));
        background.position().add(10.0f, 10.0f, 0.0f);

        // Toggle
        enabled.listen((o, n) -> {
            text.forEach(ui -> ui.visible.set(n));
            background.visible.set(n);
        });
        enabled.set(true);
    }

    @SuppressWarnings("UnusedAssignment")
    public void updateDisplay() {
        if (!enabled.getb()) return;

        int i = 0;

        text.get(i++).setText(String.format("FPS: %.2f", 1.0f / GameEngine.gameLoop().lastRenderDelta()));
        text.get(i++).setText(String.format("UPS: %.2f", 1.0f / GameEngine.gameLoop().getUpdateDelta()));

        Territory at = Conquerer.Q.worldHandler.getTerritoryUnderMouse();
        text.get(i++).setText(String.format("Territory: %s", ((at == null) ? "None" : at.name)));
        text.get(i++).setText(String.format("Biome: %s", ((at == null) ? "None" : at.getBiome().name)));
        text.get(i++).setText(String.format("Location: %s", ((at == null) ? "None" : "(" + Util.format(at.location) + ")")));
        text.get(i++).setText(String.format("Ocean: %s", ((at == null) ? "Talse" : (at.isWater ? "True" : "False"))));
        EmpireHandler.Empire empire = ConquererHandler.playerEmpire.get();
        text.get(i++).setText(String.format("Current Empire: %s", ((empire == null) ? "None" : empire.name)));
        text.get(i++).setText(String.format("Territory Owner: %s", ((at == null || at.getCurrentOwner() == null) ? "None" : at.getCurrentOwner().name)));
    }

}
