import { Application, Color, Container, Graphics, Text } from "pixi.js";
import type { RenderModel } from "./renderModel";

function drawHex(graphics: Graphics, x: number, y: number, radius: number, fill: number) {
  const angleOffset = Math.PI / 6;
  graphics.moveTo(
    x + Math.cos(angleOffset) * radius,
    y + Math.sin(angleOffset) * radius
  );

  for (let side = 1; side <= 6; side += 1) {
    const angle = angleOffset + (Math.PI / 3) * side;
    graphics.lineTo(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
  }

  graphics.closePath();
  graphics.fill(fill);
  graphics.stroke({ color: 0xf8e7b9, width: 2, alpha: 0.6 });
}

function drawFeatureMarker(graphics: Graphics, x: number, y: number) {
  graphics.circle(x, y, 18);
  graphics.stroke({ color: 0xb57cff, width: 4, alpha: 0.95 });
}

function projectAxialToScreen(q: number, r: number) {
  const originX = 170;
  const originY = 190;
  const horizontalStep = 95;
  const verticalStep = 82;

  return {
    x: originX + q * horizontalStep,
    y: originY + r * verticalStep + q * 0
  };
}

export async function mountPreviewScene(host: HTMLDivElement, renderModel: RenderModel) {
  const app = new Application();
  await app.init({
    resizeTo: host,
    antialias: true,
    background: new Color("#11161f")
  });

  host.replaceChildren(app.canvas);

  const root = new Container();
  app.stage.addChild(root);

  const title = new Text({
    text: renderModel.title,
    style: {
      fill: "#f7f1df",
      fontFamily: "Georgia, serif",
      fontSize: 28,
      fontWeight: "600"
    }
  });
  title.position.set(28, 24);
  root.addChild(title);

  const subtitle = new Text({
    text: renderModel.subtitle,
    style: {
      fill: "#9cb2c5",
      fontFamily: "Georgia, serif",
      fontSize: 14
    }
  });
  subtitle.position.set(30, 60);
  root.addChild(subtitle);

  for (const tile of renderModel.terrainTiles) {
    const position = projectAxialToScreen(tile.q, tile.r);
    const hex = new Graphics();
    drawHex(hex, position.x, position.y, 56, tile.fill);
    if (tile.terrainHidden) {
      hex.alpha = 0.42;
    }
    root.addChild(hex);

    if (tile.featureHidden) {
      const marker = new Graphics();
      drawFeatureMarker(marker, position.x, position.y);
      root.addChild(marker);
    }

    const label = new Text({
      text: tile.visibilityLabel ? `${tile.label}\n${tile.visibilityLabel}` : tile.label,
      style: {
        fill: "#f9f6ef",
        fontFamily: "Georgia, serif",
        fontSize: 15,
        fontWeight: "700"
      }
    });
    label.anchor.set(0.5);
    label.position.set(position.x, position.y);
    root.addChild(label);
  }

  return () => {
    app.destroy(true, { children: true });
  };
}
