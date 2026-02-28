package me.molybdenum.ambience_mini.engine.client.core.render.areas;

import me.molybdenum.ambience_mini.engine.client.core.areas.*;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.Vector2i;
import me.molybdenum.ambience_mini.engine.client.core.render.Vector3d;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.LineDrawer;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.areas.Vector3i;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseLevelState;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.areas.*;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class BaseAreaRenderer<TVec3, TBlockPos>
{
    private static final int ANGLE_THICKNESS = 2;
    private static final int ANGLE_WIDTH = 8;
    private static final int TEXT_MARGIN = 4;
    private static final int LINE_SEP = 2;

    // Core modules
    private BaseClientAreaManager areaManager;
    private BaseLevelState<TBlockPos, TVec3, ?, ?> level;
    private BaseNotification<?> notification;

    private final ConcurrentHashMap<Area, Cube> areaCubes = new ConcurrentHashMap<>();

    // Area manipulation
    private AreaViewMode mode = AreaViewMode.OFF;

    private Vector3i areaFromBlock = null;
    private Area selectedArea = null;
    private Cube selectedCube = null;
    private Cube.Face selectedFace = null;

    private Area lookingAtArea = null;

    // Input
    private long lastInputTime = 0;
    private boolean inputWasConfirm;

    // Rendering
    public final BaseDrawer baseDrawer;


    protected BaseAreaRenderer(BaseDrawer baseDrawer) {
        this.baseDrawer = baseDrawer;
    }

    public void init(
            BaseClientAreaManager areaManager,
            BaseLevelState<TBlockPos, TVec3, ?, ?> level,
            BaseNotification<?> notification
    ) {
        if (this.areaManager != null)
            throw new RuntimeException("Multiple calls to 'BaseAreaRenderer.init'!");
        this.areaManager = areaManager;
        this.level = level;
        this.notification = notification;

        areaManager.addUpdateListener((area, operation) -> {
            switch (operation)  {
                case PUT -> areaCubes.put(area, new Cube(area.fromBlock, area.toBlock));
                case DELETE -> areaCubes.remove(area);
            }
        });
    }

    protected Area getSelectedArea() {
        return selectedArea;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    protected abstract boolean isVisible(Cube cube);

    protected abstract IAreaScreen createAndShowAreaScreen();


    // -----------------------------------------------------------------------------------------------------------------
    // Public API
    public boolean isShowingAreaView() {
        return mode != AreaViewMode.OFF;
    }

    public AreaViewMode setOrToggleViewMode(AreaViewMode mode) {
        if (mode != null)
            this.mode = mode;
        else if (this.mode == AreaViewMode.OFF)
            this.mode = AreaViewMode.AREA_SELECTION;
        else if (this.mode == AreaViewMode.AREA_SELECTION)
            this.mode = AreaViewMode.AREA_CONSTRUCTION;
        else
            this.mode = AreaViewMode.OFF;

        areaFromBlock = null;
        selectedArea = null;
        selectedCube = null;
        selectedFace = null;

        return this.mode;
    }

    public void registerConfirm() {
        lastInputTime = System.currentTimeMillis();
        inputWasConfirm = true;
    }


    public void registerCancel() {
        lastInputTime = System.currentTimeMillis();
        inputWasConfirm = false;
    }

    public void tickAndRender(TVec3 cameraPos, float cameraRotX, float cameraRotY) {
        if (mode == AreaViewMode.OFF)
            return;

        Vector3d camPos = level.toAmVector3d(cameraPos);
        Vector3d camDir = Vector3d.ofRotationAndDistance(cameraRotX, cameraRotY, Common.AREA_SELECTION_RANGE);
        Supplier<Vector3i> lookPos = () -> level.toAmVector3i(level.getAirJustBeforeLookedAtBlockIfInRange(cameraPos, cameraRotX, cameraRotY, Common.AREA_SELECTION_RANGE));

        tickAndRenderNonSelectedAreas(camPos, camDir);
        if (selectedArea != null)
            tickAndRenderSelectedArea(camPos, camDir, lookPos.get());
        else if (mode == AreaViewMode.AREA_CONSTRUCTION)
            tickAndRenderAreaBuilder(lookPos.get());
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Area rendering
    private void tickAndRenderSelectedArea(Vector3d camPos, Vector3d camDir, Vector3i lookPos) {
        FaceHitResult hit = selectedFace == null ? selectedCube.getLookingAt(camPos, camDir) : null;
        Cube.Face lookingAt = hit != null ? hit.face : selectedFace;

        Integer offset = selectedFace != null && lookPos != null
                ? selectedFace.p1.getDirectedDistanceTo(selectedFace.direction, lookPos)
                    + (selectedFace.direction.isNegative ? 0 : 1) // If positive direction, offset is (in an intuitive sense) off by one. Fix that.
                : null;

        if (consumeConfirmInput()) {
            if (selectedFace == null) {
                if (lookingAt != null)
                    selectedFace = lookingAt;
                else {
                    // TODO: area finalization menu.

                    selectedArea.fromBlock = selectedCube.getFromBlock();
                    selectedArea.toBlock = selectedCube.getToBlock();
                    areaManager.putArea(selectedArea);
                    selectedArea = null;
                    selectedCube = null;
                    return;
                }
            }
            else if (offset != null)
            {
                if (selectedCube.canExtendOrContractBy(selectedFace.direction, offset)) {
                    selectedCube = selectedCube.extendOrContractTo(selectedFace.direction, offset);
                    selectedFace = null;
                }
                else
                    notification.printToChat(AmLang.MSG_AREA_INVALID_LOCATION);
            }
            else
                notification.printToChat(AmLang.MSG_AREA_LOOK_AT_DESTINATION);
        }

        if (isVisible(selectedCube)) {
            baseDrawer.drawLines(builder -> {
                for (var edge : selectedCube.edges)
                    builder.drawEdge(edge, getEdgeColor(edge, lookingAt), Color.ALPHA_OPAQUE);

                if (selectedFace != null && offset != null)
                    renderAreaExtension(builder, offset);
            });
            baseDrawer.drawQuads(builder -> {
                for (var face : selectedCube.faces)
                    getFaceColorAndAlpha(face, lookingAt, Color.ALPHA_OPAQUE).deconstructVoid(
                            (color, alpha) -> builder.drawFace(face, color, alpha)
                    );
            });
        }

        if (consumeCancelInput()) {
            if (selectedFace == null) {
                selectedArea = null;
                selectedCube = null;
            }
            else
                selectedFace = null;
        }
    }

    private void tickAndRenderAreaBuilder(Vector3i lookPos) {
        if (consumeCancelInput())
            areaFromBlock = null;

        if (lookPos != null) {
            if (areaFromBlock == null) {
                renderSimpleBox(lookPos, Vector3i.ONE, Color.WHITE);

                if (consumeConfirmInput())
                    areaFromBlock = lookPos;
            }
            else {
                var minAndSize = Vector3i.minAndSizeOf(areaFromBlock, lookPos);
                renderSimpleBox(minAndSize.left(), minAndSize.right(), Color.WHITE);

                if (consumeConfirmInput()) {
                    selectedArea = new Area(level.getDimensionID());
                    selectedCube = new Cube(areaFromBlock, lookPos);
                    areaFromBlock = null;
                }
            }
        }
    }

    private void tickAndRenderNonSelectedAreas(Vector3d camPos, Vector3d camDir) {
        // TODO: Render non-selected and non-highlighted cubes with slightly lower alpha

        lookingAtArea = null;
        if (mode == AreaViewMode.AREA_CONSTRUCTION) {
            // If in area construction mode, don't bother with look-at-detection
            for (var area : areaManager.areas())
                if (area != selectedArea) {
                    Cube cube = areaCubes.get(area);
                    renderCubeSimple(cube, Color.WHITE);
                }
        }
        else {
            // You can look at multiple areas at once if on a straight line. Collect candidates and highlight the closest one.
            ArrayList<Triple<Area, Cube, Double>> highlightCandidates = new ArrayList<>();

            for (var entry : areaCubes.entrySet()) {
                Area area = entry.getKey();
                if (area != selectedArea) {
                    Cube cube = entry.getValue();
                    var hit = cube.getLookingAt(camPos, camDir);
                    if (hit != null && hit.distance <= Common.AREA_SELECTION_RANGE)
                        highlightCandidates.add(new Triple<>(area, cube, hit.distance));
                    else
                        renderCubeSimple(cube, Color.WHITE); // Not a highlight-candidate. Render immediately
                }
            }

            var candidates = highlightCandidates.stream().sorted(Comparator.comparingDouble(Triple::third)).iterator();
            while (candidates.hasNext()) {
                var next = candidates.next();
                if (candidates.hasNext())
                    renderCubeSimple(next.second(), Color.WHITE); // Do not highlight the furthest ones.
                else {
                    renderCubeSimple(next.second(), Color.AREA_LOOKING); // Only highlight the closest one.
                    lookingAtArea = next.first();
                }
            }
        }
    }

    private void renderAreaExtension(LineDrawer builder, int offset) {
        Cube.Face offsetFace = selectedFace.offset(offset);

        // Face edges
        Color color = selectedCube.canExtendOrContractBy(selectedFace.direction, offset)
                ? Color.AREA_VALID : Color.AREA_ERROR;
        builder.drawLine(offsetFace.p1, offsetFace.p2, color, Color.ALPHA_OPAQUE);
        builder.drawLine(offsetFace.p2, offsetFace.p3, color, Color.ALPHA_OPAQUE);
        builder.drawLine(offsetFace.p3, offsetFace.p4, color, Color.ALPHA_OPAQUE);
        builder.drawLine(offsetFace.p4, offsetFace.p1, color, Color.ALPHA_OPAQUE);

        // Offset lines
        if (offset > 0) {
            builder.drawLine(selectedFace.p1, offsetFace.p1, Color.AREA_VALID, Color.ALPHA_OPAQUE);
            builder.drawLine(selectedFace.p2, offsetFace.p2, Color.AREA_VALID, Color.ALPHA_OPAQUE);
            builder.drawLine(selectedFace.p3, offsetFace.p3, Color.AREA_VALID, Color.ALPHA_OPAQUE);
            builder.drawLine(selectedFace.p4, offsetFace.p4, Color.AREA_VALID, Color.ALPHA_OPAQUE);
        }
    }

    private void renderCubeSimple(Cube cube, Color color) {
        if (isVisible(cube))
            renderSimpleBox(cube.p, cube.size, color);
    }

    protected void renderSimpleBox(Vector3i minimum, Vector3i size, Color color) {
        var pX = minimum.offsetX(size.x());
        var pY = minimum.offsetY(size.y());
        var pZ = minimum.offsetZ(size.z());
        var pXY = pX.offsetY(size.y());
        var pXZ = pX.offsetZ(size.z());
        var pYZ = pY.offsetZ(size.z());
        var pXYZ = minimum.add(size);

        // Box outlines
        int lineAlpha = getPulsingAlpha();
        baseDrawer.drawLines(builder -> {
            builder.drawLine(minimum, pX, color, lineAlpha);
            builder.drawLine(minimum, pY, color, lineAlpha);
            builder.drawLine(minimum, pZ, color, lineAlpha);

            builder.drawLine(pX, pXZ, color, lineAlpha);
            builder.drawLine(pX, pXY, color, lineAlpha);

            builder.drawLine(pY, pXY, color, lineAlpha);
            builder.drawLine(pY, pYZ, color, lineAlpha);

            builder.drawLine(pZ, pXZ, color, lineAlpha);
            builder.drawLine(pZ, pYZ, color, lineAlpha);

            builder.drawLine(pYZ, pXYZ, color, lineAlpha);
            builder.drawLine(pXZ, pXYZ, color, lineAlpha);
            builder.drawLine(pXY, pXYZ, color, lineAlpha);
        });

        // Box faces
        int quadAlpha = (int)(1d/3 * lineAlpha);
        baseDrawer.drawQuads(builder -> {
            builder.drawQuad(minimum, pX, pXY, pY, color, quadAlpha); // North
            builder.drawQuad(minimum, pZ, pYZ, pY, color, quadAlpha); // West

            builder.drawQuad(pXZ, pX, pXY, pXYZ, color, quadAlpha); // East
            builder.drawQuad(pXZ, pZ, pYZ, pXYZ, color, quadAlpha); // South

            builder.drawQuad(pY, pXY, pXYZ, pYZ, color, quadAlpha); // Top
            builder.drawQuad(minimum, pX, pXZ, pZ, color, quadAlpha); // Bottom
        });
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Overlay rendering
    public void renderAreaInfoOverlay(int screenWidth, int screenHeight) {
        if (lookingAtArea != null) {
            int lineHeight = baseDrawer.getLineHeight();

            String name = "Name: " + lookingAtArea.name;
            String owner = "Owner: " + "N/A";              // TODO: add owner information
            int textWidth = Integer.max(baseDrawer.getTextWidth(name), baseDrawer.getTextWidth(owner));
            int textHeight = lineHeight * 2 + LINE_SEP;

            int boxWidth = textWidth + (TEXT_MARGIN * 2);
            int boxHeight = textHeight + (TEXT_MARGIN * 2);
            Vector2i boxPos = new Vector2i(
                    (screenWidth / 2) - (boxWidth / 2),
                    (screenHeight / 2) + 10
            );

            Vector2i namePos = boxPos.offset(TEXT_MARGIN, TEXT_MARGIN);
            Vector2i ownerPos = boxPos.offset(TEXT_MARGIN, TEXT_MARGIN + lineHeight + LINE_SEP);

            drawLeftAngle(boxPos.offset(-ANGLE_WIDTH, 0), boxHeight);
            drawRightAngle(boxPos.offset(boxWidth,0), boxHeight);
            baseDrawer.drawQuads(bld -> bld.draw2dRectangle(boxPos, boxPos.offset(boxWidth, boxHeight), Color.BLACK, Color.ALPHA_OPAQUE / 2));
            baseDrawer.drawText(bld -> bld.drawText(name, namePos, Color.WHITE, Color.ALPHA_OPAQUE));
            baseDrawer.drawText(bld -> bld.drawText(owner, ownerPos, Color.WHITE, Color.ALPHA_OPAQUE));
        }
    }

    private void drawLeftAngle(Vector2i position, int height) {
        Vector2i upperLeft = position.offset(ANGLE_WIDTH - ANGLE_THICKNESS, 0);
        Vector2i upperRight = position.offset(ANGLE_WIDTH, 0);
        Vector2i middleLeft = position.offset(0, height / 2);
        Vector2i middleRight = position.offset(ANGLE_THICKNESS, height / 2);
        Vector2i bottomLeft = position.offset(ANGLE_WIDTH - ANGLE_THICKNESS, height);
        Vector2i bottomRight = position.offset(ANGLE_WIDTH, height);

        baseDrawer.drawQuads(quadDrawer -> {
            quadDrawer.draw2dQuad(upperRight, bottomRight, middleRight, middleRight, Color.BLACK, Color.ALPHA_OPAQUE / 2);
            quadDrawer.draw2dQuad(upperLeft, upperRight, middleRight, middleLeft, Color.WHITE, Color.ALPHA_OPAQUE);
            quadDrawer.draw2dQuad(middleRight, middleLeft, bottomLeft, bottomRight, Color.WHITE, Color.ALPHA_OPAQUE);
        });
    }

    private void drawRightAngle(Vector2i upperLeft, int height) {
        Vector2i upperRight = upperLeft.offset(ANGLE_THICKNESS, 0);
        Vector2i middleLeft = upperLeft.offset(ANGLE_WIDTH - ANGLE_THICKNESS, height / 2);
        Vector2i middleRight = upperLeft.offset(ANGLE_WIDTH, height / 2);
        Vector2i bottomLeft = upperLeft.offset(0, height);
        Vector2i bottomRight = upperLeft.offset(ANGLE_THICKNESS, height);

        baseDrawer.drawQuads(quadDrawer -> {
            quadDrawer.draw2dQuad(upperLeft, bottomLeft, middleLeft, middleLeft, Color.BLACK, Color.ALPHA_OPAQUE / 2);
            quadDrawer.draw2dQuad(upperLeft, upperRight, middleRight, middleLeft, Color.WHITE, Color.ALPHA_OPAQUE);
            quadDrawer.draw2dQuad(middleRight, middleLeft, bottomLeft, bottomRight, Color.WHITE, Color.ALPHA_OPAQUE);
        });
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Confirmation menu rendering
    public void openAreaScreen() {
        IAreaScreen screen = createAndShowAreaScreen();

        // TODO: Maybe nothing necessary out here?
    }

    public void renderGeneralAreaScreen(int screenWidth, int screenHeight) {
        Vector2i borderPos = new Vector2i((screenWidth - AbstractAreaScreenSymbiote.MENU_WIDTH)/2, (screenHeight - AbstractAreaScreenSymbiote.MENU_HEIGHT)/2);
        renderBorderedBox(borderPos);

        String areaNameString = notification.translateFromKey(AmLang.STRING_AREA_NAME);
        baseDrawer.drawText(textDrawer -> {
            textDrawer.drawText(areaNameString, borderPos.offset(AbstractAreaScreenSymbiote.MENU_INNER_MARGIN, AbstractAreaScreenSymbiote.MENU_INNER_MARGIN), Color.WHITE, Color.ALPHA_OPAQUE);
            //textDrawer.drawText(notification.translateFromKey(AmLang.STRING_AREA_NAME), borderPos.offset(30, 30 + 2 + baseDrawer.getLineHeight()), Color.WHITE, Color.ALPHA_OPAQUE);
        });

    }

    private void renderBorderedBox(Vector2i p1Inner) {
        Vector2i p2Inner = p1Inner.offset(AbstractAreaScreenSymbiote.MENU_SIZE.x(), 0);
        Vector2i p3Inner = p1Inner.offset(AbstractAreaScreenSymbiote.MENU_SIZE.x(), AbstractAreaScreenSymbiote.MENU_SIZE.y());
        Vector2i p4Inner = p1Inner.offset(0, AbstractAreaScreenSymbiote.MENU_SIZE.y());

        Vector2i p1Outer = p1Inner.offset(-AbstractAreaScreenSymbiote.MENU_BORDER_THICKNESS, -AbstractAreaScreenSymbiote.MENU_BORDER_THICKNESS);
        Vector2i p2Outer = p2Inner.offset(AbstractAreaScreenSymbiote.MENU_BORDER_THICKNESS, -AbstractAreaScreenSymbiote.MENU_BORDER_THICKNESS);
        Vector2i p3Outer = p3Inner.offset(AbstractAreaScreenSymbiote.MENU_BORDER_THICKNESS, AbstractAreaScreenSymbiote.MENU_BORDER_THICKNESS);
        Vector2i p4Outer = p4Inner.offset(-AbstractAreaScreenSymbiote.MENU_BORDER_THICKNESS, AbstractAreaScreenSymbiote.MENU_BORDER_THICKNESS);

        baseDrawer.drawQuads(quadDrawer -> {
            quadDrawer.draw2dQuad(p1Inner, p4Inner, p4Outer, p1Outer, Color.WHITE, Color.ALPHA_OPAQUE); // left border
            quadDrawer.draw2dQuad(p1Inner, p2Inner, p2Outer, p1Outer, Color.WHITE, Color.ALPHA_OPAQUE); // top border
            quadDrawer.draw2dQuad(p3Inner, p2Inner, p2Outer, p3Outer, Color.WHITE, Color.ALPHA_OPAQUE); // right order
            quadDrawer.draw2dQuad(p3Inner, p4Inner, p4Outer, p3Outer, Color.WHITE, Color.ALPHA_OPAQUE); // bottom order

            quadDrawer.draw2dRectangle(p1Inner, p3Inner, Color.BLACK, Color.ALPHA_OPAQUE/2);
        });
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Interaction
    private boolean consumeConfirmInput() {
        if (System.currentTimeMillis() - lastInputTime < 1000 && inputWasConfirm) {
            lastInputTime = 0;
            return true;
        }
        return false;
    }
    private boolean consumeCancelInput() {
        if (System.currentTimeMillis() - lastInputTime < 1000 && !inputWasConfirm) {
            lastInputTime = 0;
            return true;
        }
        return false;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Misc
    protected int getPulsingAlpha() {
        return (int)(255 * (0.7 + 0.3 * Math.sin(Math.PI/1000 * (System.currentTimeMillis() % 2000))));
    }

    protected Color getEdgeColor(Cube.Edge edge, @Nullable Cube.Face lookingAt) {
        return lookingAt != null && edge.touches(lookingAt.direction) ? Color.AREA_LOOKING : Color.WHITE;
    }

    protected Pair<Color, Integer> getFaceColorAndAlpha(Cube.Face face, @Nullable Cube.Face lookingAt, int baseAlpha) {
        boolean isLookingAt = face == lookingAt;
        Color c = isLookingAt ? Color.AREA_LOOKING : Color.WHITE;
        int a = isLookingAt ? (int)(0.5 * baseAlpha) : (int)(0.33 * baseAlpha);
        return new Pair<>(c, a);
    }
}
