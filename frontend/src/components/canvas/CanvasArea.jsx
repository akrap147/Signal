import React, { useRef, useEffect, useLayoutEffect } from "react";
import {
  subscribeToCanvas,
  publishDraw,
  fetchHistory,
} from "../../stores/useCanvasStore";
import useChatStore from "../../stores/useChatStore";
import useAuthStore from "../../stores/useAuthStore";

function applyEvent(ctx, event) {
  if (event.type === "CLEAR") {
    ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
    return;
  }
  ctx.beginPath();
  ctx.globalCompositeOperation =
    event.type === "ERASE" ? "destination-out" : "source-over";
  ctx.strokeStyle = event.color || "#ffffff";
  ctx.lineWidth = event.brushSize || 4;
  ctx.lineCap = "round";
  ctx.lineJoin = "round";
  ctx.moveTo(event.prevX, event.prevY);
  ctx.lineTo(event.x, event.y);
  ctx.stroke();
}

const CanvasArea = ({ roomId }) => {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const isDrawing = useRef(false);
  const lastPos = useRef({ x: 0, y: 0 });

  // UI state
  const [color, setColor] = React.useState("#ffffff");
  const [brushSize, setBrushSize] = React.useState(4);
  const [isEraser, setIsEraser] = React.useState(false);

  // Refs for event handlers (always current, no stale closures)
  const colorRef = useRef(color);
  const brushSizeRef = useRef(brushSize);
  const isEraserRef = useRef(isEraser);
  useEffect(() => {
    colorRef.current = color;
  }, [color]);
  useEffect(() => {
    brushSizeRef.current = brushSize;
  }, [brushSize]);
  useEffect(() => {
    isEraserRef.current = isEraser;
  }, [isEraser]);

  const { isConnected } = useChatStore();
  const { user } = useAuthStore();
  const userId = user?.id;

  // 캔버스 크기를 컨테이너에 맞춤
  useLayoutEffect(() => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (!canvas || !container) return;
    canvas.width = container.clientWidth || 800;
    canvas.height = container.clientHeight || 600;
  }, []);

  // STOMP 구독 + 히스토리 재생
  useEffect(() => {
    if (!roomId || !isConnected) return;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");

    fetchHistory(roomId).then((events) => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      events.forEach((event) => applyEvent(ctx, event));
    });

    const subscription = subscribeToCanvas(roomId, (event) => {
      // 자신의 이벤트는 로컬에서 이미 그렸으므로 스킵 (타입 불일치 방지를 위해 String 비교)
      if (String(event.userId) === String(userId)) return;
      applyEvent(ctx, event);
    });

    return () => subscription?.unsubscribe();
  }, [roomId, isConnected, userId]);

  // Native 이벤트 리스너로 드로잉 처리 (React synthetic event의 stale closure 문제 회피)
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const getPos = (e) => {
      const rect = canvas.getBoundingClientRect();
      return { x: e.clientX - rect.left, y: e.clientY - rect.top };
    };

    const onMouseDown = (e) => {
      isDrawing.current = true;
      lastPos.current = getPos(e);
    };

    const onMouseMove = (e) => {
      if (!isDrawing.current) return;

      const ctx = canvas.getContext("2d");
      const { x, y } = getPos(e);
      const { x: prevX, y: prevY } = lastPos.current;
      const eraser = isEraserRef.current;
      const currentColor = colorRef.current;
      const currentBrushSize = brushSizeRef.current;

      ctx.beginPath();
      ctx.globalCompositeOperation = eraser ? "destination-out" : "source-over";
      ctx.strokeStyle = eraser ? "rgba(0,0,0,1)" : currentColor;
      ctx.lineWidth = currentBrushSize;
      ctx.lineCap = "round";
      ctx.lineJoin = "round";
      ctx.moveTo(prevX, prevY);
      ctx.lineTo(x, y);
      ctx.stroke();

      publishDraw({
        type: eraser ? "ERASE" : "DRAW",
        roomId,
        userId,
        x,
        y,
        prevX,
        prevY,
        color: eraser ? "#000000" : currentColor,
        brushSize: currentBrushSize,
      });

      lastPos.current = { x, y };
    };

    const onMouseUp = () => {
      isDrawing.current = false;
    };

    canvas.addEventListener("mousedown", onMouseDown);
    canvas.addEventListener("mousemove", onMouseMove);
    canvas.addEventListener("mouseup", onMouseUp);
    canvas.addEventListener("mouseleave", onMouseUp);

    return () => {
      canvas.removeEventListener("mousedown", onMouseDown);
      canvas.removeEventListener("mousemove", onMouseMove);
      canvas.removeEventListener("mouseup", onMouseUp);
      canvas.removeEventListener("mouseleave", onMouseUp);
    };
  }, [roomId, userId]);

  const handleClear = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    publishDraw({
      type: "CLEAR",
      roomId,
      userId,
      x: 0,
      y: 0,
      prevX: 0,
      prevY: 0,
      color: "#000000",
      brushSize: 0,
    });
  };

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        flex: 1,
        overflow: "hidden",
      }}
    >
      {/* 툴바 */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: "12px",
          padding: "8px 16px",
          background: "var(--bg-tertiary)",
          borderBottom: "1px solid var(--border-subtle)",
          flexShrink: 0,
        }}
      >
        <input
          type="color"
          value={color}
          onChange={(e) => {
            setColor(e.target.value);
            setIsEraser(false);
          }}
          title="색상 선택"
          style={{
            width: "28px",
            height: "28px",
            padding: 0,
            border: "none",
            background: "none",
            cursor: "pointer",
            borderRadius: "4px",
          }}
        />
        <span style={{ color: "var(--text-muted)", fontSize: "0.8rem" }}>
          크기
        </span>
        <input
          type="range"
          min="1"
          max="40"
          value={brushSize}
          onChange={(e) => setBrushSize(Number(e.target.value))}
          style={{ width: "80px" }}
        />
        <span
          style={{
            color: "var(--text-muted)",
            fontSize: "0.8rem",
            minWidth: "20px",
          }}
        >
          {brushSize}
        </span>
        <button
          onClick={() => setIsEraser((v) => !v)}
          style={{
            padding: "4px 12px",
            borderRadius: "6px",
            border: "none",
            cursor: "pointer",
            background: isEraser ? "#5865f2" : "var(--bg-hover)",
            color: "var(--text-primary)",
            fontSize: "0.8rem",
          }}
        >
          {isEraser ? "지우개 ✓" : "지우개"}
        </button>
        <button
          onClick={handleClear}
          style={{
            padding: "4px 12px",
            borderRadius: "6px",
            border: "none",
            cursor: "pointer",
            background: "#ed4245",
            color: "#fff",
            fontSize: "0.8rem",
          }}
        >
          전체 지우기
        </button>
      </div>

      {/* 캔버스 */}
      <div
        ref={containerRef}
        style={{
          flex: 1,
          overflow: "hidden",
          background: "#1e1f22",
          position: "relative",
        }}
      >
        <canvas
          ref={canvasRef}
          style={{ display: "block", cursor: isEraser ? "cell" : "crosshair" }}
        />
      </div>
    </div>
  );
};

export default CanvasArea;
